package com.simprints.id

import com.simprints.fingerprint.infra.scanner.data.worker.FirmwareFileUpdateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

class ScheduleBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val syncOrchestrator: SyncOrchestrator,
    private val authStore: AuthStore,
    private val firmwareFileUpdateScheduler: FirmwareFileUpdateScheduler,
) {

    suspend operator fun invoke() {
        if (authStore.signedInProjectId.isNotEmpty()) {
            eventSyncManager.scheduleSync()
            imageUpSyncScheduler.scheduleImageUpSync()
            syncOrchestrator.scheduleBackgroundWork()
            firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()
        }
    }
}
