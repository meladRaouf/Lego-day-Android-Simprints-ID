package com.simprints.clientapi.legacy.validators

import android.content.Intent
import com.simprints.clientapi.validators.ClientRequestValidator
import com.simprints.libsimprints.Constants

abstract class LegacyCalloutValidator(intent: Intent) : ClientRequestValidator(intent) {

    // TODO: extract legacy api checks here
//    fun hasValidApiKey(): Boolean =
//        !intent.getStringExtra(Constants.SIMPRINTS_API_KEY).isNullOrBlank()

}
