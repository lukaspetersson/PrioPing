package com.example.prioping.ui.action

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prioping.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActionViewModel(private val context: Context) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)

    var email: String
        get() = sharedPreferences.getString("email", "") ?: ""
        set(value) = sharedPreferences.edit().putString("email", value).apply()

    var emailFlagged: Boolean
        get() = sharedPreferences.getBoolean("email_flagged", false)
        set(value) = sharedPreferences.edit().putBoolean("email_flagged", value).apply()

    var emailUnflagged: Boolean
        get() = sharedPreferences.getBoolean("email_unflagged", false)
        set(value) = sharedPreferences.edit().putBoolean("email_unflagged", value).apply()

    var filterFlagged: Boolean
        get() = sharedPreferences.getBoolean("filter_flagged", false)
        set(value) = sharedPreferences.edit().putBoolean("filter_flagged", value).apply()

    var filterUnflagged: Boolean
        get() = sharedPreferences.getBoolean("filter_unflagged", false)
        set(value) = sharedPreferences.edit().putBoolean("filter_unflagged", value).apply()

    var isErrorFlagged: Boolean
        get() = sharedPreferences.getBoolean("error_flagged", false)
        set(value) = sharedPreferences.edit().putBoolean("error_flagged", value).apply()

    class ActionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ActionViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
