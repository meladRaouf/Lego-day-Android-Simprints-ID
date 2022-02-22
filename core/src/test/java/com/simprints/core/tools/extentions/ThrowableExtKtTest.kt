package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ThrowableExtKtTest {

    @Test
    fun gettingNoBackendMaintenanceErrorReturnsFalse() {
        assertThat(Throwable().isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingBackendMaintenanceErrorReturnsTrue() {
        val errorResponse =
            "{\"error\":\"002\"}"
        val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
        val mockResponse = Response.error<Any>(503, errorResponseBody)
        val throwable = HttpException(mockResponse)

        assertThat(throwable.isBackendMaintenanceException()).isTrue()
    }
}
