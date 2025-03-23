package com.ymsu.bike_compose.data

data class StationInfoDetail(
    val available_bikes: Int = 0,
    val available_e_bikes: Int = 0,
    val available_return: Int = 0,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val station_address: String = "",
    val station_name: String = "",
    val station_uid: String = "",
    val update_time: String = "",
    val image_url: String = ""
)