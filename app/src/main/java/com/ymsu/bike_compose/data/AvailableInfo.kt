package com.ymsu.bike_compose.data

data class AvailableInfoItem(
    val AvailableRentBikes: Int,
    val AvailableRentBikesDetail: AvailableRentBikesDetail,
    val AvailableReturnBikes: Int,
    val ServiceStatus: Int,
    val ServiceType: Int,
    val SrcUpdateTime: String,
    val StationID: String,
    val StationUID: String,
    val UpdateTime: String
)

data class AvailableRentBikesDetail(
    val ElectricBikes: Int,
    val GeneralBikes: Int
)