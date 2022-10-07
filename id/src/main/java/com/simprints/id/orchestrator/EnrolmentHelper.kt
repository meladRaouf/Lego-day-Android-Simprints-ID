package com.simprints.id.orchestrator

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.infra.enrolment.records.domain.models.Subject

interface EnrolmentHelper {

    fun buildSubject(projectId: String,
                     userId: String,
                     moduleId: String,
                     fingerprintResponse: FingerprintCaptureResponse?,
                     faceResponse: FaceCaptureResponse?,
                     timeHelper: TimeHelper
    ): Subject

    suspend fun enrol(subject: Subject)
}
