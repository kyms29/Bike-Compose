package com.ymsu.bike_compose

import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.data.StationInfoDetail

data class BikeState(
    val search: String = "",
    val searchResults: List<StationInfo> = emptyList(),
    val allFavoriteStations: List<StationInfo> = emptyList(),
    val nearFavoriteStations: List<StationInfo> = emptyList(),
    val favoriteStations: List<StationInfo> = emptyList(),
    val recordFavoriteList: List<String> = emptyList(),
    val selectedStation: StationInfo? = null,
    val isLoading: Boolean = true,
    val errorMessage: String = "",
    val range:Int = 1000
)