package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.session.Location

@Keep
internal data class ApiLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
) {

    constructor(location: Location) :
        this(location.latitude, location.longitude)
}
