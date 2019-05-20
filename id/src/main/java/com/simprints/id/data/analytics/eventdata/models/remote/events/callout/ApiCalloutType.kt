package com.simprints.id.data.analytics.eventdata.models.remote.events.callout

import io.realm.internal.Keep

@Keep
enum class ApiCalloutType {
    CONFIRMATION,
    ENROLMENT,
    IDENTIFICATION,
    VERIFICATION
}
