package com.simprints.infra.eventsync.sync.down.tasks

import androidx.annotation.VisibleForTesting
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.event.domain.models.subject.*
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class EventDownSyncTask @Inject constructor(
    private val subjectRepository: EnrolmentRecordManager,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val subjectFactory: SubjectFactory,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val eventRemoteDataSource: EventRemoteDataSource,
) {

    suspend fun downSync(
        scope: CoroutineScope,
        operation: EventDownSyncOperation
    ): Flow<EventDownSyncProgress> = flow {
        var lastOperation = operation.copy()
        var count = 0
        val batchOfEventsToProcess = mutableListOf<EnrolmentRecordEvent>()

        try {
            eventRemoteDataSource.getEvents(operation.queryEvent.fromDomainToApi(), scope)
                .consumeAsFlow()
                .collect {
                    batchOfEventsToProcess.add(it)
                    count++
                    //We immediately process the first event to initialise a progress
                    if (batchOfEventsToProcess.size > EVENTS_BATCH_SIZE || count == 1) {
                        lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
                        emitProgress(lastOperation, count)
                        batchOfEventsToProcess.clear()
                    }
                }

            lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
            emitProgress(lastOperation, count)

            lastOperation = lastOperation.copy(state = COMPLETE, lastSyncTime = timeHelper.now())
            emitProgress(lastOperation, count)
        } catch (t: Throwable) {
            Simber.d(t)

            lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
            emitProgress(lastOperation, count)

            lastOperation = lastOperation.copy(state = FAILED, lastSyncTime = timeHelper.now())
            emitProgress(lastOperation, count)
        }
    }

    private suspend fun FlowCollector<EventDownSyncProgress>.emitProgress(
        lastOperation: EventDownSyncOperation,
        count: Int
    ) {
        eventDownSyncScopeRepository.insertOrUpdate(lastOperation)
        this.emit(EventDownSyncProgress(lastOperation, count))
    }

    private suspend fun processBatchedEvents(
        operation: EventDownSyncOperation,
        batchOfEventsToProcess: MutableList<EnrolmentRecordEvent>,
        lastOperation: EventDownSyncOperation
    ): EventDownSyncOperation {

        val actions = batchOfEventsToProcess.map { event ->
            return@map when (event.type) {
                EnrolmentRecordEventType.EnrolmentRecordCreation -> {
                    handleSubjectCreationEvent(event as EnrolmentRecordCreationEvent)
                }
                EnrolmentRecordEventType.EnrolmentRecordDeletion -> {
                    handleSubjectDeletionEvent(event as EnrolmentRecordDeletionEvent)
                }
                EnrolmentRecordEventType.EnrolmentRecordMove -> {
                    handleSubjectMoveEvent(operation, event as EnrolmentRecordMoveEvent)
                }
            }
        }.flatten()

        subjectRepository.performActions(actions)

        Simber.tag(SYNC_LOG_TAG).d("[DOWN_SYNC_HELPER] batch processed")

        return if (batchOfEventsToProcess.size > 0) {
            lastOperation.copy(
                state = RUNNING,
                lastEventId = batchOfEventsToProcess.last().id,
                lastSyncTime = timeHelper.now()
            )
        } else {
            lastOperation.copy(state = RUNNING, lastSyncTime = timeHelper.now())
        }
    }

    @VisibleForTesting
    fun handleSubjectCreationEvent(event: EnrolmentRecordCreationEvent): List<SubjectAction> {
        val subject = subjectFactory.buildSubjectFromCreationPayload(event.payload)
        return if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
            listOf(Creation(subject))
        } else {
            emptyList()
        }
    }

    private suspend fun handleSubjectMoveEvent(
        operation: EventDownSyncOperation,
        event: EnrolmentRecordMoveEvent
    ): List<SubjectAction> {
        val modulesIdsUnderSyncing = operation.queryEvent.moduleIds
        val attendantUnderSyncing = operation.queryEvent.attendantId
        val enrolmentRecordDeletion = event.payload.enrolmentRecordDeletion
        val enrolmentRecordCreation = event.payload.enrolmentRecordCreation

        val actions = mutableListOf<SubjectAction>()
        when {
            modulesIdsUnderSyncing != null && modulesIdsUnderSyncing.isNotEmpty() -> {

                /**
                 * handleSubjectMoveEvent is executed by each worker to process a new moveEvent.
                 * The deletion part of a move is executed if:
                 * 1) the deletion is part of the EventDownSyncOperation (deletion.moduleId == op.moduleId)
                 * AND
                 * 2) the creation is not synced by other workers.
                 * Required to avoid a race condition:
                 * Let's assume that SID is syncing by module1 (by worker1) and module2 (by worker2) and a subjectA
                 * is moved from module1 to module2.
                 * Both workers will receive a move event (worker1 to delete subjectA and worker2 to create subjectA), but
                 * because workers run in parallel, the worker1 may execute the deletion after worker2 has done the
                 * creation. So finally the subject is not moved, but actually deleted.
                 * So if another worker is going to create (insertOrUpdate) the subject for a different module,
                 * then the deletion will be ignored and a the update is executed.)
                 */
                if (enrolmentRecordDeletion.isUnderSyncingByCurrentDownSyncOperation(operation) &&
                    (!enrolmentRecordCreation.isUnderOverallSyncing())
                ) {

                    actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                }

                if (enrolmentRecordCreation.isUnderSyncingByCurrentDownSyncOperation(operation)) {
                    createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                        actions.add(
                            it
                        )
                    }
                }
            }
            attendantUnderSyncing != null -> {
                if (attendantUnderSyncing == enrolmentRecordDeletion.attendantId) {
                    actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                }

                if (attendantUnderSyncing == enrolmentRecordCreation.attendantId) {
                    createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                        actions.add(
                            it
                        )
                    }
                }
            }
            else -> {
                actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                    actions.add(
                        it
                    )
                }
            }
        }

        return actions
    }

    private fun createASubjectActionFromRecordCreation(enrolmentRecordCreation: EnrolmentRecordCreationInMove?): Creation? =
        enrolmentRecordCreation?.let {
            val subject = subjectFactory.buildSubjectFromMovePayload(it)
            if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
                Creation(subject)
            } else {
                null
            }
        }


    private fun EnrolmentRecordDeletionInMove.isUnderSyncingByCurrentDownSyncOperation(op: EventDownSyncOperation) =
        op.queryEvent.moduleIds?.let { moduleId.partOf(it) } ?: false

    private fun EnrolmentRecordCreationInMove.isUnderSyncingByCurrentDownSyncOperation(op: EventDownSyncOperation) =
        op.queryEvent.moduleIds?.let { moduleId.partOf(it) } ?: false

    private suspend fun EnrolmentRecordCreationInMove.isUnderOverallSyncing() =
        moduleId.partOf(configManager.getDeviceConfiguration().selectedModules)

    private fun String.partOf(modules: List<String>) = modules.contains(this)

    private fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent): List<SubjectAction> =
        listOf(Deletion(event.payload.subjectId))

    companion object {
        const val EVENTS_BATCH_SIZE = 200
    }
}