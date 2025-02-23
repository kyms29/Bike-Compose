package com.ymsu.bike_compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ymsu.bike_compose.theme.AppTheme

@Composable
fun SettingsScreen() {
    Text(
        text = "Settings Screen", modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    // use list + 從資料庫拿出我收藏的車站清單，再呼叫tdx api去拿到全台灣站點資料
    // 1. viewmodel要可以呼叫全台站點
    // 2. 用progress bar來處理載入資料的時候的畫面
    // 3. 點擊站點可以切換到地圖頁面
}

@Preview(showBackground = true)
@Composable
fun previewSettings(){
    AppTheme {
        SettingsScreen()
    }
}