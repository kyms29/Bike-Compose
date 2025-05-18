package com.ymsu.bike_compose.data

data class StationInfo(
    val stationInfoDetail: StationInfoDetail,
    val isFavorite: Boolean = false,
    val distance: Float = 0F
)
