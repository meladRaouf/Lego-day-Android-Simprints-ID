package com.simprints.id.testUtils

import android.app.Activity
import com.simprints.id.activities.front.FrontActivity
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.robolectric.Shadows

inline fun <reified T> mock(): T =
        Mockito.mock(T::class.java)

fun <T> whenever(methodCall: T): OngoingStubbing<T> =
        Mockito.`when`(methodCall)

fun <T> verifyOnlyInteraction(mock: T, methodCall: T.() -> Any?) {
    Mockito.verify(mock).methodCall()
    Mockito.verifyNoMoreInteractions(mock)
}

fun <T> verifyOnlyInteractions(mock: T, vararg methodCalls: T.() -> Any?) {
    for (methodCall in methodCalls) {
        Mockito.verify(mock).methodCall()
    }
    Mockito.verifyNoMoreInteractions(mock)
}

fun <T> anyNotNull(): T {
    Mockito.any<T>()
    return uninitialized()
}
fun <T> uninitialized(): T = null as T

/**
 * Junit 5 has a nice assertThrows method
 * (http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/Assertions.html#assertThrows-java.lang.Class-org.junit.jupiter.api.function.Executable-)
 * This is a placeholder until we migrate from JUnit 4 to Junit 5
 */
inline fun <reified T : Throwable> assertThrows(executable: () -> Unit): T {
    try {
        executable()
    } catch (exception: Throwable) {
        when (exception) {
            is T -> return exception
            else -> throw(exception)
        }
    }
    throw AssertionFailedError("Expected an ${T::class.java.simpleName} to be thrown")
}

inline fun <reified T : Throwable> assertThrows(throwable: T, executable: () -> Unit): T {
    val thrown = assertThrows<T>(executable)
    assertEquals(throwable, thrown)
    return thrown
}

inline fun assertActivityStarted(clazz: Class<out Activity>, activity: FrontActivity) {
    val shadowActivity = Shadows.shadowOf(activity)
    val startedIntent = shadowActivity.nextStartedActivity
    Assert.assertThat(startedIntent.component.className,
        CoreMatchers.equalTo(clazz.name))
}