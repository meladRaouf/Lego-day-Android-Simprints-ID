package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.tools.extensions.trace
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

class SignerManagerTest {

    @MockK lateinit var projectRepository: ProjectRepository
    @MockK lateinit var remoteDbManager: RemoteDbManager
    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var peopleUpSyncMaster: PeopleUpSyncMaster
    @MockK lateinit var downSyncDao: DownSyncDao
    @MockK lateinit var upSyncDao: UpSyncDao
    private lateinit var signerManager: SignerManagerImpl

    private val token = Token("some_token")

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        signerManager = SignerManagerImpl(projectRepository, remoteDbManager, loginInfoManager, preferencesManager, peopleUpSyncMaster, downSyncDao, upSyncDao)
        mockkStatic("com.simprints.id.tools.extensions.PerformanceMonitoring_extKt")
        every { any<Completable>().trace(any()) }.answers { this.value }
    }

    @Test
    fun signIn_shouldSignInToRemoteDb() {
        mockRemoteSignedIn()

        signIn()

        verify { remoteDbManager.signIn(token.value) }
    }

    @Test
    fun signInToRemoteFails_signInShouldFail() {
        mockRemoteSignedIn(true)

        val tester = signIn()

        verifySignedInFailed(tester)
    }

    @Test
    fun signIn_shouldStoreCredentialsLocally() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()

        signIn()

        verify { loginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }
    }

    @Test
    fun storeCredentialsFails_signInShouldFail() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally(true)

        val tester = signIn()

        verifySignedInFailed(tester)
    }

    @Test
    fun signIn_shouldFetchProjectInfo() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto()

        signIn()

        coVerify { projectRepository.loadAndRefreshCache(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun loadAndRefreshCacheFails_signInShouldFail() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto(true)

        val tester = signIn()

        verifySignedInFailed(tester)
    }

    @Test
    fun signIn_shouldResumePeopleSync() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto()
        mockResumePeopleSync()
        signIn()
        coVerify { peopleUpSyncMaster.resume(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun resumeProjectSyncFails_signInShouldFail() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto()
        mockResumePeopleSync(true)

        val tester = signIn()

        verifySignedInFailed(tester)
    }

    @Test
    fun signIn_shouldSucceed() {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto()
        mockResumePeopleSync()

        val tester = signIn()

        tester.awaitAndAssertSuccess()
    }


    @Test
    fun signOut_shouldRemoveAnyState() {
        every { loginInfoManager.signedInProjectId } returns DEFAULT_PROJECT_ID

        signerManager.signOut()

        verifyUpSyncGotPaused()
        verifyStoredCredentialsGotCleaned()
        verifyRemoteManagerGotSignedOut()
        verifyLastDownSyncInfoGotDeleted()
        verifyLastUpSyncInfoGotDeleted()
        verifyAllSharedPreferencesExceptRealmKeysGotCleared()
    }

    private fun signIn() = signerManager.signIn(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, token).test()

    private fun mockStoreCredentialsLocally(error: Boolean = false) =
        every { loginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to store credentials"))
            }
        }

    private fun mockRemoteSignedIn(error: Boolean = false) =
        every { remoteDbManager.signIn(token.value) }.apply {
            this.returns(
                if (!error) {
                    Completable.complete()
                } else {
                    Completable.error(Throwable("Failed to remote sign in"))
                })
        }

    private fun mockFetchingProjectInto(error: Boolean = false) =
        coEvery { projectRepository.loadAndRefreshCache(any()) }.apply {
            if (!error) {
                this.returns(Project())
            } else {
                this.throws(Throwable("Failed to fetch project info"))
            }
        }

    private fun mockResumePeopleSync(error: Boolean = false) =
        coEvery { peopleUpSyncMaster.resume(any()) }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to resume upsync"))
            }
        }

    private fun verifyUpSyncGotPaused() = verify { peopleUpSyncMaster.pause(DEFAULT_PROJECT_ID) }
    private fun verifyStoredCredentialsGotCleaned() = verify { loginInfoManager.cleanCredentials() }
    private fun verifyRemoteManagerGotSignedOut() = verify { remoteDbManager.signOut() }
    private fun verifyLastDownSyncInfoGotDeleted() = verify { downSyncDao.deleteAll() }
    private fun verifyLastUpSyncInfoGotDeleted() = verify { upSyncDao.deleteAll() }
    private fun verifyAllSharedPreferencesExceptRealmKeysGotCleared() = verify { preferencesManager.clearAllSharedPreferencesExceptRealmKeys() }

    private fun verifySignedInFailed(it: TestObserver<Void>) {
        assertThat(it.errorCount()).isEqualTo(1)
    }
}