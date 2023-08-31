package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrolExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType


class EnrolBuilder(
    private val extractor: EnrolExtractor,
    private val project: Project,
    private val tokenization: Tokenization,
    validator: EnrolValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? EnrolRequest) ?: return baseRequest
        val encryptedUserId =
            encryptField(request.userId, project, TokenKeyType.AttendantId, tokenization)
        val encryptedModuleId =
            encryptField(request.moduleId, project, TokenKeyType.ModuleId, tokenization)
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAppRequest(): BaseRequest = EnrolRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        metadata = extractor.getMetadata(),
        moduleId = extractor.getModuleId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
