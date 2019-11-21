package com.simprints.fingerprint.activities.connect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.connect.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForConnectScannerActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate.vibrate
import kotlinx.android.synthetic.main.activity_connect_scanner.*
import org.koin.android.viewmodel.ext.android.viewModel

class ConnectScannerActivity : FingerprintActivity() {

    private val viewModel: ConnectScannerViewModel by viewModel()

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val connectScannerRequest: ConnectScannerTaskRequest =
            this.intent.extras?.getParcelable(ConnectScannerTaskRequest.BUNDLE_KEY) as ConnectScannerTaskRequest?
                ?: throw InvalidRequestForConnectScannerActivityException()

        observeScannerEvents()
        observeLifecycleEvents()

        viewModel.start()
    }

    private fun observeScannerEvents() {
        viewModel.progress.observe(this, Observer { connectScannerProgressBar.progress = it })
        viewModel.message.observe(this, Observer { connectScannerInfoTextView.setText(it) })
        viewModel.vibrate.observe(this, Observer { vibrate(this) })
        viewModel.showScannerErrorDialogWithScannerId.observe(this, Observer { showDialogForScannerErrorConfirmation(it) })
    }

    private fun observeLifecycleEvents() {
        viewModel.launchAlert.observe(this, Observer { launchAlert(this, it) })
        viewModel.finish.observe(this, Observer { continueToNextActivity() })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                    scannerErrorConfirmationDialog?.dismiss()
                    viewModel.tryAgainFromErrorOrRefusal()
                }
            }
        }
    }

    private fun continueToNextActivity() {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(ConnectScannerTaskResult.BUNDLE_KEY, ConnectScannerTaskResult())
        })
    }

    private fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), RequestCode.REFUSAL.value)
    }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    private fun showDialogForScannerErrorConfirmation(scannerId: String) {
        scannerErrorConfirmationDialog = buildConfirmScannerErrorAlertDialog(scannerId).also {
            it.show()
            viewModel.logScannerErrorDialogShownToCrashReport()
        }
    }

    private fun buildConfirmScannerErrorAlertDialog(scannerId: String) =
        ConfirmScannerErrorBuilder()
            .build(
                this, scannerId,
                onYes = { viewModel.handleScannerDisconnectedYesClick() },
                onNo = { viewModel.handleScannerDisconnectedNoClick() }
            )

    override fun onBackPressed() {
        goToRefusalActivity()
    }
}