package com.simprints.moduleapi.face.requests

import android.os.Parcelable


interface IFaceRequest : Parcelable {

    companion object {
        const val BUNDLE_KEY = "FaceRequestBundleKey"
    }

    val projectId: String
    val userId: String
    val moduleId: String
}