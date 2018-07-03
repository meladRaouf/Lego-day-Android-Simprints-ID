package com.simprints.id

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.testTools.WaitingUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@SmallTest
class ApplicationTest {

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromMainLauncherActivity::class.java, false, true)

    @Test
    fun rxJavaUndeliverableExceptionHappens_shouldBeHandled() {

        val observable1 = Observable.create<Int> {
            Thread.sleep(100)
            it.onError(Exception("Ops1"))
        }

        val observable2 = Observable.create<Int> {
            Thread.sleep(300)

            //It will throw an UndeliverableException
            it.onError(Exception("UndeliverableException exception"))
        }

        Observables.zip(observable1, observable2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {},
                onError = { Timber.d(it.message) },
                onNext = {}
            )

        WaitingUtils.waitOnSystem(1000)
    }
}