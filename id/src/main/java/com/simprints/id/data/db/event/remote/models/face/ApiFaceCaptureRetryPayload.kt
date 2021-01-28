package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureRetryEvent.FaceCaptureRetryPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FaceCaptureRetry

@Keep
data class ApiFaceCaptureRetryPayload(override val startTime: Long, //Not added on API yet
                                      val endTime: Long,
                                      override val version: Int) : ApiEventPayload(FaceCaptureRetry, version, startTime) {

    constructor(domainPayload: FaceCaptureRetryPayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}