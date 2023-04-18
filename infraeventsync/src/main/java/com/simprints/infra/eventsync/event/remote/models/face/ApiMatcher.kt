package com.simprints.infra.eventsync.event.remote.models.face

import com.simprints.infra.events.event.domain.models.Matcher

internal enum class ApiMatcher {

    SIM_AFIS,
    RANK_ONE
}

internal fun Matcher.fromDomainToApi() = when (this) {
    Matcher.SIM_AFIS -> ApiMatcher.SIM_AFIS
    Matcher.RANK_ONE -> ApiMatcher.RANK_ONE
}