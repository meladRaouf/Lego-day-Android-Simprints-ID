package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.db.sync.room.SyncStatus
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.db.sync.viewModel.SyncStatusViewModel
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)
    private val syncButton: Button = rootView.findViewById(R.id.dashboardSyncCardSyncButton)
    private val totalPeopleInLocal: TextView = rootView.findViewById(R.id.totalPeopleInLocal)

    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun updateCard(cardModel: DashboardCard) {
        bind(cardModel)
    }

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)

        val component = (rootView.context.applicationContext as Application).component
        component.inject(this)

        if (cardModel is DashboardSyncCard) {
            cardModel.cardView = this
            setTotalPeopleInDbCounter(cardModel)
            setUploadCounter(cardModel)
            setDownloadCounterAndLastSyncTime(cardModel)
            setListenerForSyncButton(cardModel)
        }
    }

    private fun setTotalPeopleInDbCounter(cardModel: DashboardSyncCard) {
        totalPeopleInLocal.text = "${Math.max(cardModel.peopleInDb, 0)}"
    }

    private fun setUploadCounter(cardModel: DashboardSyncCard) {
        syncUploadCount.text = "${Math.max(cardModel.peopleToUpload, 0)}"
    }

    private fun setDownloadCounterAndLastSyncTime(cardModel: DashboardSyncCard) {

        val observer = Observer<SyncStatus> {
            cardModel.peopleToDownload = it.peopleToDownSync
            syncDownloadCount.text = "${Math.max(it.peopleToDownSync, 0)}"
            if (it.peopleToDownSync > 0) {
                cardModel.syncNeeded = true
            }
            calculateLastSyncTimeAndUpdateText(it)
        }
        val syncStatusViewModel = SyncStatusViewModel(syncStatusDatabase)
        syncStatusViewModel.syncStatus.observe(rootView.context as DashboardActivity, observer)
    }

    private fun calculateLastSyncTimeAndUpdateText(syncStatus: SyncStatus) {
        val lastSyncTime = calculateLatestSyncTime(syncStatus.lastDownSyncTime, syncStatus.lastUpSyncTime)
        syncDescription.text = String.format(rootView.context.getString(R.string.dashboard_card_sync_last_sync),
            lastSyncTime)
    }

    private fun calculateLatestSyncTime(lastDownSyncTime: String?, lastUpSyncTime: String?): String {
        val lastDownSyncDate = lastDownSyncTime?.let { dateFormat.parse(it) }
        val lastUpSyncDate =  lastUpSyncTime?.let { dateFormat.parse(it) }

        if (lastDownSyncDate != null && lastUpSyncDate != null) {
            return if (lastDownSyncDate.after(lastUpSyncDate)) {
                lastDownSyncDate.toString()
            } else {
                lastUpSyncDate.toString()
            }
        }

        lastDownSyncDate?.let { return it.toString() }
        lastUpSyncDate?.let { return it.toString() }

        return ""
    }

    private fun setListenerForSyncButton(cardModel: DashboardSyncCard) {
        if (cardModel.peopleToDownload > 0 || cardModel.peopleToUpload > 0) {
            syncButton.setOnClickListener { cardModel.onSyncActionClicked(cardModel) }
        }
    }
}
