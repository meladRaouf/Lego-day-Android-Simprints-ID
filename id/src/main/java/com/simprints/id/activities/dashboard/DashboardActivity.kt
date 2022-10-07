package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayer
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncConnecting
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.databinding.ActivityDashboardBinding
import com.simprints.id.databinding.ActivityDashboardCardDailyActivityBinding
import com.simprints.id.databinding.ActivityDashboardCardProjectDetailsBinding
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardActivity : BaseSplitActivity() {

    private var syncAgainTicker: ReceiveChannel<Unit>? = null

    @Inject
    lateinit var projectDetailsCardDisplayer: DashboardProjectDetailsCardDisplayer

    @Inject
    lateinit var syncCardDisplayer: DashboardSyncCardDisplayer

    @Inject
    lateinit var dailyActivityCardDisplayer: DashboardDailyActivityCardDisplayer

    @Inject
    lateinit var viewModelFactory: DashboardViewModelFactory

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    private lateinit var viewModel: DashboardViewModel
    private val binding by viewBinding(ActivityDashboardBinding::inflate)

    // set bindings for included layouts
    private val projectDetailsBinding: ActivityDashboardCardProjectDetailsBinding by lazy { binding.dashboardProjectDetails }
    private val dailyActivityBinding: ActivityDashboardCardDailyActivityBinding by lazy { binding.dashboardDailyActivity }
    private var menu: Menu? = null

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LOGOUT_RESULT_CODE = 1
        private const val ONE_MINUTE = 1000 * 60L
        private const val TIME_FOR_CHECK_IF_SYNC_REQUIRED = 1 * ONE_MINUTE
    }

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(binding.root)
        title = getString(R.string.dashboard_label)

        setupActionBar()
        viewModel = ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]
        setupCards()
        observeConfiguration()
        observeCardData()
        loadDailyActivity()
    }

    private fun setupActionBar() {
        binding.dashboardToolbar.title = getString(R.string.dashboard_label)
        setSupportActionBar(binding.dashboardToolbar)
        supportActionBar?.elevation = 4F

        setMenuItemClickListener()
    }

    private fun setMenuItemClickListener() {
        binding.dashboardToolbar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.menuPrivacyNotice -> startActivity(
                    Intent(
                        this,
                        PrivacyNoticeActivity::class.java
                    )
                )
                R.id.menuSettings -> startActivityForResult(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    ), SETTINGS_ACTIVITY_REQUEST_CODE
                )
                R.id.debug -> if (BuildConfig.DEBUG_MODE) {
                    startActivity(Intent(this, DebugActivity::class.java))
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)

        menu.run {
            findItem(R.id.debug)?.isVisible = BuildConfig.DEBUG_MODE

            findItem(R.id.menuSettings).title =
                getString(R.string.menu_settings)

            with(findItem(R.id.menuPrivacyNotice)) {
                title = getString(R.string.menu_privacy_notice)
            }
        }

        this.menu = menu
        return true
    }

    private fun setupCards() {
        projectDetailsCardDisplayer.initRoot(projectDetailsBinding.dashboardProjectDetailsCard)
        dailyActivityCardDisplayer.initRoot(dailyActivityBinding.dashboardDailyActivityCardRoot)
    }

    @ObsoleteCoroutinesApi
    private fun observeConfiguration() {
        viewModel.syncToBFSIDAllowed.observe(this) {
            if (it) {
                syncCardDisplayer.initRoot(binding.dashboardSyncCard)
                startTickerToCheckIfSyncIsRequired()
            }
        }
        viewModel.consentRequiredLiveData.observe(this) {
            this.menu?.findItem(R.id.menuPrivacyNotice)?.isVisible = it
        }
    }

    private fun observeCardData() {
        observeForProjectDetails()
        observeForSyncCardState()
    }

    private fun observeForProjectDetails() {
        viewModel.projectCardStateLiveData.observe(this) {
            projectDetailsCardDisplayer.displayProjectDetails(it)
        }
    }

    private fun observeForSyncCardState() {
        viewModel.syncCardStateLiveData.observe(this) {
            syncCardDisplayer.displayState(it)
        }

        syncCardDisplayer.userWantsToOpenSettings.observe(this, LiveDataEventObserver {
            openSettings()
        })

        syncCardDisplayer.userWantsToSelectAModule.observe(this, LiveDataEventObserver {
            openSelectModules()
        })

        syncCardDisplayer.userWantsToSync.observe(this, LiveDataEventObserver {
            syncCardDisplayer.displayState(SyncConnecting(null, 0, null))
            eventSyncManager.sync()
        })
    }

    private fun loadDailyActivity() {
        viewModel.dailyActivity.observe(this) {
            if (it.hasNoActivity()) {
                dailyActivityBinding.dashboardDailyActivityCard.visibility = View.GONE
            } else {
                dailyActivityBinding.dashboardDailyActivityCard.visibility = View.VISIBLE
                dailyActivityCardDisplayer.displayDailyActivityState(it)
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    private fun openSelectModules() {
        startActivity(Intent(this, ModuleSelectionActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        loadDailyActivity()
    }

    @ObsoleteCoroutinesApi
    private fun startTickerToCheckIfSyncIsRequired() {
        lifecycleScope.launch {
            stopTickerToCheckIfSyncIsRequired()
            syncAgainTicker = ticker(
                delayMillis = TIME_FOR_CHECK_IF_SYNC_REQUIRED,
                initialDelayMillis = 0
            ).also {
                Simber.tag(SYNC_LOG_TAG).d("[ACTIVITY] Launch sync if required")
                viewModel.syncIfRequired()
            }

            lifecycleScope.launch {
                syncCardDisplayer.startTickerToUpdateLastSyncText()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopTickerToCheckIfSyncIsRequired()
        syncCardDisplayer.stopOngoingTickerToUpdateLastSyncText()
    }

    private fun stopTickerToCheckIfSyncIsRequired() {
        syncAgainTicker?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse =
            AlertActivityHelper.extractPotentialAlertScreenResponse(data)

        if (potentialAlertScreenResponse != null) {
            finish()
        }

        if (resultCode == LOGOUT_RESULT_CODE && requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            startRequestLoginActivityAndFinish()
        }
    }

    private fun startRequestLoginActivityAndFinish() {
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }

}
