package com.simprints.infra.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.EVENT_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME
import com.simprints.infra.sync.SyncConstants.FIRMWARE_UPDATE_WORK_NAME
import com.simprints.infra.sync.SyncConstants.IMAGE_UP_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.PROJECT_SYNC_WORK_NAME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME
import com.simprints.infra.sync.SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyncOrchestratorImplTest {


    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configRepo: ConfigRepository

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase

    @MockK
    private lateinit var cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase

    private lateinit var syncOrchestrator: SyncOrchestratorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        syncOrchestrator = SyncOrchestratorImpl(
            workManager,
            authStore,
            configRepo,
            eventSyncManager,
            shouldScheduleFirmwareUpdate,
            cleanupDeprecatedWorkers,
        )
    }

    @Test
    fun `does not schedules any workers if not logged in`() = runTest {
        every { authStore.signedInProjectId } returns ""
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        syncOrchestrator.scheduleBackgroundWork()

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        }
    }

    @Test
    fun `schedules all necessary background workers if logged in`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns true

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.enqueueUniquePeriodicWork(PROJECT_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(DEVICE_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(IMAGE_UP_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(EVENT_SYNC_WORK_NAME, any(), any())
            workManager.enqueueUniquePeriodicWork(FIRMWARE_UPDATE_WORK_NAME, any(), any())
        }
    }

    @Test
    fun `schedules images with any connection if not specified`() = runTest {
        coEvery {
            configRepo.getProjectConfiguration().synchronization.up.imagesRequireUnmeteredConnection
        } returns false
        every { authStore.signedInProjectId } returns "projectId"

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.enqueueUniquePeriodicWork(
                IMAGE_UP_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.constraints.requiredNetworkType == NetworkType.CONNECTED }
            )
        }
    }

    @Test
    fun `schedules images with unmetered constraint if requested`() = runTest {
        coEvery {
            configRepo.getProjectConfiguration().synchronization.up.imagesRequireUnmeteredConnection
        } returns true
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.enqueueUniquePeriodicWork(
                IMAGE_UP_SYNC_WORK_NAME,
                any(),
                match { it.workSpec.constraints.requiredNetworkType == NetworkType.UNMETERED }
            )
        }
    }

    @Test
    fun `schedules cancel firmware update worker if no support for vero 2`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"
        coEvery { shouldScheduleFirmwareUpdate.invoke() } returns false

        syncOrchestrator.scheduleBackgroundWork()

        verify {
            workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME)
        }
    }

    @Test
    fun `cancels all necessary background workers`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        syncOrchestrator.cancelBackgroundWork()

        verify {
            workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(IMAGE_UP_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(FIRMWARE_UPDATE_WORK_NAME)

            // Explicitly cancel event sync sub-workers
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `schedules device worker when requested`() = runTest {
        syncOrchestrator.startDeviceSync()

        verify {
            workManager.enqueueUniqueWork(
                DEVICE_SYNC_WORK_NAME_ONE_TIME,
                any(),
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `reschedules event sync worker with correct tags`() = runTest {
        every { eventSyncManager.getPeriodicWorkTags() } returns listOf("tag1", "tag2")

        syncOrchestrator.rescheduleEventSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                EVENT_SYNC_WORK_NAME,
                any(),
                match { it.tags.containsAll(setOf("tag1", "tag2")) })
        }
    }

    @Test
    fun `cancel event sync worker cancels correct worker`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        syncOrchestrator.cancelEventSync()

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME)
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `start event sync worker with correct tags`() = runTest {
        every { eventSyncManager.getOneTimeWorkTags() } returns listOf("tag1", "tag2")

        syncOrchestrator.startEventSync()

        verify {
            workManager.enqueueUniqueWork(
                EVENT_SYNC_WORK_NAME_ONE_TIME,
                any(),
                match<OneTimeWorkRequest> { it.tags.containsAll(setOf("tag1", "tag2")) }
            )
        }
    }

    @Test
    fun `stop event sync worker cancels correct worker`() = runTest {
        every { eventSyncManager.getAllWorkerTag() } returns "syncWorkers"

        syncOrchestrator.cancelEventSync()

        verify {
            workManager.cancelUniqueWork(EVENT_SYNC_WORK_NAME_ONE_TIME)
            workManager.cancelAllWorkByTag("syncWorkers")
        }
    }

    @Test
    fun `reschedules image worker when requested`() = runTest {
        syncOrchestrator.rescheduleImageUpSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                IMAGE_UP_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                any(),
            )
        }
    }

    @Test
    fun `schedules record upload`() = runTest {
        syncOrchestrator.uploadEnrolmentRecords(INSTRUCTION_ID, listOf(SUBJECT_ID))

        coVerify(exactly = 1) {
            workManager.enqueueUniqueWork(
                any(),
                any(),
                match<OneTimeWorkRequest> { oneTimeWorkRequest ->
                    val subjectIdsInput = oneTimeWorkRequest.workSpec.input.getStringArray(
                        RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME
                    )
                    val instructionIdInput = oneTimeWorkRequest.workSpec.input.getString(
                        RECORD_UPLOAD_INPUT_ID_NAME
                    )
                    instructionIdInput == INSTRUCTION_ID &&
                        subjectIdsInput.contentEquals(arrayOf(SUBJECT_ID))
                }
            )
        }
    }

    @Test
    fun `delegates sync info deletion`() = runTest {
        syncOrchestrator.deleteEventSyncInfo()
        coVerify { eventSyncManager.deleteSyncInfo() }
    }

    @Test
    fun `delegates worker cleanup requests`() = runTest {
        syncOrchestrator.cleanupWorkers()
        verify { cleanupDeprecatedWorkers.invoke() }
    }

    companion object {

        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }
}
