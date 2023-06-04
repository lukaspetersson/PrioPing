package com.example.prioping.service


import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.prioping.MyApplication
import com.example.prioping.ai.OpenAIApi
import com.example.prioping.ai.OpenAIApiRequestBody
import com.example.prioping.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class NotificationService : NotificationListenerService() {

    companion object {
        val notifications = MutableLiveData<List<StatusBarNotification>>()
    }

    private val notificationDao = MyApplication.database.notificationDao()

override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)

    val sharedPreferences = this.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)
    val isServiceActive = sharedPreferences.getBoolean("service_active", true)
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

        val apiKey = sharedPreferences.getString("api_key", "") ?: ""
        val instructions = sharedPreferences.getString("instruction", "")?: ""
        val isErrorFlagged = sharedPreferences.getBoolean("error_flagged", false)

        val prompt = getPrompt(instructions, packageName, title, text, subText, bigText)

        // You need to implement getAiResponse()
        // This should be a function which calls the OpenAI API using the apiKey and instructions
        var aiResp: String = ""
        var errorOccurred: Boolean = false
        if (apiKey.isEmpty()) {
            aiResp = "Error: no api key provided"
            errorOccurred = true
        }
        else if (instructions.isEmpty()) {
            aiResp = "Error: no instructions provided"
            errorOccurred = true
        }
        else{
           CoroutineScope(Dispatchers.IO).launch {
            try {
                aiResp = getAiResponse(apiKey, prompt)
            } catch (e: Exception) {
                aiResp = "Error: ${e.message}"
                errorOccurred = true
            }
        }
        }

        val flagged = if (errorOccurred) isErrorFlagged else aiResp.split(" ").last().lowercase().contains("flag")

        if (!isSystemApp && !packageName.contains("com.android") && !packageName.contains("example.prioping")) {
            val actionEmailFlagged = sharedPreferences.getBoolean("email_flagged", false)
            val actionEmailUnflagged = sharedPreferences.getBoolean("email_unflagged", false)
            val actionFilterFlagged = sharedPreferences.getBoolean("filter_flagged", false)
            val actionFilterUnflagged = sharedPreferences.getBoolean("filter_unflagged", false)
            //val emailAddress = sharedPreferences.getString("email_address", "")

            if ((flagged && actionFilterFlagged) || (!flagged && actionFilterUnflagged)) {
                cancelNotification(it.key)
            }

            if ((flagged && actionEmailFlagged) || (!flagged && actionEmailUnflagged)) {
                // You need to implement sendEmail()
                // This should be a function which sends an email using the SMTP server to the emailAddress
                //sendEmail(emailAddress, title, text)
            }

            val notification = NotificationEntity(
                timestamp = System.currentTimeMillis(),
                title = title,
                text = text,
                subText = subText,
                bigText = bigText,
                packageName = packageName,
                aiResp = aiResp,
                flagged = flagged,
                error = errorOccurred

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
    fun getPrompt(instructions: String, packageName: String, title: String?, text: String?, subText: String?, bigText: String?): String {
    val sb = StringBuilder()
    sb.append("App: $packageName.\n")
    title?.let { sb.append("Title: $it.") }
    text?.let { sb.append("Text: $it.") }
    subText?.let { sb.append("Sub text: $it.") }
    bigText?.let { sb.append("Big text: $it.") }
    sb.append("Flag this notification if it satisfies these criteria: $instructions.")
    sb.append("To flag, the last word of your output should be 'FLAG'")
    return sb.toString()
}


private suspend fun getAiResponse(apiKey: String, prompt: String): String {
    val api = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAIApi::class.java)

    val system_prompt = "You are a productivity assistant helping me to flag notifications that need my attention and filter out the rest."

    val body = OpenAIApiRequestBody(
        model = "gpt-3.5-turbo",
        messages = listOf(
            OpenAIApiRequestBody.Message(
                role = "system",
                content = system_prompt
            ),
            OpenAIApiRequestBody.Message(
                role = "user",
                content = prompt
            )
        )
    )
    return try {
        val response = api.getAiResponse("Bearer $apiKey", body)
        if (response.isSuccessful) {
            response.body()?.choices?.getOrNull(0)?.message?.content ?: "No response from AI"
        } else {
            "AI request failed with response code: ${response.code()}"
        }
    } catch (e: Exception) {
        "AI request failed with error: ${e.message}"
    }
}


    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        notifications.postValue(activeNotifications.toList())
    }
}

