package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.TIMED_OUT

@Keep
internal data class ApiArtificialTerminationPayload(
    override val startTime: Long,
    override val version: Int,
    val reason: ApiReason,
) : ApiEventPayload(ApiEventPayloadType.ArtificialTermination, version, startTime) {

    constructor(domainPayload: ArtificialTerminationPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.reason.fromDomainToApi())

    @Keep
    enum class ApiReason {
        TIMED_OUT, NEW_SESSION
    }
}

internal fun ArtificialTerminationPayload.Reason.fromDomainToApi() =
    when (this) {
        TIMED_OUT -> ApiArtificialTerminationPayload.ApiReason.TIMED_OUT
        NEW_SESSION -> ApiArtificialTerminationPayload.ApiReason.NEW_SESSION
    }
