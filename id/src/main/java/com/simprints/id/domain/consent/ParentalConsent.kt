package com.simprints.id.domain.consent

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.simprints.id.R
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutAction.IDENTIFY
import com.simprints.id.session.callout.CalloutAction.VERIFY


data class ParentalConsent(
    @SerializedName("consent_parent_enrol_only") var consentParentEnrolOnly: Boolean = false,
    @SerializedName("consent_parent_enrol") var consentParentEnrol: Boolean = true,
    @SerializedName("consent_parent_id_verify") var consentParentIdVerify: Boolean = true,
    @SerializedName("consent_parent_share_data_no") var consentParentShareDataNo: Boolean = true,
    @SerializedName("consent_parent_share_data_yes") var consentParentShareDataYes: Boolean = false,
    @SerializedName("consent_parent_collect_yes") var consentParentCollectYes: Boolean = false,
    @SerializedName("consent_parent_privacy_rights") var consentParentPrivacyRights: Boolean = true,
    @SerializedName("consent_parent_confirmation") var consentParentConfirmation: Boolean = true
) {

    fun assembleText(context: Context, calloutAction: CalloutAction, programName: String, organisationName: String) = StringBuilder().apply {
        when (calloutAction) {
            IDENTIFY, VERIFY -> {
                if (consentParentIdVerify) append(context.getString(R.string.consent_parental_id_verify).format(programName))
            }
            else -> {
                if (consentParentEnrolOnly) append(context.getString(R.string.consent_parental_enrol_only).format(programName))
                if (consentParentEnrol) append(context.getString(R.string.consent_parental_enrol).format(programName))
            }
        }
        if (consentParentShareDataNo) append(context.getString(R.string.consent_parental_share_data_no))
        if (consentParentShareDataYes) append(context.getString(R.string.consent_parental_share_data_yes).format(organisationName))
        if (consentParentCollectYes) append(context.getString(R.string.consent_parental_collect_yes))
        if (consentParentPrivacyRights) append(context.getString(R.string.consent_parental_privacy_rights))
        if (consentParentConfirmation) append(context.getString(R.string.consent_parental_confirmation))
    }.toString()
}