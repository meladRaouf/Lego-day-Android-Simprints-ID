package com.simprints.infra.eventsync.status.down

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.eventsync.SampleSyncScopes.modulesDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.userDownSyncScope
import com.simprints.infra.eventsync.exceptions.MissingArgumentForDownSyncScopeException
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.*
import com.simprints.infra.eventsync.status.down.domain.getUniqueKey
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.down.local.DbEventsDownSyncOperationState
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class EventDownSyncScopeRepositoryImplTest {

    companion object {
        private val LAST_EVENT_ID = GUID1
        private val LAST_SYNC_TIME = TIME1
        private val LAST_STATE = DownSyncState.COMPLETE
    }

    @MockK
    lateinit var loginManager: LoginManager

    @MockK
    lateinit var downSyncOperationOperationDao: DbEventDownSyncOperationStateDao

    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventDownSyncScopeRepository =
            EventDownSyncScopeRepository(
                loginManager,
                downSyncOperationOperationDao,
            )

        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginManager.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID
        coEvery { downSyncOperationOperationDao.load() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun buildProjectDownSyncScope() {
        runTest(UnconfinedTestDispatcher()) {
            val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                GROUP.GLOBAL
            )

            assertProjectSyncScope(syncScope)
        }
    }

    @Test
    fun buildUserDownSyncScope() {
        runTest(UnconfinedTestDispatcher()) {

            val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                GROUP.USER
            )

            assertUserSyncScope(syncScope)
        }
    }

    @Test
    fun buildModuleDownSyncScope() {
        runTest(UnconfinedTestDispatcher()) {
            val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                GROUP.MODULE
            )

            assertModuleSyncScope(syncScope)
        }
    }

    @Test
    fun throwWhenProjectIsMissing() {
        runTest(UnconfinedTestDispatcher()) {
            every { loginManager.getSignedInProjectIdOrEmpty() } returns ""

            assertThrows<MissingArgumentForDownSyncScopeException> {
                eventDownSyncScopeRepository.getDownSyncScope(
                    listOf(Modes.FINGERPRINT),
                    DEFAULT_MODULES.toList(),
                    GROUP.GLOBAL
                )
            }
        }
    }

    @Test
    fun throwWhenUserIsMissing() {
        runTest(UnconfinedTestDispatcher()) {
            every { loginManager.getSignedInUserIdOrEmpty() } returns ""

            assertThrows<MissingArgumentForDownSyncScopeException> {
                eventDownSyncScopeRepository.getDownSyncScope(
                    listOf(Modes.FINGERPRINT),
                    DEFAULT_MODULES.toList(),
                    GROUP.GLOBAL
                )
            }
        }
    }

    @Test
    fun downSyncOp_refresh_shouldReturnARefreshedOp() {
        runTest(UnconfinedTestDispatcher()) {

            val refreshedSyncOp =
                eventDownSyncScopeRepository.refreshState(projectDownSyncScope.operations.first())

            assertThat(refreshedSyncOp).isNotNull()
            refreshedSyncOp.assertProjectSyncOpIsRefreshed()
        }
    }

    @Test
    fun insertOrUpdate_shouldInsertIntoTheDb() {
        runBlocking {
            eventDownSyncScopeRepository.insertOrUpdate(projectDownSyncScope.operations.first())

            coVerify { downSyncOperationOperationDao.insertOrUpdate(any()) }
        }
    }

    @Test
    fun deleteOperations_shouldDeleteOpsFromDb() {
        runBlocking {

            eventDownSyncScopeRepository.deleteOperations(
                DEFAULT_MODULES.toList(),
                listOf(Modes.FINGERPRINT)
            )

            DEFAULT_MODULES.forEach { moduleId ->
                val scope = SubjectModuleScope(
                    DEFAULT_PROJECT_ID,
                    listOf(moduleId),
                    listOf(Modes.FINGERPRINT)
                )
                coVerify(exactly = 1) { downSyncOperationOperationDao.delete(scope.operations.first().getUniqueKey()) }
            }
        }
    }

    @Test
    fun deleteAll_shouldDeleteAllOpsFromDb() {
        runBlocking {

            eventDownSyncScopeRepository.deleteAll()

            coVerify { downSyncOperationOperationDao.deleteAll() }
        }
    }

    private fun getSyncOperationsWithLastResult() =
        projectDownSyncScope.operations.map {
            DbEventsDownSyncOperationState(
                it.getUniqueKey(),
                LAST_STATE,
                LAST_EVENT_ID,
                LAST_SYNC_TIME
            )
        } +
            userDownSyncScope.operations.map {
                DbEventsDownSyncOperationState(
                    it.getUniqueKey(),
                    LAST_STATE,
                    LAST_EVENT_ID,
                    LAST_SYNC_TIME
                )
            } +
            modulesDownSyncScope.operations.map {
                DbEventsDownSyncOperationState(
                    it.getUniqueKey(),
                    LAST_STATE,
                    LAST_EVENT_ID,
                    LAST_SYNC_TIME
                )
            }


    private fun assertProjectSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectProjectScope::class.java)
        with((syncScope as SubjectProjectScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertUserSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectUserScope::class.java)
        with((syncScope as SubjectUserScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertModuleSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectModuleScope::class.java)
        with((syncScope as SubjectModuleScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(moduleIds).containsExactly(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun EventDownSyncOperation.assertProjectSyncOpIsRefreshed() {
        assertThat(lastEventId).isEqualTo(LAST_EVENT_ID)
        assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        assertThat(state).isEqualTo(LAST_STATE)
        assertThat(queryEvent.projectId).isNotNull()
        assertThat(queryEvent.moduleIds).isNull()
        assertThat(queryEvent.modes).isEqualTo(DEFAULT_MODES)
    }
}
