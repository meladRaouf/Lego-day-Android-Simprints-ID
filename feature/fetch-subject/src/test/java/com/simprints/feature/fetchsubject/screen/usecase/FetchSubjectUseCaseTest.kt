package com.simprints.feature.fetchsubject.screen.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.fetchsubject.screen.FetchSubjectState
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.network.ConnectivityTracker
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class FetchSubjectUseCaseTest {

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    private lateinit var connectivityTracker: ConnectivityTracker

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var subject: Subject


    private lateinit var useCase: FetchSubjectUseCase


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = FetchSubjectUseCase(connectivityTracker, enrolmentRecordManager, eventSyncManager)
    }

    @Test
    fun `fetch should check local DB first`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns flowOf(subject)

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        coVerify { enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)) }
        assertThat(result).isInstanceOf(FetchSubjectState.FoundLocal::class.java)
    }

    @Test
    fun `fetch should not check remote if present in local DB`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns flowOf(subject)

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        coVerify(exactly = 0) { eventSyncManager.downSyncSubject(any(), any()) }
        assertThat(result).isInstanceOf(FetchSubjectState.FoundLocal::class.java)
    }

    @Test
    fun `fetch should try down syncing if not present in local DB`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns emptyFlow()

        useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        coVerify { eventSyncManager.downSyncSubject(any(), any()) }
    }

    @Test
    fun `fetch should return from local DB if present after downsync`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returnsMany listOf(emptyFlow(), flowOf(subject))

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        coVerify(exactly = 2) { enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)) }
        assertThat(result).isInstanceOf(FetchSubjectState.FoundRemote::class.java)
    }

    @Test
    fun `fetch should return notFound if not present after downsync and connected`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns emptyFlow()
        every { connectivityTracker.isConnected() } returns true

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        assertThat(result).isInstanceOf(FetchSubjectState.NotFound::class.java)
    }

    @Test
    fun `fetch should return connection error if not present after downsync and not connected`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns emptyFlow()
        every { connectivityTracker.isConnected() } returns false

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        assertThat(result).isInstanceOf(FetchSubjectState.ConnectionError::class.java)
    }

    @Test
    fun `fetch should return notFound if fails and connected`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } throws Exception("test")
        every { connectivityTracker.isConnected() } returns true

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        assertThat(result).isInstanceOf(FetchSubjectState.NotFound::class.java)
    }

    @Test
    fun `fetch should return connection error if fails and not connected`() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } throws Exception("test")
        every { connectivityTracker.isConnected() } returns false

        val result = useCase(DEFAULT_PROJECT_ID, DEFAULT_SUBJECT_ID)

        assertThat(result).isInstanceOf(FetchSubjectState.ConnectionError::class.java)
    }


    companion object {
        private const val DEFAULT_PROJECT_ID = "projectId"
        private const val DEFAULT_SUBJECT_ID = "subject"
    }

}
