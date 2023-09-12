package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrolBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrolExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.tokenization.TokenizationManager
import io.mockk.mockk

object EnrolRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        EnrolRequest(
            projectId = MOCK_PROJECT_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            moduleId = MOCK_MODULE_ID,
            unknownExtras = emptyMap()
        )

    override fun getBuilder(extractor: ClientRequestExtractor): EnrolBuilder {
        val project = mockk<Project>()
        val tokenizationManager = mockk<TokenizationManager>()
        return EnrolBuilder(
            extractor = extractor as EnrolExtractor,
            project = project,
            tokenizationManager = tokenizationManager,
            validator = getValidator(extractor)
        )
    }

    override fun getValidator(extractor: ClientRequestExtractor): EnrolValidator =
        EnrolValidator(extractor as EnrolExtractor)

    override fun getMockExtractor(): EnrolExtractor {
        val mockEnrolmentExtractor = mockk<EnrolExtractor>()
        setMockDefaultExtractor(mockEnrolmentExtractor)
        return mockEnrolmentExtractor
    }
}
