package com.simprints.infra.eventsync.exceptions

import com.simprints.core.exceptions.UnexpectedException

internal class MalformedSyncOperationException(
    message: String = "People sync operation is malformed as input for worker"
): UnexpectedException(message)
