package com.ymsu.bike_compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route:String, val title:String, val icon: ImageVector) {
    object Home: Screens("home","首頁",Icons.Default.Home)
    object Map: Screens("map","地圖",Icons.Default.Map)
    object Settings: Screens("settings","設定",Icons.Default.Settings)
}
