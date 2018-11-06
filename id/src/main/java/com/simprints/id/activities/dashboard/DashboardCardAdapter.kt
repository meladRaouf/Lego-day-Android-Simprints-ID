package com.simprints.id.activities.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.activities.dashboard.views.DashboardCardView
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView

class DashboardCardAdapter(private val cardModels: ArrayList<DashboardCard>) :
    RecyclerView.Adapter<DashboardCardView>() {

    enum class CardViewType {
        GENERAL,
        SYNC
    }

    override fun getItemViewType(position: Int): Int {
        return if (cardModels[position] is DashboardSyncCard) {
            CardViewType.SYNC.ordinal
        } else {
            CardViewType.GENERAL.ordinal
        }
    }

    override fun onBindViewHolder(holder: DashboardCardView, position: Int) = holder.bind(cardModels[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == CardViewType.GENERAL.ordinal) {
            DashboardCardView(LayoutInflater.from(parent.context).inflate(R.layout.activity_dashboard_card, parent, false))
        } else {
            DashboardSyncCardView(LayoutInflater.from(parent.context).inflate(R.layout.activity_dashboard_sync_card, parent, false))
        }

    override fun getItemCount() = cardModels.size
}
