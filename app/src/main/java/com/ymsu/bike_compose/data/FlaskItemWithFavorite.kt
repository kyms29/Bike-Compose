package com.ymsu.bike_compose.data

data class FlaskItemWithFavorite(val stationInfoFromFlaskItem: StationInfoFromFlaskItem,
                                    var isFavorite:Boolean = false,
                                    var distance: Float = 0F)
