package com.simprints.core.domain.response

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@Keep
@ExcludedFromGeneratedTestCoverageReports("Enum")
enum class AppErrorReason {
    DIFFERENT_PROJECT_ID_SIGNED_IN,
    DIFFERENT_USER_ID_SIGNED_IN,
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    BLUETOOTH_NOT_SUPPORTED,
    LOGIN_NOT_COMPLETE,
    UNEXPECTED_ERROR,
    ROOTED_DEVICE,
    ENROLMENT_LAST_BIOMETRICS_FAILED,
    LICENSE_MISSING,
    LICENSE_INVALID,
    FINGERPRINT_CONFIGURATION_ERROR,
    FACE_CONFIGURATION_ERROR,
    BACKEND_MAINTENANCE_ERROR,
    PROJECT_PAUSED,
    BLUETOOTH_NO_PERMISSION,
    PROJECT_ENDING
}
