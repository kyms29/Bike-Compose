package com.ymsu.bike_compose.data

data class StationInfo(
    val stationInfoDetail: StationInfoDetail,
    var isFavorite: Boolean = false,
    var distance: Float = 0F
)
