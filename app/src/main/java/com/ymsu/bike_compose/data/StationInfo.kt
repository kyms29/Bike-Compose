package com.ymsu.bike_compose.data

data class StationInfoItem(
    val AuthorityID: String = "",
    val BikesCapacity: Int = 0,
    val ServiceType: Int = 0,
    val SrcUpdateTime: String = "",
    val StationAddress: StationAddress = StationAddress(),
    val StationID: String = "",
    val StationName: StationName = StationName(),
    val StationPosition: StationPosition = StationPosition(),
    val StationUID: String = "",
    val UpdateTime: String = ""
)

data class StationPosition(
    val GeoHash: String = "",
    val PositionLat: Double = 0.0,
    val PositionLon: Double = 0.0
)

data class StationName(
    val En: String = "",
    val Zh_tw: String = ""
)

data class StationAddress(
    val En: String = "",
    val Zh_tw: String = ""
)