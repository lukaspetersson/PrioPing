package com.example.prioping.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData

class NotificationService : NotificationListenerService() {

    companion object {
        val notifications = MutableLiveData<List<StatusBarNotification>>()
    }

    private val notificationId = 1
    private val channelId = "NotificationChannel"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        notifications.postValue(activeNotifications.toList())
        showNotification()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        notifications.postValue(activeNotifications.toList())
    }

    private fun showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notifications", IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("New Notification")
            .setContentText("A new notification has been logged.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Set your own notification icon here
            .build()

        notificationManager.notify(notificationId, notification)
    }
}

