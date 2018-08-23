package com.simprints.id.activities

import com.google.firebase.FirebaseApp
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.domain.consent.ParentalConsent
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createRoboLaunchActivity
import com.simprints.id.tools.delegates.lazyVar
import junit.framework.Assert
import kotlinx.android.synthetic.main.activity_launch.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class LaunchActivityTest : RxJavaTest, DaggerForTests() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var settingsPreferencesManager: SettingsPreferencesManager
    @Inject lateinit var remoteDbManagerMock: RemoteDbManager

    override var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests(
            settingsPreferencesManagerRule = MockRule
        )
    }

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            dbManagerRule = MockRule,
            scheduledPeopleSyncManagerRule = MockRule,
            scheduledSessionsSyncManagerRule = MockRule)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
    }

    @Test
    fun enrollmentCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockSettingsPreferencesManager(parentalConsentExists = false)

        val calloutAction = CalloutAction.REGISTER
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        Assert.assertEquals("", parentConsentText)
    }

    @Test
    fun identifyCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockSettingsPreferencesManager(parentalConsentExists = false)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        Assert.assertEquals("", parentConsentText)
    }

    @Test
    fun enrollmentCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockSettingsPreferencesManager(parentalConsentExists = true)

        val calloutAction = CalloutAction.REGISTER
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        val defaultParentalConsentText = ParentalConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultParentalConsentText, parentConsentText)
    }

    @Test
    fun identifyCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockSettingsPreferencesManager(parentalConsentExists = true)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        val defaultParentalConsentText = ParentalConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultParentalConsentText, parentConsentText)
    }

    @Test
    fun malformedConsentJson_showsDefaultConsent() {
        mockSettingsPreferencesManager(generalConsentOptions = MALFORMED_CONSENT_OPTIONS)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(defaultGeneralConsentText, generalConsentText)
    }

    @Test
    fun extraUnrecognisedConsentOptions_stillShowsCorrectValues() {
        mockSettingsPreferencesManager(generalConsentOptions = EXTRA_UNRECOGNISED_CONSENT_OPTIONS)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val targetConsentText = EXTRA_UNRECOGNISED_CONSENT_TARGET.assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(targetConsentText, generalConsentText)
    }

    @Test
    fun partiallyMissingConsentOptions_stillShowsCorrectValues() {
        mockSettingsPreferencesManager(generalConsentOptions = PARTIALLY_MISSING_CONSENT_OPTIONS)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val targetConsentText = PARTIALLY_MISSING_CONSENT_TARGET.assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        Assert.assertEquals(targetConsentText, generalConsentText)
    }

    private fun mockSettingsPreferencesManager(parentalConsentExists: Boolean = false,
                                               generalConsentOptions: String = REMOTE_CONSENT_GENERAL_OPTIONS,
                                               parentalConsentOptions: String = REMOTE_CONSENT_PARENTAL_OPTIONS) {

        whenever(settingsPreferencesManager.language).thenReturn(LANGUAGE)
        whenever(settingsPreferencesManager.programName).thenReturn(PROGRAM_NAME)
        whenever(settingsPreferencesManager.organizationName).thenReturn(ORGANIZATION_NAME)

        whenever(settingsPreferencesManager.parentalConsentExists).thenReturn(parentalConsentExists)
        whenever(settingsPreferencesManager.generalConsentOptionsJson).thenReturn(generalConsentOptions)
        whenever(settingsPreferencesManager.parentalConsentOptionsJson).thenReturn(parentalConsentOptions)
    }

    companion object {
        private const val LANGUAGE = "en"
        private const val PROGRAM_NAME = "PROGRAM NAME"
        private const val ORGANIZATION_NAME = "ORGANIZATION NAME"
        private const val REMOTE_CONSENT_GENERAL_OPTIONS = "{\"consent_enrol_only\":false,\"consent_enrol\":true,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true}"
        private const val REMOTE_CONSENT_PARENTAL_OPTIONS = "{\"consent_parent_enrol_only\":false,\"consent_parent_enrol\":true,\"consent_parent_id_verify\":true,\"consent_parent_share_data_no\":true,\"consent_parent_share_data_yes\":false,\"consent_parent_collect_yes\":false,\"consent_parent_privacy_rights\":true,\"consent_parent_confirmation\":true}"

        private const val MALFORMED_CONSENT_OPTIONS = "gibberish{\"000}\"\""

        private const val EXTRA_UNRECOGNISED_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true,\"this_one_doesnt_exist\":true}"
        private val EXTRA_UNRECOGNISED_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)

        private const val PARTIALLY_MISSING_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false}"
        private val PARTIALLY_MISSING_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)
    }
}