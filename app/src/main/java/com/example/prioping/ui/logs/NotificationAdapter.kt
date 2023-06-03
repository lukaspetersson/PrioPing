package com.example.prioping.ui.logs

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import com.example.prioping.data.NotificationEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prioping.databinding.ItemNotificationBinding

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    var notifications = listOf<NotificationEntity>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

    fun getAppNameFromPackageName(packageName: String, context: Context): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // if there's an error, just return the package name
        }
    }

    fun bind(notification: NotificationEntity) {
        binding.apply {
            var appName = getAppNameFromPackageName(notification.packageName, itemView.context)
            notificationTitle.text = appName+": "+notification.title
            if (notification.trigger) {
                notificationTitle.setTextColor(Color.RED)
            }

            itemView.setOnClickListener {
                if (binding.expandablePart.visibility == View.VISIBLE) {
                    binding.expandablePart.visibility = View.GONE
                } else {
                    binding.expandablePart.visibility = View.VISIBLE
                    notification.text?.let {
                        notificationText.visibility = View.VISIBLE
                        notificationText.text = "Text: $it"
                    } ?: run {
                        notificationText.visibility = View.GONE
                    }

                    notification.bigText?.let {
                        notificationBigText.visibility = View.VISIBLE
                        notificationBigText.text = "Big text: $it"
                    } ?: run {
                        notificationBigText.visibility = View.GONE
                    }

                    notification.subText?.let {
                        notificationSubText.visibility = View.VISIBLE
                        notificationSubText.text = "Sub text: $it"
                    } ?: run {
                        notificationSubText.visibility = View.GONE
                    }

                    notification.aiResp.let {
                        notificationAiResp.visibility = View.VISIBLE
                        notificationAiResp.text = "AI resp: $it"
                    } ?: run {
                        notificationAiResp.visibility = View.GONE
                    }
                }
            }
        }
    }
}

}
