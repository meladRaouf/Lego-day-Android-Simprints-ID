package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.getConfidencesString
import com.simprints.clientapi.extensions.getIdsString
import com.simprints.clientapi.extensions.getTiersString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class OdkPresenter(private val view: OdkContract.View,
                   private val action: String?,
                   private val sessionEventsManager: ClientApiSessionEventsManager,
                   private val crashReportManager: ClientApiCrashReportManager)
    : RequestPresenter(view, sessionEventsManager), OdkContract.Presenter {

    override val domainErrorToCallingAppResultCode: Map<ErrorResponse.Reason, Int>
        get() = emptyMap() //We return CANCEL for any ErrorResponse.Reason

    companion object {
        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override fun start() {
        CoroutineScope(Dispatchers.Main).launch {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)

            when (action) {
                ACTION_REGISTER -> processEnrollRequest()
                ACTION_IDENTIFY -> processIdentifyRequest()
                ACTION_VERIFY -> processVerifyRequest()
                ACTION_CONFIRM_IDENTITY -> processConfirmIdentifyRequest()
                else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
            }
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) = view.returnRegistration(enroll.guid)

    override fun handleIdentifyResponse(identify: IdentifyResponse) = view.returnIdentification(
        identify.identifications.getIdsString(),
        identify.identifications.getConfidencesString(),
        identify.identifications.getTiersString(),
        identify.sessionId
    )

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.matchResult.guidFound,
        verify.matchResult.confidence.toString(),
        verify.matchResult.tier.toString()
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForm(refusalForm.reason, refusalForm.extra)
}
