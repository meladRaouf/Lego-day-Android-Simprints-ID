package com.simprints.id.services.scheduledSync.people.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncProgress
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.json.SimJsonHelper
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncDownloaderWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var peopleDownSyncDownloaderWorker: PeopleDownSyncDownloaderWorker

    private val projectSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )


    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()
        app.component = mockk(relaxed = true)
        val correctInputData = SimJsonHelper.gson.toJson(projectSyncOp)
        peopleDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))
        coEvery { peopleDownSyncDownloaderWorker.downSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns null
    }

    @Test
    fun worker_shouldParseInputDataCorrectly() = runBlocking<Unit> {
        with(peopleDownSyncDownloaderWorker) {
            doWork()
            coEvery { personRepository.performDownloadWithProgress(this@runBlocking, any()) }
        }
    }

    @Test
    fun worker_shouldExecuteTheTask() {
        runBlocking {
            with(peopleDownSyncDownloaderWorker) {
                coEvery { peopleSyncCache.readProgress(any()) } returns 0

                coEvery { personRepository.performDownloadWithProgress(any(), any()) } returns produce { PeopleDownSyncProgress(0) }

                doWork()

                coVerify { personRepository.performDownloadWithProgress(any(), any()) }
                verify { resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to 0)) }
            }
        }
    }

    @Test
    fun worker_failForCloudIntegration_shouldFail() = runBlocking<Unit> {
        with(peopleDownSyncDownloaderWorker) {
            coEvery { personRepository.performDownloadWithProgress(this@runBlocking, any()) } throws SyncCloudIntegrationException("Cloud integration", Throwable())

            doWork()

            verify { resultSetter.failure(workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)) }
        }
    }

    @Test
    fun worker_failForNetworkIssue_shouldRetry() = runBlocking<Unit> {
        with(peopleDownSyncDownloaderWorker) {
            coEvery { personRepository.performDownloadWithProgress(this@runBlocking, any()) } throws Throwable("Network Exception")

            doWork()

            verify { resultSetter.retry() }
        }
    }

    @Test
    fun worker_inputDataIsWrong_shouldFail() = runBlocking<Unit> {
        peopleDownSyncDownloaderWorker = createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to ""))
        with(peopleDownSyncDownloaderWorker) {

            doWork()

            verify { resultSetter.failure(any()) }
        }
    }

    @Test
    fun worker_progressCountInProgressData_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<PeopleSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, workDataOf(), listOf(), workDataOf(PROGRESS_DOWN_SYNC to progress), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun worker_SyncDown_shouldExtractTheFinalProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<PeopleSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, workDataOf(OUTPUT_DOWN_SYNC to progress), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun workerResumed_progressCountInCache_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<PeopleSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, workDataOf(), listOf(), workDataOf(), 2)
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<PeopleDownSyncDownloaderWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<PeopleDownSyncDownloaderWorker>(app).build()).apply {
            crashReportManager = mockk(relaxed = true)
            resultSetter = mockk(relaxed = true)
            downSyncScopeRepository = mockk(relaxed = true)
            personRepository = mockk(relaxed = true)
            peopleSyncCache = mockk(relaxed = true)
        }
}

