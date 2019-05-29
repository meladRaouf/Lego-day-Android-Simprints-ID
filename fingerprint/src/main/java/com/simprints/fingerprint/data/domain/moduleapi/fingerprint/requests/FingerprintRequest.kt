package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier

interface FingerprintRequest : Parcelable {
    companion object {
        const val BUNDLE_KEY = "FingerprintRequest"
    }

    val projectId: String
    val userId: String
    val moduleId: String
    val metadata: String
    val language: String
    val fingerStatus: Map<FingerIdentifier, Boolean>
    val logoExists: Boolean
    val programName: String
    val organizationName: String
}