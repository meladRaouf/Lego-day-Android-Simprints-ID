package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.*
import com.simprints.libsimprints.Constants.*

internal fun ErrorResponse.Reason.libSimprintsResultCode() =
    when(this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> SIMPRINTS_INVALID_PROJECT_ID
        DIFFERENT_USER_ID_SIGNED_IN -> SIMPRINTS_INVALID_USER_ID
        GUID_NOT_FOUND_ONLINE -> SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> SIMPRINTS_UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> SIMPRINTS_BLUETOOTH_NOT_SUPPORTED
        INVALID_CLIENT_REQUEST -> SIMPRINTS_INVALID_INTENT_ACTION
        INVALID_METADATA -> SIMPRINTS_INVALID_METADATA
        INVALID_MODULE_ID -> SIMPRINTS_INVALID_MODULE_ID
        INVALID_PROJECT_ID -> SIMPRINTS_INVALID_PROJECT_ID
        INVALID_SELECTED_ID -> SIMPRINTS_INVALID_SELECTED_ID
        INVALID_SESSION_ID -> SIMPRINTS_INVALID_SESSION_ID
        INVALID_USER_ID -> SIMPRINTS_INVALID_USER_ID
        INVALID_VERIFY_ID -> SIMPRINTS_INVALID_VERIFY_GUID
        LOGIN_NOT_COMPLETE -> SIMPRINTS_LOGIN_NOT_COMPLETE
        ROOTED_DEVICE -> SIMPRINTS_ROOTED_DEVICE
        INVALID_STATE_FOR_INTENT_ACTION -> SIMPRINTS_INVALID_STATE_FOR_INTENT_ACTION
        ENROLMENT_LAST_BIOMETRICS_FAILED -> SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED
        FACE_LICENSE_MISSING -> SIMPRINTS_FACE_LICENSE_MISSING
        FACE_LICENSE_INVALID -> SIMPRINTS_FACE_LICENSE_INVALID
        FINGERPRINT_CONFIGURATION_ERROR -> SIMPRINTS_FINGERPRINT_CONFIGURATION_ERROR
        FACE_CONFIGURATION_ERROR -> SIMPRINTS_FACE_CONFIGURATION_ERROR
        BACKEND_MAINTENANCE_ERROR -> SIMPRINTS_BACKEND_MAINTENANCE_ERROR
        PROJECT_ENDING -> SIMPRINTS_INVALID_STATE_FOR_INTENT_ACTION // TODO change to PROJECT_ENDING once LibSimprints is updated
    }
