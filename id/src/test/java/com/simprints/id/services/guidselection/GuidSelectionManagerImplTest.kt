package com.simprints.id.services.guidselection

import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.models.GuidSelectionEvent
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_DEVICE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GuidSelectionManagerImplTest {

    private lateinit var guidSelectionManager: GuidSelectionManager

    private val guidSelectionRequest = GuidSelectionRequest(DEFAULT_PROJECT_ID, GUID1, GUID2)

    @MockK private lateinit var loginInfoManager: LoginInfoManager
    @MockK private lateinit var timerHelper: TimeHelper
    @MockK private lateinit var eventRepository: com.simprints.eventsystem.event.EventRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        guidSelectionManager = GuidSelectionManagerImpl(DEFAULT_DEVICE_ID, loginInfoManager, timerHelper, eventRepository)
        every { timerHelper.now() } returns CREATED_AT
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }

    @Test
    fun handleConfirmIdentityRequest_shouldCheckProjectId() {
        runBlocking {
            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            verify(exactly = 1) { loginInfoManager.isProjectIdSignedIn(DEFAULT_PROJECT_ID) }
        }
    }

    @Test
    fun handleConfirmIdentityRequest_shouldAddAnEvent() {
        runBlocking {
            every { loginInfoManager.isProjectIdSignedIn(any()) } returns true

            guidSelectionManager.handleConfirmIdentityRequest(guidSelectionRequest)

            coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(any<GuidSelectionEvent>()) }
        }
    }

}
