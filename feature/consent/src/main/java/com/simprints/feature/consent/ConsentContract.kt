package com.simprints.feature.consent

import com.simprints.feature.consent.screens.consent.ConsentFragmentArgs

object ConsentContract {

    val DESTINATION_ID = R.id.consentFragment

    const val CONSENT_RESULT = "consent_result"

    fun getArgs(type: ConsentType) = ConsentFragmentArgs(type).toBundle()
}
