package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_CONFIRM_IDENTITY
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_IDENTIFY
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_REGISTER
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_VERIFY
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_LEGACY_API_KEY
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_METADATA
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_MODULE_ID
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_PROJECT_ID
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_USER_ID
import com.simprints.clientapi.requestFactories.MockClientRequestFactory.Companion.MOCK_VERIFY_GUID
import com.simprints.clientapi.requestFactories.MockEnrollmentFactory
import com.simprints.clientapi.requestFactories.MockVerifyFactory
import com.simprints.clientapi.simprintsrequests.EnrollmentRequest
import com.simprints.clientapi.simprintsrequests.VerifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollmentRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class OdkPresenterTest {

    @Mock
    private val view: OdkContract.View = OdkActivity()

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = MockEnrollmentFactory.getMockExtractor()
        `when`(view.enrollmentExtractor).thenReturn(enrollmentExtractor)

        OdkPresenter(view, ACTION_REGISTER).apply { start() }
        Mockito.verify(view, times(1)).sendSimprintsRequest(EnrollmentRequest(
            projectId = MOCK_PROJECT_ID,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA
        ))
    }

    @Test
    fun startPresenterForLegacyRegister_ShouldRequestLegacyRegister() {
        val enrollmentExtractor = MockEnrollmentFactory.getMockExtractor(withLegacyApiKey = true)
        `when`(view.enrollmentExtractor).thenReturn(enrollmentExtractor)

        OdkPresenter(view, ACTION_REGISTER).apply { start() }
        Mockito.verify(view, times(1)).sendSimprintsRequest(LegacyEnrollmentRequest(
            legacyApiKey = MOCK_LEGACY_API_KEY,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA
        ))
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        OdkPresenter(view, ACTION_IDENTIFY).apply { start() }
        Mockito.verify(view, times(1)).requestIdentifyCallout()
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = MockVerifyFactory.getMockExtractor()
        `when`(view.verifyExtractor).thenReturn(verifyExractor)

        OdkPresenter(view, ACTION_VERIFY).apply { start() }
        Mockito.verify(view, times(1)).sendSimprintsRequest(VerifyRequest(
            projectId = MOCK_PROJECT_ID,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            verifyGuid = MOCK_VERIFY_GUID
        ))
    }

    @Test
    fun startPresenterForVerify_ShouldRequestLegacyVerify() {
        val verifyExractor = MockVerifyFactory.getMockExtractor(true)
        `when`(view.verifyExtractor).thenReturn(verifyExractor)

        OdkPresenter(view, ACTION_VERIFY).apply { start() }
        Mockito.verify(view, times(1)).sendSimprintsRequest(LegacyVerifyRequest(
            legacyApiKey = MOCK_LEGACY_API_KEY,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            verifyGuid = MOCK_VERIFY_GUID
        ))
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        OdkPresenter(view, "Garbage").apply { start() }
        Mockito.verify(view, times(1)).returnActionErrorToClient()
    }

    @Test
    fun processRegistration_ShouldReturnValidOdkRegistration() {
        val registerId = UUID.randomUUID().toString()

        OdkPresenter(view, ACTION_REGISTER).processRegistration(Registration(registerId))
        Mockito.verify(view, times(1)).returnRegistration(registerId)
    }

    @Test
    fun processIdentification_ShouldReturnValidOdkIdentification() {
        val id1 = Identification(UUID.randomUUID().toString(), 100, Tier.TIER_1)
        val id2 = Identification(UUID.randomUUID().toString(), 15, Tier.TIER_5)
        val sessionId = UUID.randomUUID().toString()

        OdkPresenter(view, ACTION_IDENTIFY).processIdentification(arrayListOf(id1, id2), sessionId)
        Mockito.verify(view, times(1)).returnIdentification(
            idList = "${id1.guid} ${id2.guid}",
            confidenceList = "${id1.confidence} ${id2.confidence}",
            tierList = "${id1.tier} ${id2.tier}",
            sessionId = sessionId
        )
    }

    @Test
    fun processVerification_ShouldReturnValidOdkVerification() {
        val verification = Verification(100, Tier.TIER_1, UUID.randomUUID().toString())

        OdkPresenter(view, ACTION_IDENTIFY).processVerification(verification)
        Mockito.verify(view, times(1)).returnVerification(
            id = verification.guid,
            confidence = verification.confidence.toString(),
            tier = verification.tier.toString()
        )
    }

    @Test
    fun processReturnError_ShouldCallActionError() {
        OdkPresenter(view, "").processReturnError()
        Mockito.verify(view, times(1)).returnActionErrorToClient()
    }

    @Test
    fun startPresenterForConfirmIdentity_ShouldRequestConfirmIdentity() {
        OdkPresenter(view, ACTION_CONFIRM_IDENTITY).apply { start() }
        Mockito.verify(view, times(1)).requestConfirmIdentityCallout()
    }

}
