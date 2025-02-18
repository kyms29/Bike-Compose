package com.ymsu.bike_compose.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteStation::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun favoriteStationDao():FavoriteStationDao

    companion object{
        var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance?: synchronized(this){
            instance?: Room.databaseBuilder(context, AppDatabase::class.java,"bike_compose")
                .build().also { instance = it }
        }
    }
}