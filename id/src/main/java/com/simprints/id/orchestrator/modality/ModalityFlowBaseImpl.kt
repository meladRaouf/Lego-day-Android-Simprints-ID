package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFaceExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor,
                                    private val fingerprintStepProcessor: FingerprintStepProcessor,
                                    private val faceStepProcessor: FaceStepProcessor,
                                    private val sessionEventsManager: SessionEventsManager): ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        when (appRequest.type) {
            AppRequestType.ENROL -> steps.add(buildConsentStep(ConsentType.ENROL))
            AppRequestType.IDENTIFY -> steps.add(buildConsentStep(ConsentType.IDENTIFY))
            AppRequestType.VERIFY -> {
                addVerifyStep(appRequest)
                steps.add(buildConsentStep(ConsentType.VERIFY))
            }
        }
    }

    private fun addVerifyStep(appRequest: AppRequest) {
        with(appRequest as AppVerifyRequest) {
            steps.add(buildVerifyCoreStep(projectId, verifyGuid))
        }
    }

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    private fun buildConsentStep(consentType: ConsentType) =
        coreStepProcessor.buildStepConsent(consentType)

    private fun buildVerifyCoreStep(projectId: String, verifyGuid: String) =
        coreStepProcessor.buildStepVerify(projectId, verifyGuid)

    fun completeAllStepsIfExitFormHappened(requestCode: Int, resultCode: Int, data: Intent?) =
        tryProcessingResultFromCoreStepProcessor(data)
            ?: tryProcessingResultFromFingerprintStepProcessor(requestCode, resultCode, data)

    private fun tryProcessingResultFromCoreStepProcessor(data: Intent?) =
        coreStepProcessor.processResult(data).also { coreResult ->
            if (isExitFormResponse(coreResult)) {
                completeAllSteps()
            }
        }

    private fun isExitFormResponse(coreResult: Step.Result?) =
        coreResult is CoreExitFormResponse ||
            coreResult is CoreFingerprintExitFormResponse ||
            coreResult is CoreFaceExitFormResponse

    private fun tryProcessingResultFromFingerprintStepProcessor(requestCode: Int,
                                                                resultCode: Int,
                                                                data: Intent?) =
        fingerprintStepProcessor.processResult(requestCode, resultCode, data).also { fingerResult ->
            if (fingerResult is FingerprintRefusalFormResponse) {
                completeAllSteps()
            }
        }

    private fun completeAllSteps() {
        steps.forEach { it.setStatus(Step.Status.COMPLETED) }
    }

    fun extractFingerprintAndAddPersonCreationEvent(fingerprintCaptureResponse: FingerprintCaptureResponse) {
        val fingerprintSamples = extractFingerprintSamples(fingerprintCaptureResponse)
        addPersonCreationEventForFingerprintSamples(fingerprintSamples)
    }

    private fun extractFingerprintSamples(result: FingerprintCaptureResponse) =
        result.captureResult.mapNotNull { captureResult ->
            val fingerId = captureResult.identifier
            captureResult.sample?.let { sample ->
                FingerprintSample(fingerId, sample.template, sample.templateQualityScore)
            }
        }

    private fun addPersonCreationEventForFingerprintSamples(fingerprintSamples: List<FingerprintSample>) {
        sessionEventsManager.addPersonCreationEventInBackground(fingerprintSamples)
    }
}