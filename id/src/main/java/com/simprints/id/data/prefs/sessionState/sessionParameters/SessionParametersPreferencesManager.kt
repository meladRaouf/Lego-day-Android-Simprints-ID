package com.simprints.id.data.prefs.sessionState.sessionParameters

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.sessionParameters.SessionParameters


interface SessionParametersPreferencesManager {

    var calloutAction: CalloutAction
    var appKey: String
    var moduleId: String
    var userId: String
    var patientId: String
    var callingPackage: String
    var metadata: String
    var resultFormat: String

    var sessionParameters: SessionParameters
        set(value) {
            calloutAction = value.calloutAction
            appKey = value.apiKey.substring(0, 8)
            moduleId = value.moduleId
            userId = value.userId
            patientId = value.patientId
            callingPackage = value.callingPackage
            metadata = value.metadata
            resultFormat = value.resultFormat
        }
        get() {
            throw NotImplementedError()
        }

    fun resetSessionParameters()

}