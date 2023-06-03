package com.example.prioping.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp >= :fromTime AND title = :title AND text = :text AND bigText = :bigText AND subText = :subText AND packageName = :packageName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getRecentSimilarNotification(fromTime: Long, title: String?, text: String?, bigText: String?, subText: String?, packageName: String?): NotificationEntity?

}

