package com.simprints.moduleapi.common

import android.os.Parcelable

interface ISecuredImageRef : Parcelable {
    val relativePath: IPath
    val fullPath: String
}
