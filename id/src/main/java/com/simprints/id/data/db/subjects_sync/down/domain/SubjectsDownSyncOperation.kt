package com.simprints.id.data.db.subjects_sync.down.domain

import com.simprints.id.data.db.subjects_sync.down.local.DbSubjectsDownSyncOperationKey
import com.simprints.id.data.db.subjects_sync.down.local.DbSubjectsDownSyncOperation
import com.simprints.id.domain.modality.Modes

data class SubjectsDownSyncOperation(val projectId: String,
                                     val attendantId: String?,
                                     val moduleId: String?,
                                     val modes: List<Modes>,
                                     val lastResult: SubjectsDownSyncOperationResult?)

fun SubjectsDownSyncOperation.fromDomainToDb(): DbSubjectsDownSyncOperation =
    DbSubjectsDownSyncOperation(
        DbSubjectsDownSyncOperationKey(projectId, modes, attendantId, moduleId),
        projectId, attendantId, moduleId, modes,
        lastResult?.state,
        lastResult?.lastEventId,
        lastResult?.lastSyncTime)
