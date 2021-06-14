package com.simprints.eventsystem.sampledata

import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT

object ScannerConnectionEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): ScannerConnectionEvent {
        val scannerInfoArg = ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo(
            "scanner_id",
            "mac_address",
            ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1,
            "hardware_version"
        )
        return ScannerConnectionEvent(CREATED_AT, scannerInfoArg, labels)
    }
}