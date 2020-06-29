package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.OneToOneMatchEvent
import com.simprints.id.data.db.event.domain.events.OneToOneMatchEvent.OneToOneMatchPayload

@Keep
class ApiOneToOneMatchEvent(domainEvent: OneToOneMatchEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiOneToOneMatchPayload(val relativeStartTime: Long,
                                  val relativeEndTime: Long,
                                  val candidateId: String,
                                  val result: ApiMatchEntry?) : ApiEventPayload(ApiEventPayloadType.ONE_TO_ONE_MATCH) {

        constructor(domainPayload: OneToOneMatchPayload) :
            this(domainPayload.creationTime,
                domainPayload.endTime,
                domainPayload.candidateId,
                domainPayload.result?.let { ApiMatchEntry(it) })
    }
}
