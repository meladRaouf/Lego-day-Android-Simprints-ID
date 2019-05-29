package com.simprints.fingerprint.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.activities.orchestrator.Orchestrator
import com.simprints.fingerprint.activities.orchestrator.OrchestratorImpl
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManagerImpl
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.locationprovider.LocationProvider
import com.simprints.fingerprint.controllers.locationprovider.LocationProviderImpl
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.controllers.scanner.ScannerManagerImpl
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.bluetooth.android.AndroidBluetoothAdapter
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import dagger.Module
import dagger.Provides

@Module
open class FingerprintModule {

    @Provides
    @FeatureScope
    open fun provideScannerManager(
        preferencesManager: FingerprintPreferencesManager,
        analyticsManager: FingerprintAnalyticsManager,
        crashReportManager: FingerprintCrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        ScannerManagerImpl(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter)

    @Provides
    @FeatureScope
    open fun provideConsentDataManager(prefs: ImprovedSharedPreferences,
                                       remoteConfigWrapper: RemoteConfigWrapper): ConsentDataManager =
        ConsentDataManagerImpl(prefs, remoteConfigWrapper)

    @Provides
    @FeatureScope
    open fun provideLocationProvider(ctx: Context): LocationProvider = LocationProviderImpl(ctx)

    @Provides
    @FeatureScope
    open fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter =
        AndroidBluetoothAdapter(BluetoothAdapter.getDefaultAdapter())

    @Provides
    @FeatureScope
    open fun provideFingerprintOrchestrator(): Orchestrator =
        OrchestratorImpl()

}