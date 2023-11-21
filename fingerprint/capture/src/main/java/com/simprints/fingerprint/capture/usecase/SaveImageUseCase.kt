package com.simprints.fingerprint.capture.usecase

import com.simprints.fingerprint.capture.exceptions.FingerprintUnexpectedException
import com.simprints.fingerprint.capture.extensions.deduceFileExtension
import com.simprints.fingerprint.capture.models.Path
import com.simprints.fingerprint.capture.models.SecuredImageRef
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import com.simprints.infra.images.model.Path as DomainPath

internal class SaveImageUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val coreEventRepository: EventRepository,
) {

    suspend operator fun invoke(
        configuration: FingerprintConfiguration,
        captureEventId: String?,
        collectedFinger: CaptureState.Collected,
    ) = if (collectedFinger.scanResult.image != null && captureEventId != null) {
        saveImage(
            collectedFinger.scanResult.image,
            captureEventId,
            configuration.vero2!!.imageSavingStrategy.deduceFileExtension()
        )
    } else if (collectedFinger.scanResult.image != null && captureEventId == null) {
        Simber.e(FingerprintUnexpectedException("Could not save fingerprint image because of null capture ID"))
        null
    } else null

    private suspend fun saveImage(
        imageBytes: ByteArray,
        captureEventId: String,
        fileExtension: String
    ): SecuredImageRef? = determinePath(captureEventId, fileExtension)?.let { path ->
        Simber.d("Saving fingerprint image ${path}")
        val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
        val projectId = currentSession.payload.projectId

        val securedImageRef = coreImageRepository.storeImageSecurely(imageBytes, projectId, DomainPath(path.parts))

        if (securedImageRef != null) {
            SecuredImageRef(Path(securedImageRef.relativePath.parts))
        } else {
            Simber.e("Saving image failed for captureId $captureEventId")
            null
        }
    }

    private suspend fun determinePath(captureEventId: String, fileExtension: String): Path? =
        try {
            val currentSession = coreEventRepository.getCurrentCaptureSessionEvent()
            val sessionId = currentSession.id
            Path(
                arrayOf(
                    SESSIONS_PATH,
                    sessionId,
                    FINGERPRINTS_PATH,
                    "$captureEventId.$fileExtension"
                )
            )
        } catch (t: Throwable) {
            Simber.e(t)
            null
        }

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}