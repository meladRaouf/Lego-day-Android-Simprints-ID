package com.simprints.clientapi.activities.baserequest

import androidx.annotation.Keep
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_METADATA
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_MODULE_ID
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_PROJECT_ID
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_SELECTED_ID
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_SESSION_ID
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_STATE_FOR_INTENT_ACTION
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_USER_ID
import com.simprints.clientapi.activities.errors.ClientApiAlert.INVALID_VERIFY_ID
import com.simprints.clientapi.activities.errors.ClientApiAlert.ROOTED_DEVICE
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.builders.ConfirmIdentifyBuilder
import com.simprints.clientapi.clientrequests.builders.EnrolBuilder
import com.simprints.clientapi.clientrequests.builders.EnrolLastBiometricsBuilder
import com.simprints.clientapi.clientrequests.builders.IdentifyBuilder
import com.simprints.clientapi.clientrequests.builders.VerifyBuilder
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentityValidator
import com.simprints.clientapi.clientrequests.validators.EnrolLastBiometricsValidator
import com.simprints.clientapi.clientrequests.validators.EnrolValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.canCoSyncData
import com.simprints.clientapi.data.sharedpreferences.canSyncDataToSimprints
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidRequestException
import com.simprints.clientapi.exceptions.InvalidSelectedIdException
import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction
import com.simprints.clientapi.exceptions.InvalidUserIdException
import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.clientapi.exceptions.RootedDeviceException
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.fromSubjectToEnrolmentCreationEvent
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.libsimprints.Constants
import com.simprints.logging.Simber
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

abstract class RequestPresenter(
    private val view: RequestContract.RequestView,
    private val eventsManager: ClientApiSessionEventsManager,
    private val deviceManager: DeviceManager,
    private val encoder: EncodingUtils = EncodingUtilsImpl,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val sessionEventsManager: ClientApiSessionEventsManager
) : RequestContract.Presenter {

    override suspend fun processEnrolRequest() = validateAndSendRequest(
        EnrolBuilder(view.enrolExtractor, EnrolValidator(view.enrolExtractor))
    )

    override suspend fun processIdentifyRequest() = validateAndSendRequest(
        IdentifyBuilder(view.identifyExtractor, IdentifyValidator(view.identifyExtractor))
    )

    override suspend fun processVerifyRequest() = validateAndSendRequest(
        VerifyBuilder(view.verifyExtractor, VerifyValidator(view.verifyExtractor))
    )

    override suspend fun processConfirmIdentityRequest() = validateAndSendRequest(
        ConfirmIdentifyBuilder(
            view.confirmIdentityExtractor,
            ConfirmIdentityValidator(view.confirmIdentityExtractor)
        )
    )

    override suspend fun processEnrolLastBiometrics() = validateAndSendRequest(
        EnrolLastBiometricsBuilder(
            view.enrolLastBiometricsExtractor,
            EnrolLastBiometricsValidator(
                view.enrolLastBiometricsExtractor,
                eventsManager.getCurrentSessionId(),
                eventsManager.isCurrentSessionAnIdentificationOrEnrolment()
            )
        )
    )

    override suspend fun validateAndSendRequest(builder: ClientRequestBuilder) = try {
        val request = builder.build()
        addSuspiciousEventIfRequired(request)
        view.sendSimprintsRequest(request)
    } catch (exception: InvalidRequestException) {
        Simber.d(exception)
        logInvalidSessionInBackground()
        handleInvalidRequest(exception)
    }

    protected suspend fun runIfDeviceIsNotRooted(block: suspend () -> Unit) {
        try {
            deviceManager.checkIfDeviceIsRooted()
            block()
        } catch (ex: RootedDeviceException) {
            handleRootedDevice(ex)
        }
    }

    private fun handleRootedDevice(exception: RootedDeviceException) {
        Simber.e(exception)
        view.handleClientRequestError(ROOTED_DEVICE)
    }

    private fun handleInvalidRequest(exception: InvalidRequestException) {
        when (exception) {
            is InvalidMetadataException -> INVALID_METADATA
            is InvalidModuleIdException -> INVALID_MODULE_ID
            is InvalidProjectIdException -> INVALID_PROJECT_ID
            is InvalidSelectedIdException -> INVALID_SELECTED_ID
            is InvalidSessionIdException -> INVALID_SESSION_ID
            is InvalidUserIdException -> INVALID_USER_ID
            is InvalidVerifyIdException -> INVALID_VERIFY_ID
            is InvalidStateForIntentAction -> INVALID_STATE_FOR_INTENT_ACTION
        }.also {
            view.handleClientRequestError(it)
        }
    }

    private suspend fun addSuspiciousEventIfRequired(request: BaseRequest) {
        if (request.unknownExtras.isNotEmpty()) {
            eventsManager.addSuspiciousIntentEvent(request.unknownExtras)
        }
    }

    private suspend fun logInvalidSessionInBackground() {
        eventsManager.addInvalidIntentEvent(view.intentAction ?: "", view.extras ?: emptyMap())
    }

    /**
     * Be aware that Android Intents have a cap at around 500KB of data that can be returned.
     * When changing events, make sure they still fit in.
     */
    override suspend fun getEventsJsonForSession(
        sessionId: String,
        jsonHelper: JsonHelper
    ): String? =
        if (sharedPreferencesManager.canCoSyncData()) {
            val events = sessionEventsManager.getAllEventsForSession(sessionId).toList()
            jsonHelper.toJson(CoSyncEvents(events))
        } else {
            null
        }

    override suspend fun getEnrolmentCreationEventForSubject(
        subjectId: String,
        subjectRepository: SubjectRepository,
        timeHelper: ClientApiTimeHelper,
        jsonHelper: JsonHelper
    ): String? {
        if (!sharedPreferencesManager.canCoSyncData()) return null

        val recordCreationEvent =
            subjectRepository.load(
                SubjectQuery(
                    projectId = getProjectIdFromRequest(),
                    subjectId = subjectId
                )
            )
                .firstOrNull()
                ?.fromSubjectToEnrolmentCreationEvent(
                    now = timeHelper.now(),
                    modalities = sharedPreferencesManager.modalities,
                    encoder = encoder
                )
                ?: return null

        return jsonHelper.toJson(CoSyncEvents(listOf(recordCreationEvent)))
    }

    /**
     * Delete the events if returning to a cosync app but not Simprints
     */
    override suspend fun deleteSessionEventsIfNeeded(sessionId: String) {
        if (sharedPreferencesManager.canCoSyncData() &&
            !sharedPreferencesManager.canSyncDataToSimprints()
        ) {
            sessionEventsManager.deleteSessionEvents(sessionId)
        }
    }

    override fun getProjectIdFromRequest() =
        view.extras?.get(Constants.SIMPRINTS_PROJECT_ID) as String

    @Keep
    private data class CoSyncEvents(val events: List<Event>)
}
