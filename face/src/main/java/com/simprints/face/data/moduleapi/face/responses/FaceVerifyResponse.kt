package com.simprints.face.data.moduleapi.face.responses

import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchingResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceVerifyResponse(val matchingResult: FaceMatchingResult) : FaceResponse