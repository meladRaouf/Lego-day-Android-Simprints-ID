package com.simprints.id.data.db.session.remote

import android.net.NetworkInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.callback.*
import com.simprints.id.data.db.session.domain.models.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.VerificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.android.waitOnSystem
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SessionRemoteDataSourceImplAndroidTest {

    companion object {
        const val SIGNED_ID_USER = "some_signed_user"
        const val CLOUD_ASYNC_SESSION_CREATION_TIMEOUT = 5000L
        val RANDOM_GUID = UUID.randomUUID().toString()
    }

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
    private val timeHelper = TimeHelperImpl()

    @get:Rule
    val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    private lateinit var sessionRemoteDataSource: SessionRemoteDataSource
    @MockK var remoteDbManager = mockk<RemoteDbManager>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testProject = testProjectRule.testProject

        val firebaseTestToken = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        coEvery { remoteDbManager.getCurrentToken() } returns firebaseTestToken.token
        val mockBaseUrlProvider = mockk<BaseUrlProvider>()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
        sessionRemoteDataSource = SessionRemoteDataSourceImpl(
            remoteDbManager,
            SimApiClientFactory(mockBaseUrlProvider, "some_device")
        )
    }

    @Test
    fun closeSessions_shouldGetUploaded() {
        runBlocking {
            val nSession = SessionRepositoryImpl.SESSION_BATCH_SIZE + 1
            val sessions = createClosedSessions(nSession)

            sessions.forEach {
                it.addAlertScreenEvents()
            }

            executeUpload(sessions)

            waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)

            val response = RemoteTestingManager.create().getSessionCount(testProject.id)
            assertThat(response.count).isEqualTo(nSession)
        }
    }

    @Test
    fun closeSession_withAllEvents_shouldGetUploaded() {
        runBlocking {
            val session = createClosedSessions(1).first().apply {
                addAlertScreenEvents()
                addArtificialTerminationEvent()
                addAuthenticationEvent()
                addAuthorizationEvent()
                addCandidateReadEvent()
                addConnectivitySnapshotEvent()
                addConsentEvent()
                addEnrolmentEvent()
                addFingerprintCaptureEvent()
                addGuidSelectionEvent()
                addIntentParsingEvent()
                addInvalidIntentEvent()
                addOneToOneMatchEvent()
                addOneToManyMatchEvent()
                addPersonCreationEvent()
                addRefusalEvent()
                addScannerConnectionEvent()
                addSuspiciousIntentEvent()
                addCallbackEvent()
                addCalloutEvent()
                addCompletionCheckEvent()
            }

            executeUpload(mutableListOf(session))

            waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)

            val response = RemoteTestingManager.create().getSessionCount(testProject.id)
            assertThat(response.count).isEqualTo(1)
        }
    }


    private suspend fun executeUpload(sessions: MutableList<SessionEvents>) {
        sessionRemoteDataSource.uploadSessions(testProject.id, sessions)
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, testProject.id)) }
        }

    private fun SessionEvents.addAlertScreenEvents() {
        AlertScreenEvent.AlertScreenEventType.values().forEach {
            addEvent(AlertScreenEvent(0, it))
        }
    }

    private fun SessionEvents.addArtificialTerminationEvent() {
        ArtificialTerminationEvent.Reason.values().forEach {
            addEvent(ArtificialTerminationEvent(0, it))
        }
    }

    private fun SessionEvents.addAuthenticationEvent() {
        AuthenticationEvent.Result.values().forEach {
            addEvent(AuthenticationEvent(0, 0, AuthenticationEvent.UserInfo("some_project", "user_id"), it))
        }
    }

    private fun SessionEvents.addAuthorizationEvent() {
        AuthorizationEvent.Result.values().forEach {
            addEvent(AuthorizationEvent(0, it, AuthorizationEvent.UserInfo("some_project", "user_id")))
        }
    }

    private fun SessionEvents.addCandidateReadEvent() {
        CandidateReadEvent.LocalResult.values().forEach { local ->
            CandidateReadEvent.RemoteResult.values().forEach { remote ->
                addEvent(CandidateReadEvent(0, 0, RANDOM_GUID, local, remote))
            }
        }
    }

    private fun SessionEvents.addConnectivitySnapshotEvent() {
        addEvent(ConnectivitySnapshotEvent(0, "Unknown", listOf(SimNetworkUtils.Connection("connection", NetworkInfo.DetailedState.CONNECTED))))
    }

    private fun SessionEvents.addConsentEvent() {
        ConsentEvent.Type.values().forEach { type ->
            ConsentEvent.Result.values().forEach { result ->
                addEvent(ConsentEvent(0, 0, type, result))
            }
        }
    }

    private fun SessionEvents.addEnrolmentEvent() {
        addEvent(EnrolmentEvent(0, RANDOM_GUID))
    }

    private fun SessionEvents.addFingerprintCaptureEvent() {
        FingerprintCaptureEvent.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->
                val fakeTemplate = EncodingUtils.byteArrayToBase64(
                    PeopleGeneratorUtils.getRandomFingerprintSample().template
                )

                val fingerprint = FingerprintCaptureEvent.Fingerprint(
                    fingerIdentifier,
                    0,
                    fakeTemplate
                )

                val event = FingerprintCaptureEvent(
                    0,
                    0,
                    fingerIdentifier,
                    0,
                    result,
                    fingerprint,
                    randomUUID()
                )

                addEvent(event)
            }
        }
    }

    private fun SessionEvents.addGuidSelectionEvent() {
        addEvent(GuidSelectionEvent(0, RANDOM_GUID))
    }

    private fun SessionEvents.addIntentParsingEvent() {
        IntentParsingEvent.IntegrationInfo.values().forEach {
            addEvent(IntentParsingEvent(0, it))
        }
    }

    private fun SessionEvents.addInvalidIntentEvent() {
        addEvent(InvalidIntentEvent(0, "some_action", emptyMap()))
    }

    private fun SessionEvents.addOneToManyMatchEvent() {
        OneToManyMatchEvent.MatchPoolType.values().forEach {
            addEvent(OneToManyMatchEvent(0, 0, OneToManyMatchEvent.MatchPool(it, 0), emptyList()))
        }
    }

    private fun SessionEvents.addOneToOneMatchEvent() {
        addEvent(OneToOneMatchEvent(0, 0, RANDOM_GUID, MatchEntry(RANDOM_GUID, 0F)))
    }

    private fun SessionEvents.addPersonCreationEvent() {
        addEvent(PersonCreationEvent(0, listOf(RANDOM_GUID, RANDOM_GUID)))
    }

    private fun SessionEvents.addRefusalEvent() {
        RefusalEvent.Answer.values().forEach {
            addEvent(RefusalEvent(0, 0, it, "other_text"))
        }
    }

    private fun SessionEvents.addScannerConnectionEvent() {
        addEvent(ScannerConnectionEvent(0, ScannerConnectionEvent.ScannerInfo("scanner_id", "macAddress", "hardware")))
    }

    private fun SessionEvents.addSuspiciousIntentEvent() {
        addEvent(SuspiciousIntentEvent(0, mapOf("some_extra_key" to "value")))
    }

    private fun SessionEvents.addCompletionCheckEvent() {
        addEvent(CompletionCheckEvent(0, true))
    }

    private fun SessionEvents.addCallbackEvent() {
        addEvent(EnrolmentCallbackEvent(0, RANDOM_GUID))

        ErrorCallbackEvent.Reason.values().forEach {
            addEvent(ErrorCallbackEvent(0, it))
        }

        Tier.values().forEach {
            addEvent(IdentificationCallbackEvent(0, RANDOM_GUID, listOf(CallbackComparisonScore(RANDOM_GUID, 0, it))))
        }

        addEvent(RefusalCallbackEvent(0, "reason", "other_text"))
        addEvent(VerificationCallbackEvent(0, CallbackComparisonScore(RANDOM_GUID, 0, Tier.TIER_1)))
        addEvent(ConfirmationCallbackEvent(0, true))
    }

    private fun SessionEvents.addCalloutEvent() {
        addEvent(EnrolmentCalloutEvent(1, "project_id", "user_id", "module_id", "metadata"))
        addEvent(ConfirmationCalloutEvent(10, "projectId", RANDOM_GUID, RANDOM_GUID))
        addEvent(IdentificationCalloutEvent(0, "project_id", "user_id", "module_id", "metadata"))
        addEvent(VerificationCalloutEvent(2, "project_id", "user_id", "module_id", RANDOM_GUID, "metadata"))
    }
}