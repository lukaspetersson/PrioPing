package com.example.prioping.ui.logs

import android.graphics.Color
import com.example.prioping.data.NotificationEntity
import android.view.LayoutInflater
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
        fun bind(notification: NotificationEntity) {
            binding.apply {
                notificationTitle.text = "Title: "+notification.title
                notificationText.text = "Text: "+notification.text
                notificationBigText.text = "Big text: "+notification.bigText
                notificationSubText.text = "Sub text: "+notification.subText
                notificationPkgName.text = "Pkg name: "+notification.packageName
                notificationAiResp.text = "AI resp: "+notification.aiResp

                if (notification.trigger) {
                root.setBackgroundColor(Color.RED)
                } else {
                    root.setBackgroundColor(Color.WHITE)
                }
            }
        }
    }
}
