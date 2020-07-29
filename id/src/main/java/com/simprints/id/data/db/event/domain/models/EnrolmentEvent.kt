package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class EnrolmentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        personId: String,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        EnrolmentPayload(createdAt, EVENT_VERSION, personId),
        ENROLMENT)


    @Keep
    data class EnrolmentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val personId: String,
        override val type: EventType = ENROLMENT,
        override val endedAt: Long = 0) : EventPayload()

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
