package com.simprints.clientapi.clientrequests.validators

import com.google.gson.Gson
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException


abstract class ClientRequestValidator(private val extractor: ClientRequestExtractor) {

    open fun validateClientRequest() {
        validateProjectId()
        validateUserId()
        validateModuleId()
        validateMetadata()
    }

    protected open fun validateProjectId() {
        if (extractor.getProjectId().isBlank())
            throw InvalidProjectIdException("Missing Project ID")
    }

    protected open fun validateUserId() {
        if (extractor.getUserId().isBlank())
            throw InvalidUserIdException("Missing User ID")
    }

    protected open fun validateModuleId() {
        if (extractor.getModuleId().isBlank())
            throw InvalidModuleIdException("Missing Module ID")
        else if (extractor.getModuleId().contains("|"))
            throw InvalidModuleIdException("Illegal Module ID")
    }

    protected open fun validateMetadata() {
        if (!extractor.getMetadata().isBlank())
            if (!hasValidMetadata())
                throw InvalidMetadataException("Invalid Metadata")
    }

    // TODO: inject gson dependency
    private fun hasValidMetadata(): Boolean = try {
        Gson().fromJson(extractor.getMetadata(), Any::class.java)
        true
    } catch (ex: com.google.gson.JsonSyntaxException) {
        false
    }

}

