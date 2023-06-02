package com.example.prioping.ui.logs

import androidx.lifecycle.ViewModel
import com.example.prioping.MyApplication
import com.example.prioping.service.NotificationService

class LogsViewModel : ViewModel() {

    private val notificationDao = MyApplication.database.notificationDao()
    val notifications = notificationDao.getNotifications()
}
