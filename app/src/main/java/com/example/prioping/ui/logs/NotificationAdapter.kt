package com.example.prioping.ui.logs

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import com.example.prioping.data.NotificationEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.prioping.databinding.ItemNotificationBinding
import androidx.recyclerview.widget.ListAdapter

class NotificationAdapter :
    ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentNotification = getItem(position)
        holder.bind(currentNotification)
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationEntity) {
            with(binding) {
                val appName = getAppNameFromPackageName(notification.packageName, itemView.context)
                notificationTitle.text = "$appName: ${notification.title}"
                notificationTitle.setTextColor(if (notification.flagged) Color.RED else Color.WHITE)

                itemView.setOnClickListener {
                    expandablePart.toggleVisibility()
                    notificationText.setTextOrHide("Text: ", notification.text)
                    notificationBigText.setTextOrHide("Big text: ", notification.bigText)
                    notificationSubText.setTextOrHide("Sub text: ", notification.subText)
                    notificationAiResp.setTextOrHide("AI resp: ", notification.aiResp)
                }
            }
        }

        private fun getAppNameFromPackageName(packageName: String, context: Context): String {
            val packageManager = context.packageManager
            return try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }
        }

        private fun TextView.setTextOrHide(prefix: String, text: String?) {
            text?.let {
                visibility = View.VISIBLE
                this.text = "$prefix$it"
            } ?: run {
                visibility = View.GONE
            }
        }

        private fun View.toggleVisibility() {
            visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
}

class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
    override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
        return oldItem.id == newItem.id
    }
}

