package com.simprints.id.service

import androidx.test.InstrumentationRegistry
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.events.GuidSelectionEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.shared.DependencyRule
import com.simprints.id.testSnippets.launchActivityEnrol
import com.simprints.id.testSnippets.setupLoginInfoToBeSignedIn
import com.simprints.id.testTools.ActivityUtils
import com.simprints.id.testTools.tryOnSystemUntilTimeout
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libsimprints.SimHelper
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class GuidSelectionServiceTest : DaggerForAndroidTests() {

    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var loginInfoManagerSpy: LoginInfoManager

    @Rule @JvmField val scanTestRule = ActivityUtils.checkLoginFromIntentActivityTestRule()

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            sessionEventsManagerRule = DependencyRule.SpyRule,
            analyticsManagerRule = DependencyRule.MockRule,
            loginInfoManagerRule = DependencyRule.MockRule)
    }

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super.setUp()
        testAppComponent.inject(this)

        setupLoginInfoToBeSignedIn(loginInfoManagerSpy, DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId, DEFAULT_TEST_CALLOUT_CREDENTIALS.userId)

        app.initDependencies()
    }

    @Test
    fun testWithStartedService() {
        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, scanTestRule)
        var session = sessionEventsManagerSpy.createSession().blockingGet()

        sessionEventsManagerSpy.updateSession {
            it.projectId = loginInfoManagerSpy.getSignedInProjectIdOrEmpty()
        }.blockingGet()

        val simHelper = SimHelper(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId, DEFAULT_TEST_CALLOUT_CREDENTIALS.userId)
        simHelper.confirmIdentity(app, session.id, "some_guid_confirmed")

        tryOnSystemUntilTimeout(10000, 500) {
            val potentialSessionWithGUIDEvent = sessionEventsManagerSpy.getCurrentSession().blockingGet()
            if (potentialSessionWithGUIDEvent.events.findLast { it is GuidSelectionEvent } == null) {
                throw Exception("GuidSelectionEvent not generated yet. Still waiting for it.")
            }
        }

        scanTestRule.activity.runOnUiThread {
            session = sessionEventsManagerSpy.loadSessionById(session.id).blockingGet()
            val potentialGuidSelectionEvent = session.events.filterIsInstance(GuidSelectionEvent::class.java).first()
            Assert.assertNotNull(potentialGuidSelectionEvent)
            Assert.assertEquals(potentialGuidSelectionEvent.selectedId, "some_guid_confirmed")
        }
    }
}
