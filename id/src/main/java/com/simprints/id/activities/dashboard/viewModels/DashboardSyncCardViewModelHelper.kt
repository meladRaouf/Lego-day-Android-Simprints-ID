package com.simprints.id.activities.dashboard.viewModels

import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class DashboardSyncCardViewModelHelper(private val vm: DashboardSyncCardViewModel) {

    enum class HelperState {
        NEED_INITIALIZATION,
        INITIALIZING,
        READY
    }
    private fun HelperState.isReady() = this == HelperState.READY

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder

    private var helperState: HelperState = HelperState.NEED_INITIALIZATION

    private val state: DashboardSyncCardViewModel.State
        get() = vm.viewModelState

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private var latestDownSyncTime: Long? = null
    private var latestUpSyncTime: Long? = null

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun initViewModel(component: AppComponent) {
        component.inject(this)
        vm.registerObserverForDownSyncWorkerState()
    }

    private fun updateTotalPeopleToDownSyncCount(): Completable =
        Completable.fromAction {
            var peopleToDownload = 0
            syncScope?.toSubSyncScopes()?.forEach { it ->
                peopleToDownload += dbManager.calculateNPatientsToDownSync(it.projectId, it.userId, it.moduleId).blockingGet()
            }
            vm.updateState(peopleToDownload = peopleToDownload, emitState = helperState.isReady())
        }

    private fun updateTotalLocalPeopleCount(): Completable =
        dbManager.getPeopleCountFromLocalForSyncGroup(preferencesManager.syncGroup)
            .flatMapCompletable {
                vm.updateState(peopleInDb = it, emitState = helperState.isReady())
                Completable.complete()
            }

    private fun updateLocalPeopleToUpSyncCount(): Completable =
        localDbManager.getPeopleCountFromLocal(toSync = true)
            .flatMapCompletable {
                vm.updateState(peopleToUpload = it, emitState = helperState.isReady())
                Completable.complete()
            }

    fun onUpSyncStatusChanged(upSyncStatus: UpSyncStatus?) {
        if (upSyncStatus != null) {
            latestUpSyncTime = upSyncStatus.lastUpSyncTime
            val lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            vm.updateState(lastSyncTime = lastSyncTime, emitState = helperState.isReady())
            fetchTotalAndUpSyncCounters()
        }
    }

    fun onDownSyncStatusChanged(downSyncStatuses: List<DownSyncStatus>) {
        if (downSyncStatuses.isNotEmpty()) {
            var peopleToDownSync = 0
            val latestDownSyncTimeForModules: ArrayList<Long> = ArrayList()
            downSyncStatuses.forEach {
                peopleToDownSync += it.totalToDownload
                it.lastSyncTime?.let { lastSyncTime -> latestDownSyncTimeForModules.add(lastSyncTime) }
            }
            latestDownSyncTime = latestDownSyncTimeForModules.max()
            val lastSyncTime = calculateLatestSyncTimeIfPossible(latestDownSyncTime, latestUpSyncTime)
            vm.updateState(
                lastSyncTime = lastSyncTime,
                peopleToDownload = peopleToDownSync,
                emitState = helperState.isReady())
        }
    }

    private fun calculateLatestSyncTimeIfPossible(lastDownSyncTime: Long?, lastUpSyncTime: Long?): String {
        val lastDownSyncDate = lastDownSyncTime?.let { Date(it) }
        val lastUpSyncDate = lastUpSyncTime?.let { Date(it) }

        if (lastDownSyncDate != null && lastUpSyncDate != null) {
            return if (lastDownSyncDate.after(lastUpSyncDate)) {
                lastDownSyncDate.toString()
            } else {
                lastUpSyncDate.toString()
            }
        }

        lastDownSyncDate?.let { return dateFormat.format(it) }
        lastUpSyncDate?.let { return dateFormat.format(it) }
        return ""
    }

    fun onDownSyncRunningChangeState(isDownSyncRunning: Boolean) {
        if (state.isDownSyncRunning != isDownSyncRunning) {
            if (isDownSyncRunning) {
                initForDownSyncRunningIfRequired()
            } else {
                initForDownSyncNotRunningIfRequired()
                fetchTotalAndUpSyncCounters()
            }
            vm.updateState(isDownSyncRunning = isDownSyncRunning, emitState = helperState.isReady())
        }
    }

    private fun initForDownSyncNotRunningIfRequired() {
        if (helperState == HelperState.NEED_INITIALIZATION) {
            helperState = HelperState.INITIALIZING
            vm.updateState(showSyncButton = preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL], emitState = false)

            fetchAllCounters {
                setHelperStateToInitialized()
            }
        }
    }

    private fun initForDownSyncRunningIfRequired() {
        if (helperState == HelperState.NEED_INITIALIZATION) {
            helperState = HelperState.INITIALIZING
            vm.updateState(showSyncButton = preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL], emitState = false)

            fetchTotalAndUpSyncCounters {
                setHelperStateToInitialized()
            }
        }
    }

    private fun fetchAllCounters(done: () -> Unit) {
        updateTotalPeopleToDownSyncCount()
            .andThen(updateTotalLocalPeopleCount())
            .andThen(updateLocalPeopleToUpSyncCount())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doFinally {
                done()
            }
            .subscribeBy(onComplete = {
            }, onError = {
                it.printStackTrace()
            })
    }

    private fun fetchTotalAndUpSyncCounters(done: () -> Unit = {}) {
        updateTotalLocalPeopleCount()
            .andThen(updateLocalPeopleToUpSyncCount())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doFinally {
                done()
            }
            .subscribeBy(onComplete = {
            }, onError = { it.printStackTrace() })
    }

    private fun setHelperStateToInitialized() {
        helperState = HelperState.READY
        vm.registerObserverForDownSyncEvents()
        vm.registerObserverForUpSyncEvents()
        vm.updateState(emitState = true)
    }
}
