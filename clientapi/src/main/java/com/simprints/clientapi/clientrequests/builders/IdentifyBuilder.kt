package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.clientapi.domain.requests.IntegrationInfo


class IdentifyBuilder(val extractor: IdentifyExtractor,
                      validator: IdentifyValidator,
                      private val integrationInfo: IntegrationInfo) :
    ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = IdentifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata(),
        unknownExtras = extractor.getUnknownExtras(),
        extra = ExtraRequestInfo(integrationInfo)
    )
}
