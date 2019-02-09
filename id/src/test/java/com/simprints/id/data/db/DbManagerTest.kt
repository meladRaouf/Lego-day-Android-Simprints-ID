package com.simprints.id.data.db

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.commontesttools.createMockBehaviorService
import com.simprints.id.commontesttools.di.DependencyRule.*
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toFirebasePerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.sync.SimApiMock
import com.simprints.id.testtools.di.AppModuleForTests
import com.simprints.id.testtools.retrofit.mockServer.mockNotFoundResponse
import com.simprints.id.testtools.retrofit.mockServer.mockResponseForDownloadPatient
import com.simprints.id.testtools.retrofit.mockServer.mockResponseForUploadPatient
import com.simprints.id.testtools.retrofit.mockServer.mockServerProblemResponse
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.roboletric.RobolectricTestMocker.setupLocalAndRemoteManagersForApiTesting
import com.simprints.id.testtools.roboletric.TestApplication
import com.simprints.libcommon.Person
import com.simprints.testframework.common.syntax.whenever
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DbManagerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>

    @Inject lateinit var localDbManagerSpy: LocalDbManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
    @Inject lateinit var remotePeopleManagerSpy: RemotePeopleManager
    @Inject lateinit var sessionEventsLocalDbManagerSpy: SessionEventsLocalDbManager
    @Inject lateinit var peopleUpSyncMasterMock: PeopleUpSyncMaster
    @Inject lateinit var dbManager: DbManager

    private val module by lazy {
        AppModuleForTests(
            app,
            localDbManagerRule = ReplaceRule { spy(LocalDbManager::class.java) },
            remoteDbManagerRule = SpyRule,
            remotePeopleManagerRule = SpyRule,
            peopleUpSyncMasterRule = MockRule,
            sessionEventsLocalDbManagerRule = MockRule
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        mockServer.start()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)

        setupLocalAndRemoteManagersForApiTesting(localDbManagerSpy, remoteDbManagerSpy, sessionEventsLocalDbManagerSpy, mockServer)
    }

    @Test
    fun savingPerson_shouldSaveThenScheduleUpSync() {
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().toRealmPerson().apply {
            updatedAt = null
            createdAt = null
        })

        mockServer.enqueue(mockResponseForUploadPatient())
        mockServer.enqueue(mockResponseForDownloadPatient(fakePerson.copy().apply {
            updatedAt = Date(1)
            createdAt = Date(0)
        }))

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable
            .assertNoErrors()
            .assertComplete()

        // savePerson makes an async task in the OnComplete, we need to wait it finishes.
        Thread.sleep(1000)

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManagerSpy, times(1)).insertOrUpdatePersonInLocal(argument.capture())

        // First time we save the person in the local dbManager, it doesn't have times and it needs to be sync
        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)

        verify(peopleUpSyncMasterMock).schedule(fakePerson.projectId/*, fakePerson.userId*/) // TODO: uncomment userId when multitenancy is properly implemented
    }

    @Test
    fun loadingPersonMissingInLocalDb_shouldStillLoadFromRemoteDb() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockResponseForDownloadPatient(person.toFirebasePerson()))

        val result = mutableListOf<Person>()

        val futureResultIsNotEmpty = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess(isDataFromRemote: Boolean) {
                futureResultIsNotEmpty.complete(result.isEmpty())
            }

            override fun onFailure(data_error: DATA_ERROR) {
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futureResultIsNotEmpty.get())
        verify(remotePeopleManagerSpy, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @Test
    fun savingPerson_serverProblemStillSavesPerson() {
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().toRealmPerson().apply {
            updatedAt = null
            createdAt = null
        })

        for (i in 0..20) mockServer.enqueue(mockServerProblemResponse())

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManagerSpy, times(1)).insertOrUpdatePersonInLocal(argument.capture())

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @Test
    fun savingPerson_noConnectionStillSavesPerson() {
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson().toRealmPerson().apply {
            updatedAt = null
            createdAt = null
        })

        val poorNetworkClientMock: PeopleRemoteInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 100, PeopleRemoteInterface::class.java))
        whenever(remotePeopleManagerSpy.getPeopleApiClient()).thenReturn(Single.just(poorNetworkClientMock))

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable.assertNoErrors()

        val argument = argumentCaptor<rl_Person>()
        verify(localDbManagerSpy, times(1)).insertOrUpdatePersonInLocal(argument.capture())

        Assert.assertNull(argument.firstValue.createdAt)
        Assert.assertNull(argument.firstValue.updatedAt)
        Assert.assertTrue(argument.firstValue.toSync)
    }

    @Test
    fun loadingPersonMissingInLocalAndRemoteDbs_shouldTriggerDataError() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        mockServer.enqueue(mockNotFoundResponse())

        val result = mutableListOf<Person>()

        val futurePersonExists = CompletableFuture<Boolean>()
        val futureDataErrorExistsAndIsPersonNotFound = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess(isDataFromRemote: Boolean) {
                futurePersonExists.complete(true)
            }

            override fun onFailure(data_error: DATA_ERROR) {
                futurePersonExists.complete(false)
                futureDataErrorExistsAndIsPersonNotFound.complete(data_error == DATA_ERROR.NOT_FOUND)
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futurePersonExists.get())
        Assert.assertTrue(futureDataErrorExistsAndIsPersonNotFound.get())
        verify(remotePeopleManagerSpy, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @Test
    fun loadingPersonMissingInLocalAndWithNoConnection_shouldTriggerDataError() {
        val person = PeopleGeneratorUtils.getRandomPerson()

        val poorNetworkClientMock: PeopleRemoteInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 100, PeopleRemoteInterface::class.java))
        whenever(remotePeopleManagerSpy.getPeopleApiClient()).thenReturn(Single.just(poorNetworkClientMock))

        val result = mutableListOf<Person>()

        val futurePersonExists = CompletableFuture<Boolean>()
        val futureDataErrorExistsAndIsPersonNotFound = CompletableFuture<Boolean>()
        val callback = object : DataCallback {
            override fun onSuccess(isDataFromRemote: Boolean) {
                futurePersonExists.complete(true)
            }

            override fun onFailure(data_error: DATA_ERROR) {
                futurePersonExists.complete(false)
                futureDataErrorExistsAndIsPersonNotFound.complete(data_error == DATA_ERROR.NOT_FOUND)
            }
        }

        dbManager.loadPerson(result, person.projectId, person.patientId, callback = callback)

        Assert.assertFalse(futurePersonExists.get())
        Assert.assertTrue(futureDataErrorExistsAndIsPersonNotFound.get())
        verify(remotePeopleManagerSpy, times(1)).downloadPerson(person.patientId, person.projectId)
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
