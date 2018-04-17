package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.session.callout.Callout

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun openLoginActivity(legacyApiKey: String)
        fun openLaunchActivity()

        fun checkCallingAppIsFromKnownSource()
        fun parseCallout(): Callout
        fun finishCheckLoginFromIntentActivity()
    }

    interface Presenter : BasePresenter {
        fun setup()
    }
}