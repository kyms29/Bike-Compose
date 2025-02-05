package com.ymsu.bike_compose.data

data class StationInfoItem(
    val AuthorityID: String,
    val BikesCapacity: Int,
    val ServiceType: Int,
    val SrcUpdateTime: String,
    val StationAddress: StationAddress,
    val StationID: String,
    val StationName: StationName,
    val StationPosition: StationPosition,
    val StationUID: String,
    val UpdateTime: String
)

data class StationPosition(
    val GeoHash: String,
    val PositionLat: Double,
    val PositionLon: Double
)

data class StationName(
    val En: String,
    val Zh_tw: String
)

data class StationAddress(
    val En: String,
    val Zh_tw: String
)