package com.simprints.eventsystem.event.domain.models.face

import androidx.annotation.Keep
import com.google.common.truth.Truth
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import com.simprints.eventsystem.event.domain.models.face.FaceOnboardingCompleteEvent.Companion.EVENT_VERSION
import org.junit.Test

@Keep
class FaceOnboardingCompleteEventTest {
    @Test
    fun create_FaceOnboardingCompleteEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val event = FaceOnboardingCompleteEvent(CREATED_AT, ENDED_AT, labels)
        Truth.assertThat(event.id).isNotNull()
        Truth.assertThat(event.labels).isEqualTo(labels)
        Truth.assertThat(event.type).isEqualTo(FACE_ONBOARDING_COMPLETE)
        with(event.payload) {
            Truth.assertThat(createdAt).isEqualTo(CREATED_AT)
            Truth.assertThat(endedAt).isEqualTo(ENDED_AT)
            Truth.assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            Truth.assertThat(type).isEqualTo(FACE_ONBOARDING_COMPLETE)
        }
    }
}

