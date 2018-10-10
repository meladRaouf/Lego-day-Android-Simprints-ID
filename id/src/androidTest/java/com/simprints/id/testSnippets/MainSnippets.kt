package com.simprints.id.testSnippets

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.testTools.*
import com.simprints.id.testTools.StringUtils.getResourceString
import com.simprints.libsimprints.*
import org.hamcrest.Matchers.*
import org.junit.Assert.*

fun launchActivityEnrol(calloutCredentials: CalloutCredentials,
                        enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchActivityEnrol")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_REGISTER_INTENT, enrolTestRule)
}

fun launchActivityIdentify(calloutCredentials: CalloutCredentials,
                           identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchActivityIdentify")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_IDENTIFY_INTENT, identifyTestRule)
}

fun launchActivityVerify(calloutCredentials: CalloutCredentials,
                         verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                         guid: String?) {
    log("launchActivityVerify")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_VERIFY_INTENT, verifyTestRule,
        verifyGuidExtra = guid)
}

fun fullHappyWorkflow(numberOfScans: Int = 2, dialogResult: String = "✓ LEFT THUMB\n✓ LEFT INDEX FINGER\n") {
    log("fullHappyWorkflow")
    setupActivityAndContinue()

    (0 until numberOfScans).forEach { collectFingerprintsPressScan() }

    checkIfDialogIsDisplayedWithResultAndClickConfirm(dialogResult)
}

fun setupActivityAndContinue() {
    log("setupActivityAndContinue")
    setupActivity()
    setupActivityContinue()
}

fun setupActivityAndDecline() {
    log("setupActivityAndDecline")
    setupActivity()
    setupActivityDecline()
}

fun setupActivity() {
    log("setupActivity")
    tryOnUiUntilTimeout(10000, 50) {
        ActivityUtils.grantPermissions()
        onView(withId(R.id.generalConsentTextView))
            .check(matches(isDisplayed()))
    }
}

private fun setupActivityContinue() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(15000, 500) {
        onView(withId(R.id.consentAcceptButton))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun setupActivityDecline() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(12000, 500) {
        onView(withId(R.id.consentDeclineButton))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun collectFingerprintsPressScan() {
    log("collectFingerprintsPressScan")
    tryOnUiUntilTimeout(10000, 200) {
        onView(withId(R.id.scan_button))
            .check(matches(not(withText(R.string.cancel_button))))
            .perform(click())
    }
    Thread.sleep(500) //Wait for ViewPager animation
}

fun skipFinger() {
    log("skipFinger")
    tryOnUiUntilTimeout(10000, 200) {
        onView(withId(R.id.missingFingerText))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun waitForSplashScreenAppearsAndDisappears() {
    log("checkSplashScreen")
    tryOnUiUntilTimeout(10000, 200) {
        onView(withId(R.id.splashGetReady))
            .check(matches(isDisplayed()))
    }

    tryOnUiUntilTimeout(10000, 200) {
        onView(withId(R.id.splashGetReady))
            .check(doesNotExist())
    }

    waitOnUi(2000)
}

fun checkIfDialogIsDisplayedWithResultAndClickConfirm(dialogResult: String = "✓ LEFT THUMB\n✓ LEFT INDEX FINGER\n") {
    tryOnUiUntilTimeout(1000, 50) {
        onView(withText(getResourceString(R.string.confirm_fingers_dialog_title)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.message))
            .inRoot(isDialog())
            .check(matches(withText(dialogResult)))
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
    }
}

fun collectFingerprintsEnrolmentCheckFinished(enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("collectFingerprintsEnrolmentCheckFinished")
    tryOnSystemUntilTimeout(5000, 500) {
        assertTrue(enrolTestRule.activity.isDestroyed)
    }
}

fun enrolmentReturnedResult(enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>): String {
    log("enrolmentReturnedResult")
    val registration = enrolTestRule.activityResult
        .resultData.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
    val guid = registration.guid
    assertNotNull(guid)
    return guid
}

fun matchingActivityIdentificationCheckFinished(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityIdentificationCheckFinished")
    tryOnSystemUntilTimeout(20000, 500) {
        assertTrue(identifyTestRule.activity.isDestroyed)
    }
}

fun guidIsTheOnlyReturnedIdentification(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("guidIsTheOnlyReturnedIdentification")
    val identifications = identifyTestRule.activityResult
        .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    assertEquals(1, identifications.size.toLong())
    assertEquals(guid, identifications[0].guid)
    assertTrue(identifications[0].confidence > 0)
    assertNotEquals(Tier.TIER_5, identifications[0].tier)
}

fun twoReturnedIdentificationsOneMatchOneNotMatch(identifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                                            matchGuid: String,
                                            notMatchGuid: String) {
    log("twoReturnedIdentificationsOneMatchOneNotMatch")
    val identifications = identifyTestRule.activityResult
        .resultData.getParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
    assertEquals(2, identifications.size.toLong())

    assertEquals(matchGuid, identifications[0].guid)
    assertTrue(identifications[0].confidence > 0)
    assertNotEquals(Tier.TIER_5, identifications[0].tier)

    assertEquals(notMatchGuid, identifications[1].guid)
    assertTrue(identifications[1].confidence > 0)
    assertEquals(Tier.TIER_5, identifications[1].tier)
}

fun matchingActivityVerificationCheckFinished(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("matchingActivityVerificationCheckFinished")
    tryOnSystemUntilTimeout(5000, 500) {
        assertTrue(verifyTestRule.activity.isDestroyed)
    }
}

fun verificationSuccessful(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("verificationSuccessful")
    val verification = verifyTestRule.activityResult
        .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    assertEquals(guid, verification.guid)
    assertTrue(verification.confidence > 0)
    assertNotEquals(Tier.TIER_5, verification.tier)
}

fun verificationNotAMatch(verifyTestRule: ActivityTestRule<CheckLoginFromIntentActivity>, guid: String) {
    log("verificationSuccessful")
    val verification = verifyTestRule.activityResult
        .resultData.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
    assertEquals(guid, verification.guid)
    assertTrue(verification.confidence > 0)
    assertEquals(Tier.TIER_5, verification.tier)
}
