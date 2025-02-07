package com.ymsu.bike_compose

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ymsu.bike_compose.theme.AppTheme

@Composable
fun MapScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        /*
            TODO:
             2. 有網路才能取得目前位置 並移到該位置
             3. 根據目前位置畫出周遭X公尺的站點資訊
         */
        val singapore = LatLng(1.3521, 103.8198) // 指定一個位置
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(singapore, 12f)
        }

        val stationList by viewModel.stationList.collectAsState(initial = emptyList())

        stationList.forEach {
            Log.d("[MapScreen]", "stationList => $it")
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = rememberMarkerState(position = singapore),
                title = "Singapore",
                snippet = "A beautiful city!"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun previewMap(){
    val viewModel = MainViewModel()
    AppTheme {
        MapScreen(viewModel)
    }
}