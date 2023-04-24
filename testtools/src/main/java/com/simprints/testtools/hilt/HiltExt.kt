package com.simprints.testtools.hilt

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

const val FRAGMENT_TAG = "FRAGMENT_TAG"

/**
 * launchFragmentInContainer from the androidx.fragment:fragment-testing library
 * is NOT possible to use right now as it uses a hardcoded Activity under the hood
 * (i.e. [EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint.
 *
 * As a workaround, use this function that is equivalent. It requires you to add
 * [HiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
 * as can be found in this project.
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
    @StyleRes themeResId: Int = androidx.appcompat.R.style.Theme_AppCompat,
    navController: NavController? = null,
    crossinline action: Fragment.() -> Unit = {}
) {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra(
        "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
        themeResId
    )

    ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, FRAGMENT_TAG)
            .setMaxLifecycle(fragment, initialState)
            .commitNow()

        navController?.also {
            Navigation.setViewNavController(fragment.requireView(), it)
        }

        fragment.action()
    }
}

fun FragmentActivity.moveToState(state: Lifecycle.State) {
    val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)!!
    supportFragmentManager.commitNow {
        setMaxLifecycle(fragment, state)
    }
}

fun testNavController(graph: Int, startDestination: Int? = null): TestNavHostController {
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    navController.setGraph(graph)
    startDestination?.also { navController.setCurrentDestination(it) }
    return navController
}