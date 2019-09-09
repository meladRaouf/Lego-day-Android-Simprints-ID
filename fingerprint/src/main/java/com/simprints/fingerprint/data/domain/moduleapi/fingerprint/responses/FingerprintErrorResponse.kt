package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintErrorResponse(val reason: FingerprintErrorReason) : FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ERROR
}

/**
 * If user presses a CLOSE button, we return a FingerprintResponse.
 * If user presses BACK, an ExitForm is shown, except for UNEXPECTED_ERROR and GUID_NOT_FOUND_ONLINE (same as CLOSE).
 */
enum class FingerprintErrorReason {
    GUID_NOT_FOUND_ONLINE,
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED;

    companion object {
        fun fromFingerprintAlertToErrorResponse(fingerprintAlert: FingerprintAlert): FingerprintErrorResponse =
            when (fingerprintAlert) {
                FingerprintAlert.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                FingerprintAlert.UNEXPECTED_ERROR -> UNEXPECTED_ERROR

                //User can not leave these alerts, so Fingerprint module should not produce any error response for them.
                FingerprintAlert.BLUETOOTH_NOT_ENABLED,
                FingerprintAlert.NOT_PAIRED,
                FingerprintAlert.MULTIPLE_PAIRED_SCANNERS,
                FingerprintAlert.DISCONNECTED,
                FingerprintAlert.LOW_BATTERY -> UNEXPECTED_ERROR
            }.run {
                FingerprintErrorResponse(this)
            }
    }
}
