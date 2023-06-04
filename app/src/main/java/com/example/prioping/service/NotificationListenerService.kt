package com.example.prioping.service


import android.app.Notification
import android.content.Context
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

class NotificationService : NotificationListenerService() {

    companion object {
        val notifications = MutableLiveData<List<StatusBarNotification>>()
    }

    private val notificationDao = MyApplication.database.notificationDao()

    @OptIn(BetaOpenAI::class)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val sharedPreferences = this.getSharedPreferences("com.example.prioping", Context.MODE_PRIVATE)
        val isServiceActive = sharedPreferences.getBoolean("service_active", true)
        if (!isServiceActive) return

        sbn?.let {
            val title = it.notification.extras.getCharSequence("android.title")?.toString()
            val text = it.notification.extras.getCharSequence("android.text")?.toString()
            val subText = it.notification.extras.getCharSequence("android.subText")?.toString()
            val bigText = it.notification.extras.getCharSequence("android.bigText")?.toString()
            val packageName = it.packageName
            val isSystemApp = (it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0
            if (!isSystemApp && !packageName.contains("com.android") && !packageName.contains("example.prioping")) {
                val apiKey = sharedPreferences.getString("api_key", "") ?: ""
                val instructions = sharedPreferences.getString("instruction", "")?: ""
                val isErrorFlagged = sharedPreferences.getBoolean("error_flagged", false)
                CoroutineScope(Dispatchers.IO).launch {

                    val prompt = getPrompt(instructions, packageName, title, text, subText, bigText)
                    Log.e("EEEEE", "11111111111: "+prompt)

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
                        try {
                            val response = getAiResponse(apiKey, prompt)
                            Log.e("EEEEE", "3333333333333: "+response)
                            aiResp = response.choices[0].message?.content.toString()
                        } catch (e: Exception) {
                            aiResp = "Error: "+e.message
                            errorOccurred = true
                        }
                    }

                    Log.e("EEEEE", "3333333333333: "+aiResp)

                    val flagged = if (errorOccurred) isErrorFlagged else aiResp.split(" ").last().lowercase().contains("flag")
                    Log.e("EEEEE", "44444444444: "+flagged)

                    val actionEmailFlagged = sharedPreferences.getBoolean("email_flagged", false)
                    val actionEmailUnflagged = sharedPreferences.getBoolean("email_unflagged", false)
                    val actionFilterFlagged = sharedPreferences.getBoolean("filter_flagged", false)
                    val actionFilterUnflagged = sharedPreferences.getBoolean("filter_unflagged", false)
                    //val emailAddress = sharedPreferences.getString("email_address", "")

                    if ((flagged && actionFilterFlagged) || (!flagged && actionFilterUnflagged)) {
                        cancelNotification(it.key)
                        Log.e("EEEEE", "555555555: CANCEL")

                    }

                    if ((flagged && actionEmailFlagged) || (!flagged && actionEmailUnflagged)) {
                        // You need to implement sendEmail()
                        // This should be a function which sends an email using the SMTP server to the emailAddress
                        //sendEmail(emailAddress, title, text)
                        Log.e("EEEEE", "666666666: EMAIL")

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
        title?.let { sb.append("Title: $it.\n") }
        text?.let { sb.append("Text: $it.\n") }
        subText?.let { sb.append("Sub text: $it.\n") }
        bigText?.let { sb.append("Big text: $it.\n") }
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

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        notifications.postValue(activeNotifications.toList())
    }
}

