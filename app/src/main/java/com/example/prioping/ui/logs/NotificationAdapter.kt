package com.example.prioping.ui.logs

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
                notificationTitle.text = notification.title
                notificationText.text = notification.text
            }
        }
    }
}
