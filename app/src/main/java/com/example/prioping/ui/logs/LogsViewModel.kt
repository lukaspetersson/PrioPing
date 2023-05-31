package com.example.prioping.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.prioping.service.NotificationService

class LogsViewModel : ViewModel() {

    val notifications = NotificationService.notifications.asLiveData()
}

