package com.simprints.id.data.db.event.domain.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.*
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentRecordCreationEvent(
    createdAt: Long,
    subjectId: String,
    projectId: String,
    moduleId: String,
    attendantId: String,
    biometricReferences: List<BiometricReference>
) : Event(
    UUID.randomUUID().toString(),
    listOf(ProjectId(projectId), ModuleId(listOf(moduleId)), AttendantId(attendantId)),
    EnrolmentRecordCreationPayload(createdAt, DEFAULT_EVENT_VERSION, subjectId, projectId, moduleId, attendantId, biometricReferences)) {

    class EnrolmentRecordCreationPayload(
        createdAt: Long,
        eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>
    ) : EventPayload(EventPayloadType.ENROLMENT_RECORD_CREATION, eventVersion, createdAt)
// startTime and relativeStartTime are not used for Pokodex events
}
