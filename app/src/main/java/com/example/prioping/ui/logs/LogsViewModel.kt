package com.example.prioping.ui.logs

import androidx.lifecycle.ViewModel
import com.example.prioping.service.NotificationService

class LogsViewModel : ViewModel() {

    val notifications = NotificationService.notifications
}
