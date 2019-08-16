package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.consent.shortconsent.ConsentDataManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.TimeHelper

class ConsentViewModelFactory(private val consentDataManager: ConsentDataManager,
                              private val crashReportManager: CrashReportManager,
                              private val preferencesManager: PreferencesManager,
                              private val sessionEventsManager: SessionEventsManager,
                              private val timeHelper: TimeHelper) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(consentDataManager,
                crashReportManager,
                sessionEventsManager,
                preferencesManager.programName,
                preferencesManager.organizationName,
                timeHelper) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
