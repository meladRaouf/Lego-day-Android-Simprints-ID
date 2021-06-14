package com.simprints.eventsystem.event_sync.up.local

import com.google.common.truth.Truth
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.eventsystem.events_sync.up.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.up.local.DbEventsUpSyncOperationState.Companion.buildFromEventsUpSyncOperationState
import com.simprints.eventsystem.sampledata.SampleDefaults.TIME1
import com.simprints.eventsystem.sampledata.SampleDefaults.projectUpSyncScope
import org.junit.Test

class DbEventsUpSyncOperationStateTest {

    @Test
    fun buildDbState_fromEventUpSyncOperation() {
        val op = projectUpSyncScope.operation.copy(lastSyncTime = TIME1, lastState = COMPLETE)
        val downSyncState = buildFromEventsUpSyncOperationState(op)

        with(downSyncState, {
            Truth.assertThat(id).isEqualTo(op.getUniqueKey())
            Truth.assertThat(lastState).isEqualTo(COMPLETE)
            Truth.assertThat(lastUpdatedTime).isEqualTo(TIME1)
        })
    }
}