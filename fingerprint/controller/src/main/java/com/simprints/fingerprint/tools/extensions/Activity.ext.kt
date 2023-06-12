package com.simprints.fingerprint.tools.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.infra.logging.Simber

fun Context.showToast(string: String) =
    runOnUiThread {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }
fun Context.runOnUiThread(f: Context.() -> Unit) {
    if (Looper.getMainLooper() === Looper.myLooper()) f() else
        Handler(Looper.getMainLooper()).post {
            f() }
}
fun Activity.logActivityCreated() {
    Simber.d("Fingerprint Activity Log : created ${this::class.simpleName}")
}

fun Activity.logActivityDestroyed() {
    Simber.d("Fingerprint Activity Log : destroyed ${this::class.simpleName}")
}

fun Activity.setResultAndFinish(resultCode: ResultCode, data: Intent?) {
    setResult(resultCode.value, data)
    finish()
}