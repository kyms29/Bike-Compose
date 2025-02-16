package com.ymsu.bike_compose.data

data class AvailableInfoItem(
    val AvailableRentBikes: Int = 0,
    val AvailableRentBikesDetail: AvailableRentBikesDetail = AvailableRentBikesDetail(),
    val AvailableReturnBikes: Int = 0,
    val ServiceStatus: Int = 0,
    val ServiceType: Int = 0,
    val SrcUpdateTime: String = "",
    val StationID: String = "",
    val StationUID: String = "",
    val UpdateTime: String = ""
)

data class AvailableRentBikesDetail(
    val ElectricBikes: Int = 0,
    val GeneralBikes: Int = 0
)