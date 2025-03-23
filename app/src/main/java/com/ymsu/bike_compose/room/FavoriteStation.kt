package com.ymsu.bike_compose.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoriteStation")
data class FavoriteStation(
    @PrimaryKey var stationUid: String
)