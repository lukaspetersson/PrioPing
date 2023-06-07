package com.example.prioping.ui.logs

import androidx.lifecycle.ViewModel
import com.example.prioping.MyApplication
import com.example.prioping.service.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogsViewModel : ViewModel() {

    private val notificationDao = MyApplication.database.notificationDao()
    val notifications = notificationDao.getNotifications()


       fun clearLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            notificationDao.clearAll()
        }
    }
}
