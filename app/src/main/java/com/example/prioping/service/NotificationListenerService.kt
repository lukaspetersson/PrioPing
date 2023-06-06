package com.example.prioping.service


import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.prioping.MyApplication
import com.example.prioping.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationService : NotificationListenerService() {

    companion object {
        val notifications = MutableLiveData<List<StatusBarNotification>>()
    }

    override fun onCreate() {
    val handler = Handler(Looper.getMainLooper())
    val runnableCode = object : Runnable {
        override fun run() {
            processNotifications()
            handler.postDelayed(this, TimeUnit.MINUTES.toMillis(5))
        }
    }
    handler.post(runnableCode)
}
    data class NotificationDetail<T, U, V, W>(val text: String, val bigText: String, val subText: String, val notificationKey: String)


    private val notificationDao = MyApplication.database.notificationDao()
// Global dictionaries
private val notificationsDict = mutableMapOf<String, MutableMap<String, MutableList<NotificationDetail<String, String, String, String>>>>()
private val notificationStore = mutableMapOf<String, MutableList<StatusBarNotification>>()

    @OptIn(BetaOpenAI::class)
override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)

    val sharedPreferences = this.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)
    val isServiceActive = sharedPreferences.getBoolean("service_active", true)
    if (!isServiceActive) return

    sbn?.let {
        val titleOrig = it.notification.extras.getCharSequence("android.title")?.toString()
        val text = it.notification.extras.getCharSequence("android.text")?.toString()
        val subText = it.notification.extras.getCharSequence("android.subText")?.toString()
        val bigText = it.notification.extras.getCharSequence("android.bigText")?.toString()
        val packageName = it.packageName
        val isSystemApp = (it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0

        if (!isSystemApp && !packageName.contains("com.android") && !packageName.contains("example.prioping")) {
            // Cancel the notification
            cancelNotification(it.key)

            val notificationList = notificationStore.getOrPut(it.key) { mutableListOf() }
            notificationList.add(it)

            var title = titleOrig
            if (titleOrig?.contains(":") == true || titleOrig?.contains("-") == true) {
                title = titleOrig.substringBefore(":").substringBefore("-")
            }

            // Insert the details into the dictionary
            val packageMap = notificationsDict.getOrPut(packageName) { mutableMapOf() }
            val titleList = packageMap.getOrPut(title ?: "") { mutableListOf() }
            val notificationDetails = NotificationDetail<String, String, String, String>(text ?: "", bigText ?: "", subText ?: "", it.key)
            if (!titleList.contains(notificationDetails)) {
                titleList.add(notificationDetails)
            }
        }
    }
}

override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    super.onNotificationRemoved(sbn)
    sbn?.let {
        val notificationList = notificationStore[it.key]
        notificationList?.remove(it)
        if (notificationList.isNullOrEmpty()) {
            notificationStore.remove(it.key)
        }
    }
}


    @OptIn(BetaOpenAI::class)
    fun processNotifications() {
    val sharedPreferences = this.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)
    val apiKey = sharedPreferences.getString("api_key", "") ?: ""
    val instructions = sharedPreferences.getString("instruction", "") ?: ""
    val isErrorFlagged = sharedPreferences.getBoolean("error_flagged", false)

    CoroutineScope(Dispatchers.IO).launch {
        val currentTime = System.currentTimeMillis()
        for ((packageName, packageMap) in notificationsDict) {
            for ((title, titleList) in packageMap) {
                // The postTime equivalent in StatusBarNotification is notification.when
                val lastNotification = notificationStore[titleList.last().notificationKey]?.lastOrNull()
                val timeDifference = currentTime - (lastNotification?.notification?.`when` ?: 0)
                if (timeDifference >= TimeUnit.MINUTES.toMillis(5) && titleList.isNotEmpty()) {
                    val mergedNotification = titleList.joinToString(" ") { it.text + " " + it.bigText }
                    val prompt = getPrompt(instructions, packageName, title, mergedNotification)

                    val aiLogEntry = NotificationEntity(
                            timestamp = System.currentTimeMillis(),
                            title = "Sent to AI: $title",
                            text = prompt,
                            subText = "",
                            bigText = "",
                            packageName = packageName,
                            aiResp = "",
                            flagged = false,
                            error = false
                        )
                    notificationDao.insert(aiLogEntry)

                    var aiResp: String
                    var errorOccurred = false

                    if (apiKey.isEmpty()) {
                        aiResp = "Error: no api key provided"
                        errorOccurred = true
                    } else if (instructions.isEmpty()) {
                        aiResp = "Error: no instructions provided"
                        errorOccurred = true
                    } else {
                        try {
                            val response = getAiResponse(apiKey, prompt)
                            aiResp = response.choices[0].message?.content.toString()
                        } catch (e: Exception) {
                            aiResp = "Error: " + e.message
                            errorOccurred = true
                        }
                    }

                    val flagged = if (errorOccurred) isErrorFlagged else aiResp.split(" ").last().lowercase().contains("flag")

                    val actionFilterFlagged = sharedPreferences.getBoolean("filter_flagged", false)
                    val actionFilterUnflagged = sharedPreferences.getBoolean("filter_unflagged", false)

                    if ((flagged && actionFilterFlagged) || (!flagged && actionFilterUnflagged)) {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    if (notificationManager is NotificationManager) {
                        val notificationList = notificationStore[titleList.first().notificationKey]
                        notificationList?.forEach { originalNotification ->
                            notificationManager.notify(originalNotification.id, originalNotification.notification)
                        }
                    }
                }

                    // Insert into database
                titleList.forEach { (text: String, bigText: String, subText: String, notificationKey: String) ->
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
                        notificationDao.insert(notification)
                    }

                    // Remove processed entries
                    titleList.clear()
                }
            }
        }
    }
}

    fun getPrompt(instructions: String, packageName: String, title: String, text: String): String {
        val sb = StringBuilder()
        sb.append("App: $packageName.\n")
        sb.append("Title: $title.\n")
        sb.append("Text: $text.\n")
        sb.append("User instructions: $instructions. \n\n")
        sb.append("Flag the notification if it satisfies the user instructions by writing 'FLAG' as the last word of your output")
        return sb.toString()
    }

        @OptIn(BetaOpenAI::class)
        private suspend fun getAiResponse(apiKey: String, prompt: String): ChatCompletion {
             val openai = OpenAI(
                token = apiKey,
            )
            val system_prompt = "You are a productivity assistant helping me to flag notifications that need my attention and filter out the rest."

        val chatCompletionRequest = ChatCompletionRequest(
          model = ModelId("gpt-3.5-turbo"),
          messages = listOf(
            ChatMessage(
              role = ChatRole.System,
              content = system_prompt
            ), ChatMessage(
              role = ChatRole.User,
              content = prompt
            )
          )
        )
        return openai.chatCompletion(chatCompletionRequest)
    }
}

