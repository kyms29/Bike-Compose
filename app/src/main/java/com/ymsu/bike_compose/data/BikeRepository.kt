package com.ymsu.bike_compose.data

class BikeRepository(private val apiService: BikeApiService) {
    suspend fun getStationInfo(): List<StationInfoItem> = apiService.getStationInfo()
    suspend fun getAvailableInfo(): List<AvailableInfoItem> = apiService.getAvailableInfo()
}