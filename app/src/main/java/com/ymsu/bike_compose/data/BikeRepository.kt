package com.ymsu.bike_compose.data

class BikeRepository(private val apiService: BikeApiService) {
    suspend fun getStationInfo(): List<StationInfoItem> = apiService.getStationInfo()
    suspend fun getAvailableInfo(): List<AvailableInfoItem> = apiService.getAvailableInfo()
    suspend fun getNearByStationInfo(nearBy: String): List<StationInfoItem> = apiService.getStationInfoNearBy(nearBy)
    suspend fun getNearByAvailableInfo(nearBy: String): List<AvailableInfoItem> = apiService.getAvailabilityInfoNearBy(nearBy)
}