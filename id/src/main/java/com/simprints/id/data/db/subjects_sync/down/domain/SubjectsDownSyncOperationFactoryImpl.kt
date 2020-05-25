package com.simprints.id.data.db.subjects_sync.down.domain

import com.simprints.id.domain.modality.Modes

class SubjectsDownSyncOperationFactoryImpl : SubjectsDownSyncOperationFactory {

    override fun buildProjectSyncOperation(projectId: String,
                                           modes: List<Modes>,
                                           syncOperationResult: SubjectsDownSyncOperationResult?) =
        SubjectsDownSyncOperation(
            projectId = projectId,
            attendantId = null,
            moduleId = null,
            modes = modes,
            lastResult = syncOperationResult
        )

    override fun buildUserSyncOperation(projectId: String,
                                        userId: String,
                                        modes: List<Modes>,
                                        syncOperationResult: SubjectsDownSyncOperationResult?) =
        SubjectsDownSyncOperation(
            projectId = projectId,
            attendantId = userId,
            moduleId = null,
            modes = modes,
            lastResult = syncOperationResult
        )

    override fun buildModuleSyncOperation(projectId: String,
                                          moduleId: String,
                                          modes: List<Modes>,
                                          syncOperationResult: SubjectsDownSyncOperationResult?) =
        SubjectsDownSyncOperation(
            projectId = projectId,
            attendantId = null,
            moduleId = moduleId,
            modes = modes,
            lastResult = syncOperationResult
        )
}
