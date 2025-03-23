package com.ymsu.bike_compose.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(settings: Settings)

    @Query("SELECT value FROM Settings WHERE `key` = :key")
    suspend fun getSettingValue(key: String):String

    @Query("DELETE FROM Settings WHERE `key` = :key")
    suspend fun removeSetting(key: String)
}