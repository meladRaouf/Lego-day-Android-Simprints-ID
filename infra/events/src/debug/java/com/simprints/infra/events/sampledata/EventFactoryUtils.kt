package com.simprints.infra.events.sampledata

import android.os.Build
import com.simprints.core.domain.fingerprint.IFingerIdentifier.LEFT_THUMB
import com.simprints.core.domain.response.AppMatchConfidence.MEDIUM
import com.simprints.core.domain.response.AppResponseTier.TIER_1
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.event.domain.models.*
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult.FOUND
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.BatteryInfo
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Vero2Version
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.Location
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.domain.models.session.SessionScopePayload
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2

fun createSessionScope(
    id: String = GUID1,
    createdAt: Timestamp = CREATED_AT,
    projectId: String = DEFAULT_PROJECT_ID,
    isClosed: Boolean = false,
): SessionScope {

    val appVersionNameArg = "appVersionName"
    val libSimprintsVersionNameArg = "libSimprintsVersionName"
    val languageArg = "language"
    val deviceArg = Device(
        Build.VERSION.SDK_INT.toString(),
        Build.MANUFACTURER + "_" + Build.MODEL,
        GUID1
    )

    val databaseInfoArg = DatabaseInfo(2, 2)
    val locationArg = Location(0.0, 0.0)

    return SessionScope(
        id = id,
        projectId = projectId,
        createdAt = createdAt,
        endedAt = ENDED_AT.takeIf { isClosed },
        payload = SessionScopePayload(
            sidVersion = appVersionNameArg,
            libSimprintsVersion = libSimprintsVersionNameArg,
            language = languageArg,
            modalities = listOf(Modality.FINGERPRINT, Modality.FACE),
            device = deviceArg,
            databaseInfo = databaseInfoArg,
            location = locationArg,
            projectConfigurationUpdatedAt = "projectConfigurationUpdatedAt"
        )
    )
}

fun createEventWithSessionId(eventId: String, sessionId: String): Event = AlertScreenEvent(
    id = eventId,
    payload = AlertScreenEvent.AlertScreenPayload(
        CREATED_AT, AlertScreenEvent.EVENT_VERSION,
        AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
    ),
    type = EventType.ALERT_SCREEN,
    sessionId = sessionId,
)

const val FACE_TEMPLATE_FORMAT = "RANK_ONE_1_23"

fun createConfirmationCallbackEvent() = ConfirmationCallbackEvent(
    CREATED_AT,
    true
)

fun createEnrolmentCallbackEvent() = EnrolmentCallbackEvent(
    CREATED_AT,
    GUID1
)

fun createErrorCallbackEvent() = ErrorCallbackEvent(
    CREATED_AT,
    DIFFERENT_PROJECT_ID_SIGNED_IN
)

fun createIdentificationCallbackEvent() = IdentificationCallbackEvent(
    CREATED_AT,
    GUID1,
    listOf(CallbackComparisonScore(GUID1, 1, TIER_1, MEDIUM))
)

fun createRefusalCallbackEvent() = RefusalCallbackEvent(
    CREATED_AT,
    "some_reason",
    "extra"
)

fun createVerificationCallbackEventV1() = VerificationCallbackEvent(
    CREATED_AT,
    CallbackComparisonScore(GUID1, 1, TIER_1, MEDIUM)
)

fun createVerificationCallbackEventV2() = VerificationCallbackEvent(
    CREATED_AT,
    CallbackComparisonScore(GUID1, 1, TIER_1, MEDIUM)
)

fun createConfirmationCalloutEvent() = ConfirmationCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    GUID1,
    GUID2
)

fun createEnrolmentCalloutEvent(projectId: String = DEFAULT_PROJECT_ID) = EnrolmentCalloutEvent(
    CREATED_AT,
    projectId,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    projectId
)

fun createIdentificationCalloutEvent() = IdentificationCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
)

fun createLastBiometricsEnrolmentCalloutEvent() = EnrolmentLastBiometricsCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    GUID2,
)

fun createVerificationCalloutEvent() = VerificationCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    GUID2,
)

fun createFaceCaptureBiometricsEvent() = FaceCaptureBiometricsEvent(
    CREATED_AT,
    FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
        yaw = 1.0f,
        roll = 0.0f,
        template = "template",
        quality = 1.0f,
        format = FACE_TEMPLATE_FORMAT
    ),
)

fun createFaceCaptureConfirmationEvent() = FaceCaptureConfirmationEvent(
    CREATED_AT,
    ENDED_AT,
    CONTINUE
)

fun createFaceCaptureEvent() = FaceCaptureEvent(
    startTime = CREATED_AT,
    endTime = ENDED_AT,
    attemptNb = 0,
    qualityThreshold = 1F,
    result = FaceCaptureEvent.FaceCapturePayload.Result.VALID,
    isFallback = true,
    face = FaceCaptureEvent.FaceCapturePayload.Face(0F, 1F, 2F, FACE_TEMPLATE_FORMAT),
)

fun createFaceFallbackCaptureEvent() = FaceFallbackCaptureEvent(
    CREATED_AT,
    ENDED_AT
)

fun createFaceOnboardingCompleteEvent() = FaceOnboardingCompleteEvent(
    CREATED_AT,
    ENDED_AT
)

fun createAlertScreenEvent() = AlertScreenEvent(
    CREATED_AT,
    BLUETOOTH_NOT_ENABLED
)

fun createAuthenticationEvent() = AuthenticationEvent(
    CREATED_AT,
    ENDED_AT,
    UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID),
    AUTHENTICATED,
)

fun createAuthorizationEvent() = AuthorizationEvent(
    CREATED_AT,
    AUTHORIZED,
    AuthorizationPayload.UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID),
)

fun createCandidateReadEvent() = CandidateReadEvent(
    CREATED_AT,
    ENDED_AT,
    GUID1,
    FOUND,
    NOT_FOUND
)

fun createCompletionCheckEvent() = CompletionCheckEvent(
    CREATED_AT,
    true
)

fun createConnectivitySnapshotEvent() = ConnectivitySnapshotEvent(
    CREATED_AT,
    listOf(
        Connection(
            SimNetworkUtils.ConnectionType.MOBILE,
            SimNetworkUtils.ConnectionState.CONNECTED
        )
    ),
)

fun createConsentEvent() = ConsentEvent(
    CREATED_AT,
    ENDED_AT,
    INDIVIDUAL,
    ACCEPTED
)

fun createEnrolmentEventV2() = EnrolmentEventV2(
    CREATED_AT,
    GUID1,
    DEFAULT_PROJECT_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_USER_ID,
    GUID2,
)

fun createEnrolmentEventV1() = EnrolmentEventV1(
    CREATED_AT,
    GUID1
)

fun createFingerprintCaptureEvent() = FingerprintCaptureEvent(
    createdAt = CREATED_AT,
    endTime = ENDED_AT,
    finger = LEFT_THUMB,
    qualityThreshold = 10,
    result = FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
    fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
        LEFT_THUMB,
        8,
        "ISO_19794_2"
    ),
    payloadId = "payloadId"
)

fun createFingerprintCaptureBiometricsEvent() = FingerprintCaptureBiometricsEvent(
    createdAt = CREATED_AT,
    fingerprint = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
        LEFT_THUMB,
        "sometemplate",
        10,
        "ISO_19794_2"
    ),
    payloadId = "payloadId"
)

fun createGuidSelectionEvent() = GuidSelectionEvent(CREATED_AT, GUID1)

fun createIntentParsingEvent() = IntentParsingEvent(CREATED_AT, COMMCARE)

fun createInvalidIntentEvent() = InvalidIntentEvent(
    CREATED_AT,
    "action",
    mapOf("extra_key" to "extra_value")
)

fun createOneToManyMatchEvent() = OneToManyMatchEvent(
    CREATED_AT,
    ENDED_AT,
    MatchPool(PROJECT, 100),
    "RANK_ONE",
    listOf(MatchEntry(GUID1, 0F))
)

fun createOneToOneMatchEvent() = OneToOneMatchEvent(
    CREATED_AT,
    ENDED_AT,
    GUID1,
    "SIM_AFIS",
    MatchEntry(GUID1, 10F),
    FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX,
)

fun createPersonCreationEvent() = PersonCreationEvent(
    CREATED_AT,
    listOf(GUID1, GUID2),
    GUID1,
    listOf(GUID1, GUID2),
    GUID2,
)

fun createRefusalEvent() = RefusalEvent(
    CREATED_AT,
    ENDED_AT,
    OTHER,
    "other_text"
)

fun createScannerConnectionEvent() = ScannerConnectionEvent(
    CREATED_AT,
    ScannerInfo("scanner_id", "macaddress", VERO_1, "version"),
)

fun createScannerFirmwareUpdateEvent() = ScannerFirmwareUpdateEvent(
    CREATED_AT,
    ENDED_AT,
    "chip",
    "targetAppVersion",
    "error",
)

fun createSuspiciousIntentEvent() = SuspiciousIntentEvent(
    CREATED_AT,
    mapOf("extra_key" to "extra_value")
)

fun createVero2InfoSnapshotEvent() = Vero2InfoSnapshotEvent(
    CREATED_AT,
    version = Vero2Version.Vero2NewApiVersion("E-1", "cypressApp", "stmApp", "un20App"),
    battery = BatteryInfo(0, 1, 2, 3)
)
