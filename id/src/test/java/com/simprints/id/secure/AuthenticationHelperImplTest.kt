package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.exceptions.safe.BackendMaintenanceException
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthenticationHelperImplTest {

    private lateinit var authenticationHelperImpl: AuthenticationHelperImpl
    private val loginInfoManager: LoginInfoManager = mockk(relaxed = true)
    private val timeHelper: TimeHelper = mockk(relaxed = true)
    private val projectAuthenticator: ProjectAuthenticator = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        authenticationHelperImpl =
            AuthenticationHelperImpl(loginInfoManager, timeHelper, projectAuthenticator, eventRepository)
    }

    @Test
    fun shouldSetBackendErrorIfBackendMaintenanceException() = runBlocking {
        val result = mockException(BackendMaintenanceException())

        assertThat(result).isInstanceOf(Result.BackendMaintenanceError::class.java)
        assertThat((result as Result.BackendMaintenanceError).estimatedOutage).isNull()
    }

    @Test
    fun shouldSetOfflineIfIOException() = runBlocking {
        val result = mockException(IOException())

        assertThat(result).isInstanceOf(Result.Offline::class.java)
    }

    @Test
    fun shouldSetSafetyNetUnavailableIfServiceUnavailableException() = runBlocking {
        val result = mockException(SafetyNetException(reason = SafetyNetExceptionReason.SERVICE_UNAVAILABLE))

        assertThat(result).isInstanceOf(Result.SafetyNetUnavailable::class.java)
    }

    @Test
    fun shouldSetSafetyNetInvalidIfSafetyNextInvalidException() = runBlocking {
        val result = mockException(SafetyNetException(reason = SafetyNetExceptionReason.INVALID_CLAIMS))

        assertThat(result).isInstanceOf(Result.SafetyNetInvalidClaim::class.java)
    }

    @Test
    fun shouldSetUnknownIfGenericException() = runBlocking {
        val result = mockException(Exception())

        assertThat(result).isInstanceOf(Result.Unknown::class.java)
    }

    @Test
    fun shouldTechnicalFailureIfSimprintsInternalServerException() = runBlocking {
        val result = mockException(SimprintsInternalServerException())

        assertThat(result).isInstanceOf(Result.TechnicalFailure::class.java)
    }

    @Test
    fun shouldBadCredentialsIfAuthRequestInvalidCredentialsException() = runBlocking {
        val result = mockException(AuthRequestInvalidCredentialsException())

        assertThat(result).isInstanceOf(Result.BadCredentials::class.java)
    }

    private suspend fun mockException(exception: Exception): Result {
        coEvery { projectAuthenticator.authenticate(any(), "", "") } throws exception

        return authenticationHelperImpl.authenticateSafely("", "", "", "")
    }
}
