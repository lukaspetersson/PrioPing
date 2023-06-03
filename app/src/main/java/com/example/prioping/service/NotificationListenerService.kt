package com.example.prioping.service


import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.example.prioping.MainActivity
import com.example.prioping.MyApplication
import com.example.prioping.R
import com.example.prioping.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : NotificationListenerService() {

    companion object {
        val notifications = MutableLiveData<List<StatusBarNotification>>()
    }

    private val notificationDao = MyApplication.database.notificationDao()


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val isServiceActive = this.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)
            .getBoolean("service_active", true)
        if (!isServiceActive) return

        sbn?.let {
            // gmail: sender, messenger: sender
            val title = it.notification.extras.getCharSequence("android.title")?.toString()
            // gmail: subject, messenger: message
            val text = it.notification.extras.getCharSequence("android.text")?.toString()
            // gmail: lukas.pe..., messenger: null
            val subText = it.notification.extras.getCharSequence("android.subText")?.toString()
            // gmail: subject+message?
            val bigText = it.notification.extras.getCharSequence("android.bigText")?.toString()
            val packageName = it.packageName
            val isSystemApp = (it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0

            if (!isSystemApp && !packageName.contains("com.android") && !packageName.contains("example.prioping")) {
                val notification = NotificationEntity(
                    timestamp = System.currentTimeMillis(),
                    title = title,
                    text = text,
                    subText = subText,
                    bigText = bigText,
                    packageName = packageName,
                    aiResp = "Test",
                    trigger = packageName.contains("google.android.gm")
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val fromTime = notification.timestamp - 10_000  // 10 seconds ago
                    val recentSimilarNotification = notificationDao.getRecentSimilarNotification(fromTime, title, text, subText, bigText, packageName)

                    // Make sure not all fields are null
                    if (title != null || text != null || subText != null || bigText != null) {
                        if (recentSimilarNotification == null) {
                            notificationDao.insert(notification)
                        }
                    }
                }

            }
        }
    }


    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        notifications.postValue(activeNotifications.toList())
    }
}

