package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.eventsync.event.remote.models.ApiTier
import com.simprints.moduleapi.app.responses.IAppResponseTier

@Keep
internal class ApiCallbackComparisonScore(val guid: String, val confidence: Int, val tier: ApiTier)

internal fun CallbackComparisonScore.fromDomainToApi() =
    ApiCallbackComparisonScore(guid, confidence, ApiTier.valueOf(tier.name))

internal fun ApiCallbackComparisonScore.fromApiToDomain() =
    CallbackComparisonScore(guid, confidence, IAppResponseTier.valueOf(tier.name))
