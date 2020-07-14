package com.simprints.id.services.guidselection

import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest

interface GuidSelectionManager {
    suspend fun handleConfirmIdentityRequest(request: GuidSelectionRequest)
}
