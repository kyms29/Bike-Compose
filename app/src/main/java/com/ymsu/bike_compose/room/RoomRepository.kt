package com.ymsu.bike_compose.room

import android.util.Log

class RoomRepository(private val favoriteStationDao: FavoriteStationDao,
                     private val settingsDao: SettingsDao) {
    private val TAG = "[RoomRepository]"

    suspend fun toggleFavorite(stationUID: String) {
        if (favoriteStationDao.isFavorite(stationUID) == null) {
            Log.d(TAG, "[toggleFavorite] add favorite = $stationUID")
            favoriteStationDao.addFavorite(FavoriteStation(stationUID))
        } else {
            Log.d(TAG, "[toggleFavorite] remove favorite = $stationUID")
            favoriteStationDao.removeFavorite(stationUID)
        }
    }

    fun getFavoriteList() = favoriteStationDao.getFavoriteList()

    suspend fun saveSetting(key:String, value: String){
        settingsDao.insertSetting(Settings(key, value))
    }

    suspend fun getSettingValue(key: String) = settingsDao.getSettingValue(key)?:"1000"
}