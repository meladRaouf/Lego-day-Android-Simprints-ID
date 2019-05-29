package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class GuidSelectionEvent(starTime: Long,
                         val selectedId: String) : Event(EventType.GUID_SELECTION, starTime)