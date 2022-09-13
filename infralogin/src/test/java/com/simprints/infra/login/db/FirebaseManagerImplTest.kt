package com.simprints.infra.login.db

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirebaseManagerImplTest {

    companion object {
        private const val GCP_PROJECT_ID = "GCP_PROJECT_ID"
        private const val API_KEY = "API_KEY"
        private const val APPLICATION_ID = "APPLICATION_ID"
    }

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val firebaseApp = mockk<FirebaseApp>(relaxed = true)
    private val loginInfoManager = mockk<LoginInfoManager>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val firebaseOptionsBuilder = mockk<FirebaseOptions.Builder>(relaxed = true)
    private val firebaseManagerImpl = FirebaseManagerImpl(loginInfoManager, context)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseApp::class)
        mockkStatic(Firebase::class)
        mockkStatic(FirebaseOptions.Builder::class)
        every { FirebaseApp.getInstance(any()) } returns firebaseApp
        every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
        every { firebaseOptionsBuilder.setApiKey(any()) } returns firebaseOptionsBuilder
        every { firebaseOptionsBuilder.setProjectId(any()) } returns firebaseOptionsBuilder
        every { firebaseOptionsBuilder.setApplicationId(any()) } returns firebaseOptionsBuilder

    }

    @Test
    fun `signIn should throw a NetworkConnectionException if Firebase throws FirebaseNetworkException`() =
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.signInWithCustomToken(any()) } throws FirebaseNetworkException("")

            assertThrows<NetworkConnectionException> {
                firebaseManagerImpl.signIn(mockk(relaxed = true))
            }
        }

    @Test
    fun `signIn should throw a NetworkConnectionException if Firebase throws ApiException`() =
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.signInWithCustomToken(any()) } throws ApiException(Status.RESULT_TIMEOUT)

            assertThrows<NetworkConnectionException> {
                firebaseManagerImpl.signIn(mockk(relaxed = true))
            }
        }

    @Test
    fun `signOut should succeed`() = runTest(UnconfinedTestDispatcher()) {
        firebaseManagerImpl.signOut()

        verify(exactly = 1) { firebaseAuth.signOut() }
        verify(exactly = 1) { firebaseApp.delete() }
        verify(exactly = 1) { loginInfoManager.clearCachedTokenClaims() }
    }

    @Test
    fun `signOut should throw a NetworkConnectionException if Firebase throws FirebaseNetworkException`() =
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.signOut() } throws FirebaseNetworkException("")

            assertThrows<NetworkConnectionException> {
                firebaseManagerImpl.signOut()
            }
        }

    @Test
    fun `signOut should throw a NetworkConnectionException if Firebase throws ApiException`() =
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.signOut() } throws ApiException(Status.RESULT_TIMEOUT)

            assertThrows<NetworkConnectionException> {
                firebaseManagerImpl.signOut()
            }
        }

    @Test
    fun `isSignedIn should return true if the project id claim is null`() {
        every { loginInfoManager.projectIdTokenClaim } returns null
        assertThat(firebaseManagerImpl.isSignedIn("", "")).isTrue()
    }

    @Test
    fun `isSignedIn should return true if the project id claim is the same as the project id`() {
        every { loginInfoManager.projectIdTokenClaim } returns "project id"
        assertThat(firebaseManagerImpl.isSignedIn("project id", "")).isTrue()
    }

    @Test
    fun `isSignedIn should return true if the project id claim is not the same as the project id`() {
        every { loginInfoManager.projectIdTokenClaim } returns "project id"
        assertThat(firebaseManagerImpl.isSignedIn("another project id", "")).isFalse()
    }

    @Test
    fun `getCurrentToken throws NetworkConnectionException if Firebase throws FirebaseNetworkException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.getAccessToken(any()) } throws FirebaseNetworkException("")
            assertThrows<NetworkConnectionException> { firebaseManagerImpl.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken throws NetworkConnectionException if Firebase throws ApiException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.getAccessToken(any()) } throws ApiException(Status.RESULT_TIMEOUT)
            assertThrows<NetworkConnectionException> { firebaseManagerImpl.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken throws RemoteDbNotSignedInException if FirebaseNoSignedInUserException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.getAccessToken(any()) } throws FirebaseNoSignedInUserException("")
            assertThrows<RemoteDbNotSignedInException> { firebaseManagerImpl.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken success`() = runBlocking {
        every {
            firebaseAuth.getAccessToken(any())
        } returns Tasks.forResult(GetTokenResult("Token", HashMap()))

        val result = firebaseManagerImpl.getCurrentToken()
        assertThat(result).isEqualTo("Token")
    }

    @Test
    fun `getCoreApp should init the app if the Firebase getInstance() throws an IllegalStateException`() {
        every { FirebaseApp.getInstance(any()) } throws IllegalStateException() andThenThrows IllegalStateException() andThen firebaseApp
        every { loginInfoManager.coreFirebaseProjectId } returns GCP_PROJECT_ID
        every { loginInfoManager.coreFirebaseApplicationId } returns APPLICATION_ID
        every { loginInfoManager.coreFirebaseApiKey } returns API_KEY
        every { Firebase.initialize(any(), any(), any()) } returns mockk()

        firebaseManagerImpl.getCoreApp()

        verify(exactly = 1) {
            Firebase.initialize(any(), match {
                it.apiKey == API_KEY && it.applicationId == APPLICATION_ID && it.projectId == GCP_PROJECT_ID
            }, any())
        }
    }

    @Test
    fun `getCoreApp should throw an IllegalStateException the app if the Firebase getInstance() throws an IllegalStateException and the coreFirebaseProjectId is empty`() {
        every { FirebaseApp.getInstance(any()) } throws IllegalStateException() andThenThrows IllegalStateException() andThen firebaseApp
        every { loginInfoManager.coreFirebaseProjectId } returns ""
        every { loginInfoManager.coreFirebaseApplicationId } returns APPLICATION_ID
        every { loginInfoManager.coreFirebaseApiKey } returns API_KEY
        every { Firebase.initialize(any(), any(), any()) } returns mockk()

        assertThrows<IllegalStateException> { firebaseManagerImpl.getCoreApp() }
    }

    @Test
    fun `getCoreApp should init the app and recreate the app if the Firebase getInstance() throws an IllegalStateException and the initialization failed`() {
        every { FirebaseApp.getInstance(any()) } throws IllegalStateException() andThenThrows IllegalStateException() andThen firebaseApp
        every { loginInfoManager.coreFirebaseProjectId } returns GCP_PROJECT_ID
        every { loginInfoManager.coreFirebaseApplicationId } returns APPLICATION_ID
        every { loginInfoManager.coreFirebaseApiKey } returns API_KEY
        every {
            Firebase.initialize(
                any(),
                any(),
                any()
            )
        } throws IllegalStateException() andThen mockk<FirebaseApp>()

        firebaseManagerImpl.getCoreApp()

        verify(exactly = 1) { firebaseApp.delete() }
        verify(exactly = 2) {
            Firebase.initialize(any(), match {
                it.apiKey == API_KEY && it.applicationId == APPLICATION_ID && it.projectId == GCP_PROJECT_ID
            }, any())
        }
    }
}