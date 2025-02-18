package com.ymsu.bike_compose.applicationLayer

import android.app.Application
import com.ymsu.bike_compose.room.AppDatabase
import com.ymsu.bike_compose.room.FavoriteRepository

class MyApplication: Application() {
    lateinit var favoriteRepository: FavoriteRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val appDatabase = AppDatabase.getInstance(applicationContext)
        val dao = appDatabase.favoriteStationDao()
        favoriteRepository = FavoriteRepository(dao)
    }
}