package com.ymsu.bike_compose.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Settings")
data class Settings(
    @PrimaryKey val key : String,
    @ColumnInfo val value: String
)