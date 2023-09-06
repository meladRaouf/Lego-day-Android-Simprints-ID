package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.builders.VerifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.VerifyValidator
import io.mockk.every
import io.mockk.mockk

internal object VerifyActionFactory : RequestActionFactory() {

    override fun getValidSimprintsRequest() = ActionRequest.VerifyActionRequest(
        packageName = MOCK_PACKAGE,
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA,
        verifyGuid = MOCK_VERIFY_GUID,
        unknownExtras = emptyMap()
    )

    override fun getBuilder(extractor: ActionRequestExtractor): VerifyRequestBuilder =
        VerifyRequestBuilder(MOCK_PACKAGE, extractor as VerifyRequestExtractor, getValidator(extractor))

    override fun getValidator(extractor: ActionRequestExtractor): VerifyValidator =
        VerifyValidator(extractor as VerifyRequestExtractor)

    override fun getMockExtractor(): VerifyRequestExtractor {
        val mockVerifyExtractor = mockk<VerifyRequestExtractor>()
        setMockDefaultExtractor(mockVerifyExtractor)
        every { mockVerifyExtractor.getVerifyGuid() } returns MOCK_VERIFY_GUID
        return mockVerifyExtractor
    }

}