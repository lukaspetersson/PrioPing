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
}

