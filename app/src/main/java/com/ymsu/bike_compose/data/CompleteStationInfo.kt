package com.ymsu.bike_compose.data

data class CompleteStationInfo(
    val stationInfoItem: StationInfoItem,
    var availableInfoItem: AvailableInfoItem,
    var isFavorite:Boolean = false,
    var distance: Float = 0F
)
