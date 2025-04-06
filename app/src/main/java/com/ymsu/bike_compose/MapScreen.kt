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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.data.StationInfoDetail
import com.ymsu.bike_compose.theme.AppTheme
import com.ymsu.bike_compose.theme.orange
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "[MapScreen]"

@Composable
fun MapScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val mapLatLng by viewModel.mapLatLng.collectAsStateWithLifecycle()
        val userLatLng by viewModel.userLatLng.collectAsStateWithLifecycle()

        var zoomLevel by remember { mutableFloatStateOf(16f) }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mapLatLng, zoomLevel)
        }
        val circleRange by viewModel.range.collectAsStateWithLifecycle()
        val nearByStationInfos by viewModel.nearByStationWithFavorite.collectAsStateWithLifecycle()
        val selectedStationFromHomeScreen by viewModel.selectedStation.collectAsStateWithLifecycle()

        LaunchedEffect(mapLatLng) {
            Log.d(TAG, "[MapScreen] LaunchedEffect mapLatLng : ${mapLatLng.toString()}")
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(mapLatLng, zoomLevel)
            )
        }

        LaunchedEffect(cameraPositionState.isMoving) {
            snapshotFlow { cameraPositionState.isMoving }
                .distinctUntilChanged()
                .collect{ isMoving ->
                    Log.d(TAG,"[MapScreen] collect moving state : ${isMoving}")
                    if(!isMoving) {
                        val position = cameraPositionState.position.target
                        Log.d(TAG, "[MapScreen ]cameraPositionState is changed to: ${position.toString()}")
                        viewModel.updateCurrentLatLng(position.latitude, position.longitude)
                        zoomLevel = cameraPositionState.position.zoom
                    }
                }
        }

        val sortedStations = remember(nearByStationInfos) {
            when (nearByStationInfos) {
                is ApiResult.Success -> {
                    val stations = (nearByStationInfos as ApiResult.Success<List<StationInfo>>).data
                    stations.sortedBy { station ->
                        calculateDistance(
                            LatLng(
                                cameraPositionState.position.target.latitude,
                                cameraPositionState.position.target.longitude
                            ),
                            LatLng(station.stationInfoDetail.lat, station.stationInfoDetail.lng)
                        )
                    }
                }
                else -> emptyList()
            }
        }

        Log.d(TAG, "sortedLocations = " + sortedStations.size)

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
                    radius = circleRange.toDouble()
                )

                MarkerContents(
                    LatLng(
                        cameraPositionState.position.target.latitude,
                        cameraPositionState.position.target.longitude
                    ),
                    sortedStations,
                    selectedStationFromHomeScreen,
                    { viewModel.setSelectedStation(null) },
                    { uid -> viewModel.clickFavorite(uid) }
                )
            }

            FloatingActionButton(
                onClick = {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
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

private fun calculateDistance(from: LatLng, to: LatLng): Double {
    val earthRadius = 6371.0

    val latDiff = Math.toRadians(to.latitude - from.latitude)
    val lngDiff = Math.toRadians(to.longitude - from.longitude)

    val a = sin(latDiff / 2).pow(2) + cos(Math.toRadians(from.latitude)) *
            cos(Math.toRadians(to.latitude)) * sin(lngDiff / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//    Log.d(TAG, "[calculateDistance] = ${earthRadius * c}")
    return earthRadius * c
}

@Composable
@GoogleMapComposable
private fun MarkerContents(
    center: LatLng,
    sortedLocations: List<StationInfo>,
    selectedStationFromHomeScreen: StationInfo?,
    handleSelectStationInfo: () -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    val context = LocalContext.current
    var showStationInfo by remember { mutableStateOf(false) }
    var selectedStationInfo by remember { mutableStateOf<StationInfo?>(null)}

    LaunchedEffect(selectedStationFromHomeScreen) {
        selectedStationFromHomeScreen?.apply {
            showStationInfo = true
            selectedStationInfo = selectedStationFromHomeScreen
            handleSelectStationInfo()
        }
    }

    val markers = remember {
        mutableStateListOf<StationInfo>()
    }

    LaunchedEffect(sortedLocations) {
        markers.removeIf { station ->
            calculateDistance(
                center,
                LatLng(station.stationInfoDetail.lat, station.stationInfoDetail.lng)
            ) > (1).toDouble()
        }

        sortedLocations.forEach { station ->
            delay(30)
            markers.add(station)
        }

        Log.d(TAG, "[MarkerContents] markers size = ${markers.size}")
    }

    markers.forEach { station ->
        key(station.stationInfoDetail.station_uid) {
            val icon = getRateIcon(
                station.stationInfoDetail.available_bikes + station.stationInfoDetail.available_e_bikes,
                station.stationInfoDetail.available_return
            )
            var markerSize = if (selectedStationInfo?.stationInfoDetail?.station_uid == station.stationInfoDetail.station_uid) 1.3f else 1f
            Marker(
                state = rememberMarkerState(position = LatLng(station.stationInfoDetail.lat, station.stationInfoDetail.lng)),
                title = station.stationInfoDetail.station_name,
                onClick = {
                    markerSize = 1.3f
                    showStationInfo = true
                    selectedStationInfo = station
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

    if (showStationInfo) {
        Log.d(TAG, "showStationInfo")
        BottomSheetDialog(
            selectedStationInfo,
            onDismiss = {
                showStationInfo = false
                selectedStationInfo = null
            },
            onFavoriteClick = onFavoriteClick,
            onShareClick = {
                val sendIntent: Intent = Intent().apply {
                    type = "text/plain"
                    action = Intent.ACTION_SEND

                    val text = selectedStationInfo?.let { info ->
                        val mapUri = "https://www.google.com/maps/dir/?api=1&destination=" + info.stationInfoDetail.lat + ","+info.stationInfoDetail.lng
                        info.stationInfoDetail.station_name + "有" +
                                (info.stationInfoDetail.available_bikes + info.stationInfoDetail.available_e_bikes).toString() + "可借" +
                                info.stationInfoDetail.available_return.toString() + "可還" + "，地點在$mapUri"
                    } ?: "尚未選擇站點"

                    putExtra(Intent.EXTRA_TEXT,text)

                }
                sendIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    context.getString(R.string.share_subject)
                )
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            },
            onNavigateClick = {
               selectedStationInfo?.let { info ->
                   val uri = Uri.parse("google.navigation:q=" + info.stationInfoDetail.lat + "," + info.stationInfoDetail.lng + "&mode=w")
                   val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                   mapIntent.setPackage("com.google.android.apps.maps")
                   context.startActivity(mapIntent)
               }
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetDialog(
    stationInfo: StationInfo?,
    onDismiss: () -> Unit,
    onFavoriteClick: (String) -> Unit,
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
        DialogContent(stationInfo)
        BottomRowContent(stationInfo, onFavoriteClick, onShareClick, onNavigateClick)
    }
}

@Composable
private fun DialogContent(stationInfo: StationInfo?) {
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
                        text = stationInfo?.let { info ->
                            info.stationInfoDetail.available_bikes.toString() + "可借"
                        }?: "0可借"
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
                        text = stationInfo?.let { info ->
                            info.stationInfoDetail.available_e_bikes.toString() + "可借"
                        }?: "0可借"
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
                        text = stationInfo?.let { info ->
                            info.stationInfoDetail.available_return.toString() + "可還"
                        } ?: "0可還"
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
                (currentTimeDate.time - convertTime(stationInfo?.stationInfoDetail?.update_time ?: "0").time.time) / 1000

            Text(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .weight(1f),
                text = diff.toInt().toString() + "秒前更新",
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 12.sp
            )

            val distanceString = stationInfo?.let { info ->
                if (info.distance > 1000) {
                    "%.2f".format(info.distance / 1000).toString() + "公里"
                } else {
                    info.distance.toInt().toString() + "公尺"
                }
            } ?: ""

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
            text = stationInfo?.stationInfoDetail?.station_name?.substringAfter("_") ?: ""
        )
        Text(
            modifier = Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            text = stationInfo?.stationInfoDetail?.station_address ?: ""
        )
    }
}

@Composable
private fun BottomRowContent(
    stationInfo: StationInfo?,
    onFavoriteClick: (String) -> Unit,
    onShareClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(stationInfo?.isFavorite ?: false)  }
    Log.d(
        TAG,
        "[BottomRowContent] Create or recompose , isFavorite = ${stationInfo?.isFavorite}"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Button(
            onClick = {
                onFavoriteClick(stationInfo?.stationInfoDetail?.station_uid ?: "")
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
                stationInfo = StationInfo(
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
            stationInfo = StationInfo(StationInfoDetail()),
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