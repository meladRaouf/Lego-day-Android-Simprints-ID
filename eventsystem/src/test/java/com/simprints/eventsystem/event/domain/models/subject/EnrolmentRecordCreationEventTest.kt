package com.simprints.eventsystem.event.domain.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes.FACE
import com.simprints.core.domain.modality.Modes.FINGERPRINT
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.moduleapi.fingerprint.IFingerIdentifier.LEFT_3RD_FINGER
import org.junit.Test

class EnrolmentRecordCreationEventTest {

    @Test
    fun create_EnrolmentRecordCreationEvent() {
        val fingerprintReference = FingerprintReference(
            GUID1,
            listOf(FingerprintTemplate(0, "some_template", LEFT_3RD_FINGER)),
            FingerprintTemplateFormat.ISO_19794_2,
            hashMapOf("some_key" to "some_value")
        )
        val faceReference =
            FaceReference(GUID2, listOf(FaceTemplate("some_template")), FaceTemplateFormat.RANK_ONE_1_23)
        val event = EnrolmentRecordCreationEvent(
            CREATED_AT,
            GUID1,
            DEFAULT_PROJECT_ID,
            DEFAULT_MODULE_ID,
            GUID2,
            listOf(FACE, FINGERPRINT),
            listOf(fingerprintReference, faceReference)
        )

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(
            EventLabels(
                subjectId = GUID1,
                projectId = DEFAULT_PROJECT_ID,
                moduleIds = listOf(DEFAULT_MODULE_ID),
                attendantId = GUID2,
                mode = listOf(FACE, FINGERPRINT)
            )
        )
        assertThat(event.type).isEqualTo(ENROLMENT_RECORD_CREATION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(3)
            assertThat(type).isEqualTo(ENROLMENT_RECORD_CREATION)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(subjectId).isEqualTo(GUID1)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(attendantId).isEqualTo(GUID2)
            assertThat(biometricReferences).containsExactly(fingerprintReference, faceReference)
        }
    }
}