package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool

@Keep
internal data class ApiOneToManyMatchPayload(
    override val startTime: Long,
    override val version: Int,
    val endTime: Long,
    val pool: ApiMatchPool,
    val matcher: String,
    val result: List<ApiMatchEntry>?,
) : ApiEventPayload(ApiEventPayloadType.OneToManyMatch, version, startTime) {

    @Keep
    data class ApiMatchPool(val type: ApiMatchPoolType, val count: Int) {
        constructor(matchPool: MatchPool) :
            this(ApiMatchPoolType.valueOf(matchPool.type.toString()), matchPool.count)
    }

    @Keep
    enum class ApiMatchPoolType {
        USER,
        MODULE,
        PROJECT;
    }

    constructor(domainPayload: OneToManyMatchPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            ApiMatchPool(domainPayload.pool),
            domainPayload.matcher,
            domainPayload.result?.map { ApiMatchEntry(it) })
}