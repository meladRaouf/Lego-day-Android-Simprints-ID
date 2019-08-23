package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.matching.request.MatchingTaskIdentifyRequest
import com.simprints.fingerprint.activities.matching.result.MatchingTaskIdentifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.MatchGroup
import com.simprints.fingerprint.data.domain.toAction
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask

class IdentifyTaskFlow : FingerprintTaskFlow() {

    override fun computeFlow(fingerprintRequest: FingerprintRequest) {
        with(fingerprintRequest as FingerprintIdentifyRequest) {
            fingerprintTasks = listOf(
                FingerprintTask.Launch(LAUNCH) { createLaunchTaskRequest() },
                FingerprintTask.CollectFingerprints(COLLECT) { createCollectFingerprintsTaskRequest() },
                FingerprintTask.Matching(MATCHING) { createMatchingTaskRequest() }
            )
        }
    }

    private fun FingerprintIdentifyRequest.createLaunchTaskRequest() =
        LaunchTaskRequest(
            projectId, this.toAction(), language, logoExists, programName, organizationName
        )

    private fun FingerprintIdentifyRequest.createCollectFingerprintsTaskRequest() =
        CollectFingerprintsTaskRequest(
            projectId, userId, moduleId, this.toAction(), language, fingerStatus
        )

    private fun FingerprintIdentifyRequest.createMatchingTaskRequest() =
        with(taskResults[COLLECT] as CollectFingerprintsTaskResult) {
            MatchingTaskIdentifyRequest(
                language, probe, buildQueryForIdentifyPool(), returnIdCount
            )
        }

    private fun FingerprintIdentifyRequest.buildQueryForIdentifyPool() =
        when (matchGroup) {
            MatchGroup.GLOBAL -> MatchingTaskIdentifyRequest.QueryForIdentifyPool(projectId)
            MatchGroup.USER -> MatchingTaskIdentifyRequest.QueryForIdentifyPool(projectId, userId = userId)
            MatchGroup.MODULE -> MatchingTaskIdentifyRequest.QueryForIdentifyPool(projectId, moduleId = moduleId)
        }

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createIdentifyResult(taskResults[MATCHING] as MatchingTaskIdentifyResult)

    companion object {
        private const val LAUNCH = "launch"
        private const val COLLECT = "collect"
        private const val MATCHING = "matching"
    }
}
