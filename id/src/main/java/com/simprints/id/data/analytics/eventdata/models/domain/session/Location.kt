package com.simprints.id.data.analytics.eventdata.models.domain.session

import androidx.annotation.Keep
import java.util.*

@Keep
open class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0,
                    var id: String = UUID.randomUUID().toString())