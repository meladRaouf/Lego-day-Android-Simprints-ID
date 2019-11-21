package com.simprints.id.domain.moduleapi.app.responses

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppConfirmationResponse(val identificationOutcome: Boolean) : AppResponse {

    @IgnoredOnParcel
    override val type: AppResponseType = AppResponseType.CONFIRMATION

}