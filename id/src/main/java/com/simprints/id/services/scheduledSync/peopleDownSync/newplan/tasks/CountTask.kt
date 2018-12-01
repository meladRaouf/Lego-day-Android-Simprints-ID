package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.DownSyncStatus
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.NewSyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.getStatusId
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Ridwan - CountTask: task to:
 * a) Make NetworkRequest - DONE
 * b) InsertOrUpdate DownSyncStatus(p,u,m).totalToDownload = X in Room
 */
class CountTask(component: AppComponent, subSyncScope: SubSyncScope) {

    val projectId = subSyncScope.projectId
    val userId = subSyncScope.userId
    val moduleId = subSyncScope.moduleId

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var newSyncStatusDatabase: NewSyncStatusDatabase

    init {
        component.inject(this)
    }

    fun execute(): Single<Int> {
        Timber.d("Count task executing for module $moduleId")
        return dbManager
            .calculateNPatientsToDownSync(projectId, userId, moduleId)
            .insertNewCountForDownSyncStatus()
    }

    private fun Single<out Int>.insertNewCountForDownSyncStatus() =
        map {

            val downSyncStatus = newSyncStatusDatabase.downSyncStatusModel.getDownSyncStatusForId(getDownSyncId())
                ?: DownSyncStatus(projectId = projectId, userId = userId, moduleId = moduleId)
            downSyncStatus.totalToDownload = it
            newSyncStatusDatabase.downSyncStatusModel.insertOrReplaceDownSyncStatus(downSyncStatus)

            it
        }

    private fun getDownSyncId() = newSyncStatusDatabase.downSyncStatusModel.getStatusId(projectId, userId, moduleId)
}
