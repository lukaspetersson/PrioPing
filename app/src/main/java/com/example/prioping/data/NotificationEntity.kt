package com.example.prioping.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val title: String?,
    val text: String?,
    val subText: String?,
    val bigText: String?,
    val packageName: String,
    val aiResp: String,
    val flagged: Boolean,
    val error: Boolean = false
)

