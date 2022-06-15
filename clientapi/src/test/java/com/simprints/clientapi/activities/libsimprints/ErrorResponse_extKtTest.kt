package com.simprints.clientapi.activities.libsimprints

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.Constants
import org.junit.Test

class ErrorResponseExtKtTest {

    @Test
    fun libSimprintsResultCode_BACKEND_MAINTENANCE_ERROR_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR
        val libSimprintsreason = Constants.SIMPRINTS_BACKEND_MAINTENANCE_ERROR

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_FACE_CONFIGURATION_ERROR_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.FACE_CONFIGURATION_ERROR
        val libSimprintsreason = Constants.SIMPRINTS_FACE_CONFIGURATION_ERROR

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_FINGERPRINT_CONFIGURATION_ERROR_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.FINGERPRINT_CONFIGURATION_ERROR
        val libSimprintsreason = Constants.SIMPRINTS_FINGERPRINT_CONFIGURATION_ERROR

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_DIFFERENT_PROJECT_ID_SIGNED_IN_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_PROJECT_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_DIFFERENT_USER_ID_SIGNED_IN_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.DIFFERENT_USER_ID_SIGNED_IN
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_USER_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_GUID_NOT_FOUND_ONLINE_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.GUID_NOT_FOUND_ONLINE
        val libSimprintsreason = Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_UNEXPECTED_ERROR_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.UNEXPECTED_ERROR
        val libSimprintsreason = Constants.SIMPRINTS_UNEXPECTED_ERROR

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_BLUETOOTH_NOT_SUPPORTED_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED
        val libSimprintsreason = Constants.SIMPRINTS_BLUETOOTH_NOT_SUPPORTED

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_CLIENT_REQUEST_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_CLIENT_REQUEST
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_INTENT_ACTION

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_METADATA_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_METADATA
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_METADATA

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_gINVALID_MODULE_ID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_MODULE_ID
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_MODULE_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_PROJECT_ID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_PROJECT_ID
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_PROJECT_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_SELECTED_ID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_SELECTED_ID
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_SELECTED_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_SESSION_ID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_SESSION_ID
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_SESSION_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_USER_ID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_USER_ID
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_USER_ID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_VERIFY_ID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_VERIFY_ID
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_VERIFY_GUID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_LOGIN_NOT_COMPLETE_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.LOGIN_NOT_COMPLETE
        val libSimprintsreason = Constants.SIMPRINTS_LOGIN_NOT_COMPLETE

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_ROOTED_DEVICE_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.ROOTED_DEVICE
        val libSimprintsreason = Constants.SIMPRINTS_ROOTED_DEVICE

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_INVALID_STATE_FOR_INTENT_ACTION_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.INVALID_STATE_FOR_INTENT_ACTION
        val libSimprintsreason = Constants.SIMPRINTS_INVALID_STATE_FOR_INTENT_ACTION

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_ENROLMENT_LAST_BIOMETRICS_FAILED_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.ENROLMENT_LAST_BIOMETRICS_FAILED
        val libSimprintsreason = Constants.SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_FACE_LICENSE_MISSING_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.FACE_LICENSE_MISSING
        val libSimprintsreason = Constants.SIMPRINTS_FACE_LICENSE_MISSING

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_FACE_LICENSE_INVALID_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.FACE_LICENSE_INVALID
        val libSimprintsreason = Constants.SIMPRINTS_FACE_LICENSE_INVALID

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD
        val libSimprintsreason = Constants.SIMPRINTS_SETUP_OFFLINE_DURING_MODALITY_DOWNLOAD

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }

    @Test
    fun libSimprintsResultCode_SETUP_MODALITY_DOWNLOAD_CANCELLED_mapCorrectly() {
        val errorReasponseReason = ErrorResponse.Reason.SETUP_MODALITY_DOWNLOAD_CANCELLED
        val libSimprintsreason = Constants.SIMPRINTS_SETUP_MODALITY_DOWNLOAD_CANCELLED

        assertThat(errorReasponseReason.libSimprintsResultCode()).isEqualTo(libSimprintsreason)
    }
}
