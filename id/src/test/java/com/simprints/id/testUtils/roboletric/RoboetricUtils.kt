package com.simprints.id.testUtils.roboletric

import android.app.Activity
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ComponentController

@Throws(NoSuchFieldException::class, IllegalAccessException::class)
fun replaceActivityInController(activityController: ActivityController<*>, activity: Activity) {
    val componentField = ComponentController::class.java.getDeclaredField("component")
    componentField.isAccessible = true
    componentField.set(activityController, activity)
}