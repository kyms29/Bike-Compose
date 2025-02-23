package com.ymsu.bike_compose.applicationLayer

import android.app.Application
import com.ymsu.bike_compose.room.AppDatabase
import com.ymsu.bike_compose.room.FavoriteRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
}