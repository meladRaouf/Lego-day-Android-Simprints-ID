package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.simprints.matcher.FaceMatchResult
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.orchestrator.model.responses.AppVerifyResponse
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.matcher.FingerprintMatchResult
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class CreateVerifyResponseUseCaseTest {

    private lateinit var useCase: CreateVerifyResponseUseCase

    @Before
    fun setUp() {
        useCase = CreateVerifyResponseUseCase()
    }

    @Test
    fun `Returns error if no decision policy`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns null
                every { fingerprint?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(10f, 20f, 30f))
        )

        assertThat(result).isInstanceOf(AppErrorResponse::class.java)
    }

    @Test
    fun `Returns face identifications with highest score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { fingerprint?.decisionPolicy } returns null
            },
            results = listOf(createFaceMatchResult(10f, 50f, 100f))
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(100)
    }

    @Test
    fun `Returns fingerprint identifications with highest score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns null
                every { fingerprint?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
            },
            results = listOf(createFingerprintMatchResult(10f, 50f, 100f))
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(100)
    }

    @Test
    fun `Returns identifications with highest face match score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { fingerprint?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
            },
            results = listOf(
                createFaceMatchResult(10f, 50f, 105f),
                createFingerprintMatchResult(10f, 50f, 100f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(105)
    }


    @Test
    fun `Returns identifications with highest fingerprint match score`() {
        val result = useCase(
            mockk {
                every { face?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
                every { fingerprint?.decisionPolicy } returns DecisionPolicy(10, 20, 30)
            },
            results = listOf(
                createFaceMatchResult(10f, 50f, 100f),
                createFingerprintMatchResult(10f, 50f, 105f),
            )
        )

        assertThat((result as AppVerifyResponse).matchResult.confidenceScore).isEqualTo(105)
    }

    private fun createFingerprintMatchResult(vararg confidences: Float): Parcelable = FingerprintMatchResult(
        confidences.map { FingerprintMatchResult.Item(personId = "1", confidenceScore = it) }
    )

    private fun createFaceMatchResult(vararg confidences: Float): Parcelable = FaceMatchResult(
        confidences.map { FaceMatchResult.Item(guid = "1", confidence = it) }
    )
}