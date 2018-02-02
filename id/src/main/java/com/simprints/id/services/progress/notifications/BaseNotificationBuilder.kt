package com.simprints.id.services.progress.notifications

import android.app.Notification
import android.app.NotificationManager
import android.support.v4.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean


abstract class BaseNotificationBuilder(private val notificationManager: NotificationManager,
                                       notificationBuilder: NotificationCompat.Builder,
                                       override val tag: String,
                                       title: String,
                                       icon: Int)
    : NotificationBuilder {

    override val id = NotificationBuilder.newId()

    protected val visible = AtomicBoolean(false)

    private val builder: NotificationCompat.Builder =
            notificationBuilder
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setOnlyAlertOnce(true)
                    .setContentTitle(title)
                    .setSmallIcon(icon)

    protected fun updateBuilder(op: NotificationCompat.Builder.() -> Unit) =
            synchronized(builder) {
                builder.op()
            }

    override fun build(): Notification {
        synchronized(builder) {
            return builder.build()
        }
    }

    override fun setVisibility(visible: Boolean) {
        this.visible.set(visible)
    }

    protected fun notifyIfVisible() {
        if (visible.get()) {
            notificationManager.notify(id, build())
        }
    }

    protected fun cancelIfVisible() {
        if (visible.get()) {
            notificationManager.cancel(id)
        }
    }
}