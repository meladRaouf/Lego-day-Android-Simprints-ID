package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.Finger
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import org.junit.Before
import org.junit.Test
import java.io.Serializable

internal class MapStepsForLastBiometricEnrolUseCaseTest {

    private lateinit var useCase: MapStepsForLastBiometricEnrolUseCase

    @Before
    fun setUp() {
        useCase = MapStepsForLastBiometricEnrolUseCase()
    }

    @Test
    fun `maps EnrolLastBiometricRequest correctly`() {
        val result = useCase(listOf(
            EnrolLastBiometricResult("subjectId")
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"))
    }


    @Test
    fun `maps FaceMatchResult correctly`() {
        val result = useCase(listOf(
            FaceMatchResult(emptyList())
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceMatchResult(emptyList()))
    }

    @Test
    fun `maps FaceCaptureResult correctly`() {
        val result = useCase(listOf(
            FaceCaptureResult(
                results = listOf(
                    FaceCaptureResult.Item(captureEventId = null, index = 0, sample = null),
                    FaceCaptureResult.Item(
                        captureEventId = GUID1,
                        index = 0,
                        sample = FaceCaptureResult.Sample(
                            faceId = "faceId",
                            template = byteArrayOf(),
                            imageRef = null,
                            format = "format"
                        )
                    )
                ),
            )
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceCaptureResult(listOf(
            FaceTemplateCaptureResult(
                template = byteArrayOf(),
                format = "format",
            )
        )))
    }

    @Test
    fun `maps FingerprintMatchResult correctly`() {
        val result = useCase(listOf(
            FingerprintMatchResult(emptyList())
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList()))
    }

    @Test
    fun `maps FingerprintCaptureResult correctly`() {
        val result = useCase(listOf(
            FingerprintCaptureResult(
                results = listOf(
                    FingerprintCaptureResult.Item(null, IFingerIdentifier.LEFT_THUMB, null),
                    FingerprintCaptureResult.Item(
                        identifier = IFingerIdentifier.RIGHT_THUMB,
                        captureEventId = GUID1,
                        sample = FingerprintCaptureResult.Sample(
                            fingerIdentifier = IFingerIdentifier.RIGHT_THUMB,
                            template = byteArrayOf(),
                            templateQualityScore = 0,
                            imageRef = null,
                            format = "format"
                        )
                    )
                ),
            )
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FingerprintCaptureResult(listOf(
            FingerTemplateCaptureResult(
                template = byteArrayOf(),
                templateQualityScore = 0,
                format = "format",
                finger = Finger.RIGHT_THUMB
            )
        )))
    }
}
