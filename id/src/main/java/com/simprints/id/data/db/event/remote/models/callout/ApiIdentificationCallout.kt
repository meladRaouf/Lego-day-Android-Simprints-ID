package com.simprints.id.data.db.event.remote.models.callout

import androidx.annotation.Keep

@Keep
data class ApiIdentificationCallout(val projectId: String,
                               val userId: String,
                               val moduleId: String,
                               val metadata: String?): ApiCallout(ApiCalloutType.Identification)