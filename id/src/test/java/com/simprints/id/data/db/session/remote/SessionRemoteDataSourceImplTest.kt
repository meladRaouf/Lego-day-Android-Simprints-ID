package com.simprints.id.data.db.session.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.data.db.session.remote.session.ApiSessionEvents
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.mockserver.mockSuccessfulResponse
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionRemoteDataSourceImplTest {

    private val timeHelper: TimeHelper = TimeHelperImpl()
    private val mockServer = MockWebServer()
    private val mockBaseUrlProvider: BaseUrlProvider = mockk()

    private val sessionRemoteDataSourceSpy = spyk(buildRemoteDataSource())

    private lateinit var sessionsRemoteInterface: SessionsRemoteInterface

    @Before
    @ExperimentalCoroutinesApi
    fun setUp() {
        UnitTestConfig(this).setupFirebase()

        mockServer.start()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns mockServer.url("/").toString()
    }

    @Test
    fun successfulResponseOnUpload() {
        runBlocking {
            sessionsRemoteInterface = SimApiClientFactory(
                mockBaseUrlProvider,
                "deviceId"
            ).build<SessionsRemoteInterface>().api
            coEvery { sessionRemoteDataSourceSpy.getSessionsApiClient() } returns sessionsRemoteInterface
            mockServer.enqueue(mockSuccessfulResponse())

            val sessions = listOf(
                createFakeClosedSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )

            sessionRemoteDataSourceSpy.uploadSessions("projectId", sessions)
            assertThat(mockServer.requestCount).isEqualTo(1)
        }
    }

    @Test
    fun failedResponseThenSuccessfulResponse_shouldTryAgainAndSucceed() {
        runBlocking {
            val sessionsRemoteInterface = buildSessionApiToThrowFirstThenCall()
            coEvery { sessionRemoteDataSourceSpy.getSessionsApiClient() } returns sessionsRemoteInterface
            mockServer.enqueue(mockSuccessfulResponse())

            val sessions = listOf(
                createFakeClosedSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )
            sessionRemoteDataSourceSpy.uploadSessions("projectId", sessions)

            assertThat(mockServer.requestCount).isEqualTo(1)
            coVerify(exactly = 2) { sessionsRemoteInterface.uploadSessions(any(), any()) }
        }
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }

    private fun buildRemoteDataSource() =
        SessionRemoteDataSourceImpl(mockk(), mockk())

    @Suppress("UNCHECKED_CAST")
    private fun buildSessionApiToThrowFirstThenCall(): SessionsRemoteInterface {
        val api = SimApiClientFactory(
            mockBaseUrlProvider,
            "deviceId"
        ).build<SessionsRemoteInterface>().api
        return mockk {
            coEvery { uploadSessions(any(), any()) } throws Throwable("Network issue") coAndThen {
                api.uploadSessions(
                    args[0] as String,
                    args[1] as HashMap<String, Array<ApiSessionEvents>>
                )
            }
        }
    }
}