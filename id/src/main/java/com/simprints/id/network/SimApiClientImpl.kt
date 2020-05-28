package com.simprints.id.network

import com.google.gson.Gson
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.tools.extensions.isClientAndCloudIntegrationIssue
import com.simprints.id.tools.extensions.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.KClass

open class SimApiClientImpl<T : SimRemoteInterface>(private val service: KClass<T>,
                                                    private val url: String,
                                                    private val deviceId: String,
                                                    private val authToken: String? = null,
                                                    private val jsonAdapter: Gson = JsonHelper.gson) : SimApiClient<T> {

    override val api: T by lazy {
        retrofit.create(service.java)
    }

    open val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(jsonAdapter))
            .baseUrl(url)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        DefaultOkHttpClientBuilder().get(authToken, deviceId)
    }

    override suspend fun <V> executeCall(traceName: String?,
                                         networkBlock: suspend (T) -> V): V {

        val trace = if (traceName != null) {
            trace(traceName)
        } else null

        trace?.start()

        return retryIO(
            times = NetworkConstants.RETRY_ATTEMPTS_FOR_NETWORK_CALLS,
            runBlock = {
                return@retryIO try {

                    withContext(Dispatchers.IO) {
                        networkBlock(api)
                    }.also { trace?.stop() }
                } catch (throwable: Throwable) {
                    throw if (throwable.isClientAndCloudIntegrationIssue()) {
                        SyncCloudIntegrationException("Http status code not worth to retry", throwable)
                    } else {
                        throwable
                    }
                }
            },
            retryIf = { it !is SyncCloudIntegrationException })
    }

}