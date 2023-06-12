package com.simprints.fingerprint.activities.connect.issues

import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryFragmentRequest

sealed class ConnectScannerIssue {
    object BluetoothOff : ConnectScannerIssue()
    object NfcOff : ConnectScannerIssue()
    object NfcPair : ConnectScannerIssue()
    object SerialEntryPair : ConnectScannerIssue()
    object ScannerOff : ConnectScannerIssue()
    class Ota(val otaFragmentRequest: OtaFragmentRequest) : ConnectScannerIssue()
    class OtaRecovery(val otaRecoveryFragmentRequest: OtaRecoveryFragmentRequest) : ConnectScannerIssue()
    object OtaFailed : ConnectScannerIssue()
}