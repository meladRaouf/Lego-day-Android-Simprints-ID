package com.simprints.face.data.moduleapi.face.responses

import android.os.Parcelable

interface FaceResponse: Parcelable {
    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }
}
