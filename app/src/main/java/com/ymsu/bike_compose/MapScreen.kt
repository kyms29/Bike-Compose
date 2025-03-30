package com.ymsu.bike_compose

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.data.StationInfoDetail
import com.ymsu.bike_compose.theme.AppTheme
import com.ymsu.bike_compose.theme.orange
import java.text.SimpleDateFormat
import java.util.Calendar

private const val TAG = "[MapScreen]"

@Composable
fun MapScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val currentLatLng by viewModel.currentLatLng.collectAsStateWithLifecycle()
        val realUserLatLng by viewModel.realUserLatLng.collectAsStateWithLifecycle()

        var zoomLevel by remember { mutableStateOf(16f) }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentLatLng, zoomLevel)
        }

        val context = LocalContext.current

        var showStationInfo by remember { mutableStateOf(false) }
        var selectedMarkerStationInfoDetail by remember {
            mutableStateOf(
                StationInfo(
                    StationInfoDetail()
                )
            )
        }
        var selectedMarker by remember { mutableStateOf<LatLng?>(null) }
        val markerSizes = remember { mutableStateMapOf<LatLng, Float>() }

        val nearByStationInfos by viewModel.nearByStationWithFavorite.collectAsStateWithLifecycle()
        val selectedStationFromHomeScreen by viewModel.selectedStation.collectAsStateWithLifecycle()

        LaunchedEffect(currentLatLng) {
            Log.d(TAG,"[LaunchedEffect] currentLatLng")
            cameraPositionState.animate(
                // use animate seems can avoid map not ready issue...?
                CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel)
            )
        }

        LaunchedEffect(selectedStationFromHomeScreen) {
            if (selectedStationFromHomeScreen != null) {
                val selectedLatLng = LatLng(
                    selectedStationFromHomeScreen!!.stationInfoDetail.lat,
                    selectedStationFromHomeScreen!!.stationInfoDetail.lng
                )

                selectedMarker = selectedLatLng
                markerSizes[selectedLatLng] = 1.3f
                showStationInfo = true
                selectedMarkerStationInfoDetail = selectedStationFromHomeScreen!!
                viewModel.setSelectedStation(null)
            }
        }

        LaunchedEffect(cameraPositionState.isMoving) {
            if (!cameraPositionState.isMoving) {
                val position = cameraPositionState.position.target
                Log.d(TAG, "cameraPositionState is moving to: ${position.latitude}, ${position.longitude}")
            viewModel.updateCurrentLatLng(position.latitude, position.longitude)
                zoomLevel = cameraPositionState.position.zoom
            }
        }


        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = true
                ),
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Circle(
                    center = LatLng(
                        cameraPositionState.position.target.latitude,
                        cameraPositionState.position.target.longitude
                    ),
                    fillColor = Color.LightGray.copy(alpha = 0.5f),
                    strokeColor = Color.LightGray.copy(alpha = 0.5f),
                    radius = 1000.0
                )

                when(nearByStationInfos) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "[MapScreen] nearByStationInfos size = " + (nearByStationInfos as ApiResult.Success<List<StationInfo>>).data.size)

                        (nearByStationInfos as ApiResult.Success<List<StationInfo>>).data.forEach { (stationInfo, isFavorite, distance) ->
                            val icon = getRateIcon(
                                stationInfo.available_bikes + stationInfo.available_e_bikes,
                                stationInfo.available_return
                            )
                            val markerPosition = LatLng(
                                stationInfo.lat,
                                stationInfo.lng
                            )
                            val markerSize = markerSizes[markerPosition] ?: 1f
                            Marker(
                                state = MarkerState(
                                    position = markerPosition
                                ),
                                title = stationInfo.station_name,
                                onClick = {
                                    selectedMarker = markerPosition
                                    markerSizes[markerPosition] = 1.3f
                                    showStationInfo = true
                                    selectedMarkerStationInfoDetail =
                                        StationInfo(stationInfo, isFavorite, distance)
                                    true
                                },
                                icon = vectorToBitmapDescriptor(
                                    vectorResId = icon,
                                    context = LocalContext.current,
                                    markerSize
                                )
                            )
                        }
                    }

                    is ApiResult.Loading -> {

                    }

                    is ApiResult.Error -> {
                        Log.d(TAG,"[MapScreen] [nearByStation] error : ${(nearByStationInfos as ApiResult.Error).message}")
                    }
                }
            }

            if (showStationInfo) {
                Log.d(TAG, "showStationInfo")
                val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                    latitude = currentLatLng.latitude
                    longitude = currentLatLng.longitude
                }
                BottomSheetDialog(currentLocation, selectedMarkerStationInfoDetail,
                    onDismiss = {
                        showStationInfo = false
                        selectedMarker?.let { markerSizes[it] = 1f }
                        selectedMarker = null
                    },
                    onFavoriteClick = {
                        viewModel.clickFavorite(selectedMarkerStationInfoDetail.stationInfoDetail.station_uid)
                    },
                    onShareClick = {
                        val sendIntent: Intent = Intent().apply {
                            val mapUri =
                                "https://www.google.com/maps/dir/?api=1&destination=" + selectedMarkerStationInfoDetail.stationInfoDetail.lat + ","
                            +selectedMarkerStationInfoDetail.stationInfoDetail.lng
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                selectedMarkerStationInfoDetail.stationInfoDetail.station_name + "有" +
                                        (selectedMarkerStationInfoDetail.stationInfoDetail.available_bikes + selectedMarkerStationInfoDetail.stationInfoDetail.available_e_bikes).toString() + "可借" +
                                        selectedMarkerStationInfoDetail.stationInfoDetail.available_return.toString() + "可還"
                                        + "，地點在$mapUri"
                            )
                            type = "text/plain"
                        }
                        sendIntent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            context.getString(R.string.share_subject)
                        )
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    onNavigateClick = {
                        val gmmIntentUri =
                            Uri.parse("google.navigation:q=" + selectedMarkerStationInfoDetail.stationInfoDetail.lat + "," + selectedMarkerStationInfoDetail.stationInfoDetail.lng + "&mode=w")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                )
            }

            FloatingActionButton(
                onClick = {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            realUserLatLng,
                            16f
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                containerColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "mylocation")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetDialog(
    currentLocation: Location,
    completeStationInfo: StationInfo,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        shape = RectangleShape,
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        DialogContent(completeStationInfo, currentLocation)
        BottomRowContent(completeStationInfo, onFavoriteClick, onShareClick, onNavigateClick)
    }
}

@Composable
private fun DialogContent(completeStationInfo: StationInfo, currentLocation: Location) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        text = completeStationInfo.stationInfoDetail.available_bikes.toString() + "可借"
                    )
                }
                Icon(
                    imageVector = Icons.Default.DirectionsBike, contentDescription = "Bike",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.background
                        )
                        .padding(2.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        text = completeStationInfo.stationInfoDetail.available_e_bikes.toString() + "可借"
                    )
                }
                Icon(
                    imageVector = Icons.Default.ElectricBolt, contentDescription = "Bike",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.background
                        )
                        .padding(2.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .width(100.dp)
                        .background(color = orange, shape = RoundedCornerShape(8.dp))
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        text = completeStationInfo.stationInfoDetail.available_return.toString() + "可還"
                    )
                }
                Icon(
                    imageVector = Icons.Default.LocalParking, contentDescription = "Bike",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(shape = RoundedCornerShape(16.dp), color = orange)
                        .padding(2.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp)
        ) {

            var currentTimeDate = Calendar.getInstance().time
            var diff =
                (currentTimeDate.time - convertTime(completeStationInfo.stationInfoDetail.update_time).time.time) / 1000

            Text(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .weight(1f),
                text = diff.toInt().toString() + "秒前更新",
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 12.sp
            )

            val distanceString = if (completeStationInfo.distance > 1000) {
                "%.2f".format(completeStationInfo.distance / 1000).toString() + "公里"
            } else {
                completeStationInfo.distance.toInt().toString() + "公尺"
            }

            Text(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .weight(1f),
                text = "距離$distanceString", color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }

        Text(
            modifier = Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            text = completeStationInfo.stationInfoDetail.station_name.substringAfter("_")
        )
        Text(
            modifier = Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            text = completeStationInfo.stationInfoDetail.station_address
        )
    }
}

@Composable
private fun BottomRowContent(
    completeStationInfo: StationInfo,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    var isFavorite by remember {
        mutableStateOf(completeStationInfo.isFavorite)
    }
    Log.d(
        TAG,
        "[BottomRowContent] Create or recompose , isFavorite = ${completeStationInfo.isFavorite}"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Button(
            onClick = {
                onFavoriteClick()
                isFavorite = !isFavorite
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "favorite",
                    tint = if (isFavorite) Color.Red else Color.Unspecified
                )
                Text(text = "收藏", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Button(
            onClick = { onShareClick() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Icon(imageVector = Icons.Default.Share, contentDescription = "share")
                Text(text = "分享", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Button(
            onClick = { onNavigateClick() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Icon(imageVector = Icons.Default.Navigation, contentDescription = "navigation")
                Text(text = "導航", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun getRateIcon(availableRent: Int = 0, availableReturn: Int = 0): Int {
    return remember(availableRent, availableReturn) {
        val availableRate =
            ((availableRent.toFloat() / (availableRent.toFloat() + availableReturn.toFloat())) * 100).toInt()

        when {
            availableRate == 0 -> R.drawable.ic_ubike_icon_0
            availableRate <= 10 -> R.drawable.ic_ubike_icon_10
            availableRate <= 20 -> R.drawable.ic_ubike_icon_20
            availableRate <= 30 -> R.drawable.ic_ubike_icon_30
            availableRate <= 40 -> R.drawable.ic_ubike_icon_40
            availableRate <= 50 -> R.drawable.ic_ubike_icon_50
            availableRate <= 60 -> R.drawable.ic_ubike_icon_60
            availableRate <= 70 -> R.drawable.ic_ubike_icon_70
            availableRate <= 80 -> R.drawable.ic_ubike_icon_80
            availableRate <= 90 -> R.drawable.ic_ubike_icon_90
            else -> R.drawable.ic_ubike_icon_100
        }
    }
}

@Composable
fun vectorToBitmapDescriptor(
    vectorResId: Int,
    context: Context,
    scale: Float = 1f
): BitmapDescriptor {
    return remember(vectorResId, context, scale) {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            ?: return@remember BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            (vectorDrawable.intrinsicWidth * scale).toInt(),
            (vectorDrawable.intrinsicHeight * scale).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

private fun getStationDistance(stationLatLng: LatLng, current: Location): String {
    var stationLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
        latitude = stationLatLng.latitude
        longitude = stationLatLng.longitude
    }

    val distance = stationLocation.distanceTo(current)

    return if (distance > 1000) {
        "%.2f".format(distance / 1000).toString() + "公里"
    } else {
        distance.toInt().toString() + "公尺"
    }
}

private val convertTime: (time: String) -> Calendar = { time ->
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+08:00")
    val calendar = Calendar.getInstance().apply {
        setTime(simpleDateFormat.parse(time))
    }
    calendar
}


@Preview(showBackground = true)
@Composable
fun PreviewBottomSheetDialog() {
    val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
        latitude = 0.0
        longitude = 0.0
    }
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.onPrimaryContainer) {
            DialogContent(
                currentLocation = currentLocation,
                completeStationInfo = StationInfo(
                    StationInfoDetail(station_address = "測試地址用", station_name = "測試站名用")
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomRowContent() {
    AppTheme {
        BottomRowContent(
            completeStationInfo = StationInfo(StationInfoDetail()),
            onFavoriteClick = {},
            onShareClick = {},
            onNavigateClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMap() {
//    val viewModel = MainViewModel()
//    AppTheme {
//        MapScreen(viewModel)
//    }
}