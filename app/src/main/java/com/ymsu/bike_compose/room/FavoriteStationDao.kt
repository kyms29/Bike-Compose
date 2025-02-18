package com.ymsu.bike_compose.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favoriteStation WHERE stationUid = :id")
    suspend fun isFavorite(id: String): FavoriteStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favoriteStation: FavoriteStation)

    @Query("DELETE FROM favoriteStation WHERE stationUid = :id")
    suspend fun removeFavorite(id: String)

    @Query("SELECT * FROM favoriteStation")
    suspend fun getAll(): List<FavoriteStation>
}