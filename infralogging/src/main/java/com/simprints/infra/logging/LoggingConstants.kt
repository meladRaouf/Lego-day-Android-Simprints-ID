package com.simprints.infra.logging

object LoggingConstants {

    /**
     * These keys have been separate into two groups of constants because of legacy reasons.
     * They used to be captured in two separate managers, and in order not to break the backwards
     * compatibility of Analytics and Crashlytics we have kept the original keys. These can be
     * merged into a single set a keys on the next Firebase overhaul.
     */
    object CrashReportingCustomKeys {
        const val USER_ID = "User ID"
        const val PROJECT_ID = "Project ID"
        const val MODULE_IDS = "Module IDs"
        const val DEVICE_ID = "Device ID"
        const val SUBJECTS_DOWN_SYNC_TRIGGERS = "People down sync triggers"
        const val SESSION_ID = "Session ID"
        const val FINGERS_SELECTED = "Fingers selected"
    }

    object AnalyticsUserProperties {
        const val USER_ID = "user_id"
        const val PROJECT_ID = "project_id"
        const val MODULE_ID = "module_id"
        const val DEVICE_ID = "device_id"

        const val MAC_ADDRESS = "mac_address"
        const val SCANNER_ID = "scanner_id"
    }

    enum class CrashReportTag {
        LOGIN,
        LOGOUT,
        SCANNER_SETUP,
        SYNC,
        SESSION,
        FINGER_CAPTURE,
        FACE_CAPTURE,
        MATCHING,
        FACE_MATCHING,
        FACE_LICENSE,
        SETTINGS,
        ALERT,
        REFUSAL,
        ID_SETUP,
        REALM_DB
    }

}
