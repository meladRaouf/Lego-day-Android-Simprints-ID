package com.simprints.id.network

import android.content.Context
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.SimApiClientImpl
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

// TODO move this into the infralogin
class SimApiClientFactoryImpl(
    val baseUrlProvider: BaseUrlProvider,
    val deviceId: String,
    private val ctx: Context,
    private val versionName: String,
    private val remoteDbManager: RemoteDbManager,
) : SimApiClientFactory {

    // Not using `inline fun <reified T : SimRemoteInterface>` because it's not possible to
    // create an interface for that or mock it. SimApiClientFactory is injected everywhere, so it's important
    // that we are able to mock it.
    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            ctx,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            versionName,
            remoteDbManager.getCurrentToken(),
        )
    }

    override fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            ctx,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            versionName,
            null,
        )
    }
}
