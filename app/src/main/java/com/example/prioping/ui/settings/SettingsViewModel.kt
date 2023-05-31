package com.example.prioping.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.example.prioping.service.NotificationService

class SettingsViewModel(private val context: Context) : ViewModel() {

    private val serviceComponent = ComponentName(context, NotificationService::class.java)

    fun isServiceEnabled(): Boolean {
        val enabledNotificationListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledNotificationListeners.contains(serviceComponent.flattenToString())
    }

    fun startService() {
        if (!isServiceEnabled()) {
            // Launch settings to grant permission
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun stopService() {
        if (isServiceEnabled()) {
            // Stop the service
            context.stopService(Intent(context, NotificationService::class.java))
        }
    }
}
