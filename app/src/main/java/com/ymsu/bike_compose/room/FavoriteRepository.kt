package com.ymsu.bike_compose.room

import android.util.Log

class FavoriteRepository(private val dao: FavoriteStationDao) {
    private val TAG = "[FavoriteRepository]"
    suspend fun toggleFavorite(stationUID: String){
        if (dao.isFavorite(stationUID) == null) {
            Log.d(TAG,"[toggleFavorite] add favorite = $stationUID")
            dao.addFavorite(FavoriteStation(stationUID))
        } else {
            Log.d(TAG,"[toggleFavorite] remove favorite = $stationUID")
            dao.removeFavorite(stationUID)
        }
    }

    suspend fun isFavorite(stationUID: String): Boolean {
        return dao.isFavorite(stationUID) != null
    }

    suspend fun getAll(): Set<String>{
        val favoriteStations = dao.getAll()
        Log.d(TAG,"[getALL] Data = "+favoriteStations.map { it.stationUid }.toString())
        return favoriteStations.map { it.stationUid }.toSet()
    }
}