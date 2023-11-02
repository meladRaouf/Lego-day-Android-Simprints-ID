package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.core.domain.common.FlowProvider
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery

/**
 * It creates a Step to launch (used to launch a specific Activity) to execute
 * a particular task in the FingerprintModule
 */
interface FingerprintStepProcessor {


    suspend fun buildStepToCapture(): Step

    fun buildStepToMatch(
        probeSamples: List<FingerprintCaptureSample>,
        query: SubjectQuery,
        flowType: FlowProvider.FlowType,
    ): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?

}
