package com.simprints.fingerprint.activities.connect.issues.scanneroff

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import kotlinx.android.synthetic.main.fragment_scanner_off.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ScannerOffFragment : Fragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_scanner_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tryAgainButton.setOnClickListener {
            replaceTryAgainButtonWithProgressBar()
        }

        connectScannerViewModel.showScannerErrorDialogWithScannerId.value?.let { scannerId ->
            couldNotConnectTextView.paintFlags = couldNotConnectTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            couldNotConnectTextView.text = getString(R.string.not_my_scanner, scannerId)
            couldNotConnectTextView.setOnClickListener {
                connectScannerViewModel.handleIncorrectScanner()
            }
            couldNotConnectTextView.visibility = View.VISIBLE
        }

        connectScannerViewModel.retryConnect()
    }

    override fun onResume() {
        super.onResume()
        connectScannerViewModel.scannerConnected.observe(this, Observer { success: Boolean? ->
            when (success) {
                true -> handleScannerConnected()
                false -> connectScannerViewModel.retryConnect()
            }
        })
        connectScannerViewModel.connectScannerIssue.observe(this, Observer {
            it?.let {
                // Set to null so it is cleared for future fragments
                connectScannerViewModel.connectScannerIssue.value = null
                connectScannerViewModel.stopConnectingAndResetState()
                goToToAppropriatePairingScreen(it)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        connectScannerViewModel.scannerConnected.removeObservers(this)
    }

    // The tryAgainButton doesn't actually do anything - we're already retrying in the background
    // Show a progress bar to make it known that something is happening
    private fun replaceTryAgainButtonWithProgressBar() {
        scannerOffProgressBar.visibility = View.VISIBLE
        tryAgainButton.visibility = View.INVISIBLE
        tryAgainButton.isEnabled = false
    }

    private fun handleScannerConnected() {
        scannerOffProgressBar.visibility = View.INVISIBLE
        couldNotConnectTextView.visibility = View.INVISIBLE
        tryAgainButton.visibility = View.VISIBLE
        tryAgainButton.isEnabled = false
        tryAgainButton.setText(R.string.scanner_on)
        tryAgainButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ finishConnectActivity() }, FINISHED_TIME_DELAY_MS)
    }

    private fun goToToAppropriatePairingScreen(issue: ConnectScannerIssue) {
        val navAction = when (issue) {
            ConnectScannerIssue.NFC_OFF -> R.id.action_scannerOffFragment_to_nfcOffFragment
            ConnectScannerIssue.NFC_PAIR -> R.id.action_scannerOffFragment_to_nfcPairFragment
            ConnectScannerIssue.SERIAL_ENTRY_PAIR -> R.id.action_scannerOffFragment_to_serialEntryPairFragment
            else -> null
        }
        navAction?.let { findNavController().navigate(it) }
    }

    private fun finishConnectActivity() {
        connectScannerViewModel.finish.postValue(Unit)
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
