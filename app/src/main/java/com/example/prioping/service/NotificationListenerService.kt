package com.example.prioping.service


import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.lifecycle.MutableLiveData
import com.example.prioping.MyApplication
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
        sbn?.let {
            val title = it.notification.extras.getString("android.title")
            val text = it.notification.extras.getString("android.text")
            val notification = NotificationEntity(timestamp = System.currentTimeMillis(), title = title, text = text, packageName = it.packageName)
            CoroutineScope(Dispatchers.IO).launch {
                notificationDao.insert(notification)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        notifications.postValue(activeNotifications.toList())
    }
}

