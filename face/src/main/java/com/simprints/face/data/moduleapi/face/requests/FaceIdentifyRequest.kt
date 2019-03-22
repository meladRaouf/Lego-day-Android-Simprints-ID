package com.simprints.face.data.moduleapi.face.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceIdentifyRequest(val projectId: String,
                               val userId: String,
                               val moduleId: String) : FaceRequest

