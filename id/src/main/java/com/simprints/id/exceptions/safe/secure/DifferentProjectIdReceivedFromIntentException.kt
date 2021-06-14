package com.simprints.id.exceptions.safe.secure

import com.simprints.core.exceptions.SafeException


class DifferentProjectIdReceivedFromIntentException(message: String = "DifferentProjectIdReceivedFromIntentException")
    : SafeException(message) {

    companion object {
        fun withProjectIds(expected: String, received: String) = DifferentProjectIdReceivedFromIntentException(
            "DifferentProjectIdReceivedFromIntentException: expected = $expected , received = $received")
    }
}
