package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.eventsync.event.remote.models.subject.ApiEnrolmentRecordPayloadType
import com.simprints.infra.eventsync.event.remote.models.subject.fromApiToDomain

@Keep
internal data class ApiEventCount(val type: ApiEnrolmentRecordPayloadType, val count: Int)

internal fun ApiEventCount.fromApiToDomain() = EventCount(type.fromApiToDomain(), count)
