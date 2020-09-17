package com.simprints.id.activities.settings.syncinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.data.db.common.models.SubjectsCount
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncInformationViewModelTest {

    @MockK lateinit var subjectLocalDataSourceMock: SubjectLocalDataSource
    @MockK lateinit var preferencesManagerMock: PreferencesManager
    @MockK lateinit var subjectsDownSyncScopeRepositoryMock: SubjectsDownSyncScopeRepository
    private lateinit var subjectRepositoryMock: SubjectRepository

    private val projectId = "projectId"
    private lateinit var viewModel: SyncInformationViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        subjectRepositoryMock = mockk()
        viewModel = SyncInformationViewModel(subjectRepositoryMock, subjectLocalDataSourceMock, preferencesManagerMock, projectId, subjectsDownSyncScopeRepositoryMock)
    }

    @Test
    fun fetchCountFromLocal_shouldUpdateValue() = runBlocking {
        val totalRecordsInLocal = 322
        mockSubjectLocalDataSourceCount(totalRecordsInLocal)

        viewModel.fetchLocalRecordCount()

        assertThat(viewModel.localRecordCountLiveData.value).isEqualTo(totalRecordsInLocal)
    }

    @Test
    fun fetchCountFromRemote_shouldUpdateValue() = runBlockingTest {
        val countInRemoteForCreate = 123
        val countInRemoteForMove = 0
        val countInRemoteForDelete = 22
        val subjectsCount = SubjectsCount(countInRemoteForCreate, countInRemoteForDelete, countInRemoteForMove)

        every { subjectsDownSyncScopeRepositoryMock.getDownSyncScope() } returns projectSyncScope
        coEvery { subjectRepositoryMock.countToDownSync(any()) } returns subjectsCount

        viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount()

        assertThat(viewModel.recordsToDownSyncCountLiveData.value).isEqualTo(countInRemoteForCreate)
        assertThat(viewModel.recordsToDeleteCountLiveData.value).isEqualTo(countInRemoteForDelete)
    }

    @Test
    fun fetchRecordsToUpSyncCount_shouldUpdateValue() = runBlockingTest {
        val recordsToUpSyncCount = 123
        mockSubjectLocalDataSourceCount(recordsToUpSyncCount)

        viewModel.fetchAndUpdateRecordsToUpSyncCount()

        assertThat(viewModel.recordsToUpSyncCountLiveData.value).isEqualTo(recordsToUpSyncCount)
    }

    @Test
    fun fetchSelectedModulesCount_shouldUpdateValue() = runBlockingTest {
        val moduleName = "module1"
        val countForModule = 123
        every { preferencesManagerMock.selectedModules } returns setOf(moduleName)
        mockSubjectLocalDataSourceCount(countForModule)

        viewModel.fetchAndUpdateSelectedModulesCount()

        with(viewModel.selectedModulesCountLiveData.value?.first()) {
            assertThat(this?.name).isEqualTo(moduleName)
            assertThat(this?.count).isEqualTo(countForModule)
        }
    }

    @Test
    fun withUnselectedModules_shouldUpdateValue() = runBlockingTest {
        val selectedModuleName = "module1"
        val unselectedModuleName = "module2"
        val recordWithSelectedModule = Subject(
            randomUUID(),
            projectId,
            "some_user_id",
            selectedModuleName
        )

        val recordWithUnselectedModule = recordWithSelectedModule.copy(
            moduleId = unselectedModuleName
        )

        val subjectsRecords = flowOf(
            recordWithSelectedModule,
            recordWithUnselectedModule,
            recordWithUnselectedModule
        )
        val selectedModuleSet = setOf(selectedModuleName)

        coEvery { subjectLocalDataSourceMock.load(any()) } returns subjectsRecords
        every { preferencesManagerMock.selectedModules } returns selectedModuleSet

        viewModel.fetchAndUpdatedUnselectedModulesCount()

        with(viewModel.unselectedModulesCountLiveData.value?.first()) {
            assertThat(this?.name).isEqualTo(unselectedModuleName)
            assertThat(this?.count).isEqualTo(2)
        }
    }

    @Test
    fun withNoUnselectedModules_shouldUpdateValueAsEmptyList() = runBlockingTest {
        val selectedModuleName = "module1"
        val recordWithSelectedModule = Subject(
            randomUUID(),
            projectId,
            "some_user_id",
            selectedModuleName
        )
        val subjectsRecords = flowOf(recordWithSelectedModule, recordWithSelectedModule)
        val selectedModuleSet = setOf(selectedModuleName)

        coEvery { subjectLocalDataSourceMock.load(any()) } returns subjectsRecords
        every { preferencesManagerMock.selectedModules } returns selectedModuleSet

        viewModel.fetchAndUpdatedUnselectedModulesCount()

        assertThat(viewModel.unselectedModulesCountLiveData.value).isEmpty()
    }

    @Test
    fun downSyncSettingIsOn_shouldRequestRecordsToDownloadAndDeleteCount() = runBlockingTest {
        every { preferencesManagerMock.subjectsDownSyncSetting } returns SubjectsDownSyncSetting.ON

        viewModel.fetchRecordsToUpdateAndDeleteCountIfNecessary()

        coVerify(exactly = 1) { viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount() }
    }

    @Test
    fun downSyncSettingIsExtra_shouldRequestRecordsToDownloadAndDeleteCount() = runBlockingTest {
        every { preferencesManagerMock.subjectsDownSyncSetting } returns SubjectsDownSyncSetting.EXTRA

        viewModel.fetchRecordsToUpdateAndDeleteCountIfNecessary()

        coVerify(exactly = 1) { viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount() }
    }

    @Test
    fun downSyncSettingIsOffShouldRequestRecordsToDownloadAndDeleteCount() = runBlockingTest {
        every { preferencesManagerMock.subjectsDownSyncSetting } returns SubjectsDownSyncSetting.OFF

        viewModel.fetchRecordsToUpdateAndDeleteCountIfNecessary()

        coVerify(exactly = 0) { viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount() }
    }

    private fun mockSubjectLocalDataSourceCount(recordCount: Int) {
        coEvery { subjectLocalDataSourceMock.count(any()) } returns recordCount
    }

}
