package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import com.example.mockscanner.MockBluetoothAdapter
import com.example.mockscanner.MockFinger
import com.example.mockscanner.MockScannerManager
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.MockRule
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.testTools.log
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.remoteadminclient.ApiException
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyWorkflowAllMainFeatures : DaggerForAndroidTests(), FirstUseLocal, HappyWifi {

    private val calloutCredentials = CalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")

    private val realmKey = Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", Base64.NO_WRAP)
    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        realmKey,
        calloutCredentials.legacyApiKey)

    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val enrolTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Rule
    @JvmField
    val identifyTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Rule
    @JvmField
    val verifyTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject
    lateinit var remoteDbManager: RemoteDbManager
    @Inject
    lateinit var randomGeneratorMock: RandomGenerator

    override var module: AppModuleForAndroidTests by lazyVar {
        object : AppModuleForAndroidTests(app, randomGeneratorRule = MockRule.MOCK) {
            override fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter = mockScannerManager
        }
    }

    var mockScannerManager = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
        *MockFinger.person1TwoFingersGoodScan(),
        *MockFinger.person1TwoFingersAgainGoodScan(),
        *MockFinger.person1TwoFingersAgainGoodScan())))

    @Before
    @Throws(ApiException::class)
    override fun setUp() {
        log("bucket01.HappyWorkflowAllMainFeatures.setUp()")
        super<HappyWifi>.setUp()
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(realmKey, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        super<FirstUseLocal>.setUp()

        signOut()
    }

    @Test
    fun happyWorkflowAllMainFeatures() {
        log("bucket01.HappyWorkflowAllMainFeatures.happyWorkflowAllMainFeatures")

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, enrolTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        // Once signed in proceed to enrol workflow
        fullHappyWorkflow()
        mainActivityEnrolmentCheckFinished(enrolTestRule)
        val guid = enrolmentReturnedResult(enrolTestRule)

        // Launch app and do an identification workflow
        launchActivityIdentify(calloutCredentials, identifyTestRule)
        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(identifyTestRule)
        guidIsTheOnlyReturnedIdentification(identifyTestRule, guid)

        // Launch app and do a verification workflow
        launchActivityVerify(calloutCredentials, verifyTestRule, guid)
        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(verifyTestRule)
        verificationSuccessful(verifyTestRule, guid)
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
