package com.example.prioping.ui.trigger

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prioping.service.NotificationService

class TriggerViewModel(private val context: Context) : ViewModel() {

    private val serviceComponent = ComponentName(context, NotificationService::class.java)
    private val sharedPreferences = context.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)

    var isServiceActive: Boolean
        get() = sharedPreferences.getBoolean("service_active", false)
        set(value) = sharedPreferences.edit().putBoolean("service_active", value).apply()

    var apiKey: String
        get() = sharedPreferences.getString("api_key", "") ?: ""
        set(value) = sharedPreferences.edit().putString("api_key", value).apply()

    var instruction: String
        get() = sharedPreferences.getString("instruction", "") ?: ""
        set(value) = sharedPreferences.edit().putString("instruction", value).apply()

    class TriggerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TriggerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TriggerViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    fun isServiceEnabled(): Boolean {
        val enabledNotificationListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledNotificationListeners.contains(serviceComponent.flattenToString())
    }

   fun startService() {
        if (!isServiceEnabled()) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            isServiceActive = true
        }
    }

    fun stopService() {
        if (isServiceEnabled()) {
            context.stopService(Intent(context, NotificationService::class.java))
            isServiceActive = false
        }
    }
}