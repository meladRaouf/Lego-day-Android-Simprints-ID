package com.simprints.id.data.analytics.eventData.controllers.domain

import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventData.models.domain.events.ScannerConnectionEvent
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single

interface SessionEventsManager: SessionEventsLocalDbManager {

    fun createSession(): Single<SessionEvents>
    fun getCurrentSession(): Single<SessionEvents>

    fun updateSession(block: (sessionEvents: SessionEvents) -> Unit): Completable
    fun updateSessionInBackground(block: (sessionEvents: SessionEvents) -> Unit)

    fun addGuidSelectionEventToLastIdentificationIfExists(selectedGuid: String, sessionId: String): Completable
    fun addPersonCreationEventInBackground(person: Person)
    fun addOneToOneMatchEventInBackground(patientId: String, startTimeVerification: Long, match: Verification?)
    fun addOneToManyEventInBackground(startTimeIdentification: Long, matches: List<Identification>, matchSize: Int)
    fun addEventForScannerConnectivityInBackground(scannerInfo: ScannerConnectionEvent.ScannerInfo)
    fun updateHardwareVersionInScannerConnectivityEvent(hardwareVersion: String)
    fun addLocationToSession(latitude: Double, longitude: Double)
    fun addEventForCandidateReadInBackground(guid: String, startCandidateSearchTime: Long, localResult: CandidateReadEvent.LocalResult, remoteResult: CandidateReadEvent.RemoteResult?)
}
