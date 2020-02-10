package com.simprints.id.data.db.project.remote

import com.google.gson.JsonElement
import com.simprints.core.network.SimApiClient
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.tools.utils.retrySimNetworkCalls


open class ProjectRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager) : ProjectRemoteDataSource {

    override suspend fun loadProjectFromRemote(projectId: String): Project =
        makeNetworkRequest({
            it.requestProject(projectId)
        }, "requestProject")

    override suspend fun loadProjectRemoteConfigSettingsJsonString(projectId: String): JsonElement =
        makeNetworkRequest({
            it.requestProjectConfig(projectId)
        }, "requestProjectConfig")

    override suspend fun getProjectApiClient(): ProjectRemoteInterface =
       buildProjectApi(remoteDbManager.getCurrentToken())

    private suspend fun <T> makeNetworkRequest(block: suspend (client: ProjectRemoteInterface) -> T, traceName: String): T =
        retrySimNetworkCalls(getProjectApiClient(), block, traceName)

    private fun buildProjectApi(authToken: String): ProjectRemoteInterface =
        SimApiClient(ProjectRemoteInterface::class.java, ProjectRemoteInterface.baseUrl, authToken).api
}
