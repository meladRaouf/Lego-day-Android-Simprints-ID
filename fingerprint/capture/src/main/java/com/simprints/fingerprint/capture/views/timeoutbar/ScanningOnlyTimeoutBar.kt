package com.simprints.fingerprint.capture.views.timeoutbar

import android.os.CountDownTimer
import android.widget.ProgressBar
import com.simprints.fingerprint.capture.views.timeoutbar.ScanningTimeoutBar.Companion.FINISHED_PROGRESS
import com.simprints.fingerprint.capture.views.timeoutbar.ScanningTimeoutBar.Companion.INITIAL_PROGRESS
import com.simprints.fingerprint.capture.views.timeoutbar.ScanningTimeoutBar.Companion.PROGRESS_INCREMENT
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class ScanningOnlyTimeoutBar(
    override val progressBar: ProgressBar,
    private val scanningTimeoutMs: Long
) : ScanningTimeoutBar {

    private var countDownTimer: CountDownTimer? = null

    private var scanningProgress = INITIAL_PROGRESS

    override fun startTimeoutBar() {
        progressBar.progress = INITIAL_PROGRESS
        scanningProgress = INITIAL_PROGRESS
        countDownTimer = createScanningTimer().also { it.start() }
    }

    private fun createScanningTimer(): CountDownTimer =
        object : CountDownTimer(scanningTimeoutMs, (scanningTimeoutMs / FINISHED_PROGRESS)) {
            override fun onTick(millisUntilFinished: Long) {
                scanningProgress += PROGRESS_INCREMENT
                progressBar.progress = scanningProgress
            }

            override fun onFinish() {
                progressBar.progress = FINISHED_PROGRESS
            }
        }

    override fun handleScanningFinished() {
        handleAllStepsFinished()
    }

    override fun handleAllStepsFinished() {
        countDownTimer?.let {
            it.cancel()
            it.onFinish()
        }
    }

    override fun handleCancelled() {
        countDownTimer?.let {
            it.cancel()
            progressBar.progress = INITIAL_PROGRESS
        }
    }
}