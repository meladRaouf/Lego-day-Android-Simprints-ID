package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.events.event.domain.models.face.FaceTemplateFormat
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.infra.events.event.domain.models.subject.BiometricReferenceType.Companion.FACE_REFERENCE_KEY
import com.simprints.infra.events.event.domain.models.subject.BiometricReferenceType.Companion.FINGERPRINT_REFERENCE_KEY
import com.simprints.infra.events.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.events.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.infra.events.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.infra.events.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = FaceReference::class, name = FACE_REFERENCE_KEY),
    JsonSubTypes.Type(value = FingerprintReference::class, name = FINGERPRINT_REFERENCE_KEY)
)
@Keep
sealed class BiometricReference(open val id: String,
                                val type: BiometricReferenceType)

data class FaceReference(override val id: String,
                         val templates: List<FaceTemplate>,
                         val format: FaceTemplateFormat = FaceTemplateFormat.RANK_ONE_1_23,
                         val metadata: HashMap<String, String>? = null) : BiometricReference(id, BiometricReferenceType.FACE_REFERENCE)

data class FingerprintReference(override val id: String,
                                val templates: List<FingerprintTemplate>,
                                val format: FingerprintTemplateFormat = FingerprintTemplateFormat.ISO_19794_2,
                                val metadata: HashMap<String, String>? = null) : BiometricReference(id, BiometricReferenceType.FINGERPRINT_REFERENCE)

enum class BiometricReferenceType {

    // a constant key is required to serialise/deserialize
    // BiometricReference correctly with Jackson (see annotation in BiometricReference).
    // Add a key in the companion object for each enum value

    /* key added: FACE_REFERENCE */
    FACE_REFERENCE,

    /* key added: FINGERPRINT_REFERENCE */
    FINGERPRINT_REFERENCE;

    companion object {
        const val FACE_REFERENCE_KEY = "FACE_REFERENCE"
        const val FINGERPRINT_REFERENCE_KEY = "FINGERPRINT_REFERENCE"
    }
}

fun ApiBiometricReference.fromApiToDomain() = when (this.type) {
    ApiBiometricReferenceType.FaceReference -> (this as ApiFaceReference).fromApiToDomain()
    ApiBiometricReferenceType.FingerprintReference -> (this as ApiFingerprintReference).fromApiToDomain()
}

fun ApiFaceReference.fromApiToDomain() = FaceReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

fun ApiFingerprintReference.fromApiToDomain() = FingerprintReference(id, templates.map { it.fromApiToDomain() }, format, metadata)