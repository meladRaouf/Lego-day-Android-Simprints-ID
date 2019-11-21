package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ConsentViewModelTest {

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()
    }

    @Test
    fun givenAppRequest_shouldReturnCorrectGeneralConsentText() {
        val generalConsentText = "general_consent"
        val generalConsentTextLiveData = MutableLiveData(generalConsentText)
        val consentTextManagerMock = mock<ConsentRepository>().apply {
            whenever(this) { getGeneralConsentText(any()) } thenReturn generalConsentTextLiveData
        }
        val consentViewModel = ConsentViewModel(buildAskConsentRequest(), consentTextManagerMock, mock())

        val generalConsentTestObserver = consentViewModel.generalConsentText.testObserver()

        assertThat(generalConsentTestObserver.observedValues.contains(generalConsentText)).isTrue()
    }

    @Test
    fun givenAppRequest_shouldReturnCorrectParentalConsentText() {
        val parentalConsentText = "parental_consent"
        val parentalConsentTextLiveData = MutableLiveData(parentalConsentText)
        val consentTextManagerMock = mock<ConsentRepository>().apply {
            whenever(this) { getParentalConsentText(any()) } thenReturn parentalConsentTextLiveData
            whenever(this) { parentalConsentExists() } thenReturn MutableLiveData(true)
        }
        val consentViewModel = ConsentViewModel(buildAskConsentRequest(), consentTextManagerMock, mock())

        val parentalConsentTestObserver = consentViewModel.parentalConsentText.testObserver()
        val parentalConsentExistsObserver = consentViewModel.parentalConsentExists.testObserver()

        assertThat(parentalConsentExistsObserver.observedValues.contains(true)).isTrue()
        assertThat(parentalConsentTestObserver.observedValues.contains(parentalConsentText)).isTrue()
    }

    private fun buildAskConsentRequest() =
        AskConsentRequest(ConsentType.ENROL)
}