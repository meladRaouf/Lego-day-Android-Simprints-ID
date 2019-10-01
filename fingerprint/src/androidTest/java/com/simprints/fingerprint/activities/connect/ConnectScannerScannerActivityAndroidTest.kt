package com.simprints.fingerprint.activities.connect

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityViewModel
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.commontesttools.scanner.setupScannerManagerMockWithMockedScanner
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.scanner.old.ScannerManager
import com.simprints.fingerprint.scanner.old.ScannerManagerImpl
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.exceptions.safe.scanner.BluetoothNotEnabledException
import com.simprints.fingerprint.exceptions.safe.scanner.MultipleScannersPairedException
import com.simprints.fingerprint.exceptions.safe.scanner.ScannerLowBatteryException
import com.simprints.fingerprint.exceptions.safe.scanner.ScannerNotPairedException
import com.simprints.fingerprint.exceptions.unexpected.scanner.UnknownScannerIssueException
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothAdapter
import com.simprints.id.Application
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.spy
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare

@RunWith(AndroidJUnit4::class)
class ConnectScannerScannerActivityAndroidTest: KoinTest {

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<ConnectScannerActivity>

    private val scannerManagerSpy: ScannerManager = spy(ScannerManagerImpl(DummyBluetoothAdapter()))
    private val dbManagerMock: FingerprintDbManager = mock()

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
        declare {
            single { scannerManagerSpy }
            factory { dbManagerMock }
        }
        mockDefaultDbManager()
        scannerManagerSpy.setupScannerManagerMockWithMockedScanner()
    }

    private fun mockDefaultDbManager() {
        whenever(dbManagerMock) { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.error(IllegalStateException())
    }

    @Test
    fun notScannerFromInitVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepFailing(ScannerNotPairedException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun multiScannersPairedFromInitVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepFailing(MultipleScannersPairedException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.MULTIPLE_PAIRED_SCANNERS.title)))
    }

    @Test
    fun bluetoothOffFromConnectVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(BluetoothNotEnabledException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.BLUETOOTH_NOT_ENABLED.title)))
    }

    @Test
    fun bluetoothNotSupportedFromConnectVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(BluetoothNotEnabledException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.BLUETOOTH_NOT_SUPPORTED.title)))
    }

    @Test
    fun bluetoothNotPairedFromConnectVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(ScannerNotPairedException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroStep_shouldShowScannerErrorConfirmDialog() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroSetup_clickYes_shouldShowCorrectAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.scanner_confirmation_yes)).perform(click())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.DISCONNECTED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromConnectVeroSetup_clickNo_shouldShowCorrectAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.scanner_confirmation_no)).perform(click())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.NOT_PAIRED.title)))
    }

    @Test
    fun unknownBluetoothIssueFromResetUIVeroStep_shouldShowErrorConfirmDialog() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun lowBatteryFromWakingUpVeroStep_shouldShowAnErrorAlert() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepSucceeding()
        makeWakeUpVeroStepFailing(ScannerLowBatteryException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withId(R.id.alertTitle)).check(matches(withText(AlertActivityViewModel.LOW_BATTERY.title)))
    }

    @Test
    fun unknownBluetoothIssueFromWakingUpVeroStep_shouldShowErrorConfirmDialog() {
        makeInitVeroStepSucceeding()
        makeConnectToVeroStepSucceeding()
        makeResetVeroUIStepSucceeding()
        makeWakeUpVeroStepFailing(UnknownScannerIssueException())

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(withText(containsString("your scanner?")))
            .inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    private fun makeInitVeroStepSucceeding() {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.complete()
    }

    private fun makeConnectToVeroStepSucceeding() {
        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.complete()
    }

    private fun makeResetVeroUIStepSucceeding() {
        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.complete()
    }

    private fun makeInitVeroStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { initVero() } thenReturn Completable.error(e)
    }

    private fun makeConnectToVeroStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { connectToVero() } thenReturn Completable.error(e)
    }

    private fun makeResetVeroUIStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { resetVeroUI() } thenReturn Completable.error(e)
    }

    private fun makeWakeUpVeroStepFailing(e: Exception) {
        whenever(scannerManagerSpy) { wakeUpVero() } thenReturn Completable.error(e)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        releaseFingerprintKoinModules()
    }

    companion object {
        private const val DEFAULT_LANGUAGE = "en"

        private fun connectScannerTaskRequest() = ConnectScannerTaskRequest(DEFAULT_LANGUAGE)

        private fun ConnectScannerTaskRequest.toIntent() = Intent().also {
            it.setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, ConnectScannerActivity::class.qualifiedName!!)
            it.putExtra(ConnectScannerTaskRequest.BUNDLE_KEY, this)
        }
    }
}
