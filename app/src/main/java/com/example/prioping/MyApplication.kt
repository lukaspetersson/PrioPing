package com.example.prioping

import android.app.Application
import androidx.room.Room
import com.example.prioping.data.AppDatabase

class MyApplication : Application() {

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "app_database").build()
    }
}


