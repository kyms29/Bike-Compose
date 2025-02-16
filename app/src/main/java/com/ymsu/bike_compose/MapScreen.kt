package com.ymsu.bike_compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
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
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.FullyStationInfo
import com.ymsu.bike_compose.data.StationAddress
import com.ymsu.bike_compose.data.StationInfoItem
import com.ymsu.bike_compose.data.StationName
import com.ymsu.bike_compose.theme.AppTheme
import com.ymsu.bike_compose.theme.orange

private const val TAG = "[MapScreen]"

@Composable
fun MapScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var currentLocation by remember { mutableStateOf(LatLng(1.3521, 103.8198)) }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentLocation, 12f)
        }

        val context = LocalContext.current

        val locationHandler = remember { LocationHandler(context) }
        LaunchedEffect(Unit) {
            locationHandler.getCurrentLocation { location ->
                if (location != null) {
                    Log.d(
                        TAG,
                        "get current location: ${location?.latitude} , ${location?.longitude}"
                    )
                    currentLocation = LatLng(location.latitude, location.longitude)
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLocation,
                            16f
                        )
                    )
                    viewModel.fetchNearByStationInfo(currentLocation.latitude, currentLocation.longitude)
                    viewModel.fetchNearByAvailableInfo(currentLocation.latitude, currentLocation.longitude)
                }
            }
        }

        LaunchedEffect(cameraPositionState.isMoving) {
            if (!cameraPositionState.isMoving) {
                val position = cameraPositionState.position.target
                Log.d(
                    TAG,
                    "cameraPositionState change to: ${position.latitude}, ${position.longitude}"
                )
                viewModel.fetchNearByStationInfo(position.latitude, position.longitude)
                viewModel.fetchNearByAvailableInfo(position.latitude, position.longitude)
            }
        }

        val combinedInfo by viewModel.nearByCombinedInfo.collectAsState(emptyList())
        var showStationInfo by remember { mutableStateOf(false) }
        var selectedFullyStationInfo by remember { mutableStateOf(FullyStationInfo(StationInfoItem(), AvailableInfoItem())) }
        var selectedMarker by remember { mutableStateOf<LatLng?>(null) }
        val markerSizes = remember { mutableStateMapOf<LatLng,Float>() }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false
                ),
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Circle(
                    center = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude),
                    fillColor = Color.LightGray.copy(alpha = 0.5f),
                    strokeColor = Color.LightGray.copy(alpha = 0.5f),
                    radius = 1000.0
                )

                combinedInfo.forEach { (stationInfo, availableInfo) ->
                    val icon = getRateIcon(availableInfo.AvailableRentBikes, availableInfo.AvailableReturnBikes)
                    val markerPosition = LatLng(
                        stationInfo.StationPosition.PositionLat,
                        stationInfo.StationPosition.PositionLon)
                    val markerSize = markerSizes[markerPosition]?:1f
                    Marker(
                        state = MarkerState(
                            position = markerPosition
                        ),
                        title = stationInfo.StationName.Zh_tw,
                        onClick = {
                            selectedMarker = markerPosition
                            markerSizes[markerPosition] = 1.5f
                            showStationInfo = true
                            selectedFullyStationInfo = FullyStationInfo(stationInfo, availableInfo)
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
                BottomSheetDialog(selectedFullyStationInfo,
                    onDismiss = {
                        showStationInfo = false
                        selectedMarker?.let { markerSizes[it] = 1f }
                        selectedMarker = null
                    }
                )
            }

            FloatingActionButton(
                onClick = { cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLocation,16f))},
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                shape = CircleShape,
                containerColor = Color.White){
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "mylocation")
            }
        }
    }
}

private fun getRateIcon(availableRent: Int = 0, availableReturn: Int = 0): Int {
    val availableRate =
        ((availableRent.toFloat() / (availableRent.toFloat() + availableReturn.toFloat())) * 100).toInt()

    return when {
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

@Composable
fun vectorToBitmapDescriptor(vectorResId: Int, context: Context, scale: Float = 1f): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return BitmapDescriptorFactory.defaultMarker()

    val bitmap = Bitmap.createBitmap(
        (vectorDrawable.intrinsicWidth*scale).toInt(),
        (vectorDrawable.intrinsicHeight*scale).toInt(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetDialog(fullyStationInfo: FullyStationInfo, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        shape = RectangleShape,
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        DialogContent(fullyStationInfo)
        BottomRowContent()
    }
}

@Composable
private fun DialogContent(fullyStationInfo: FullyStationInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(modifier = Modifier.weight(1f) ) {
                Box(modifier = Modifier.width(100.dp)
                    .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))){
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        text = fullyStationInfo.availableInfoItem.AvailableRentBikesDetail.GeneralBikes.toString()+"可借"
                    )
                }
                Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = "Bike",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(shape = RoundedCornerShape(16.dp), color =  MaterialTheme.colorScheme.background)
                        .padding(2.dp))
            }

            Box(modifier = Modifier.weight(1f) ) {
                Box(modifier = Modifier.width(100.dp)
                    .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        text = fullyStationInfo.availableInfoItem.AvailableRentBikesDetail.ElectricBikes.toString()+"可借"
                    )
                }
                Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = "Bike",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(shape = RoundedCornerShape(16.dp), color =  MaterialTheme.colorScheme.background)
                        .padding(2.dp))
            }

            Box(modifier = Modifier.weight(1f) ) {
                Row(modifier = Modifier.width(100.dp)
                    .background(color = orange, shape = RoundedCornerShape(8.dp))) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        text = fullyStationInfo.availableInfoItem.AvailableReturnBikes.toString()+"可還"
                    )
                }
                Icon(imageVector = Icons.Default.LocalParking, contentDescription = "Bike",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 10.dp, y = (-10).dp)
                        .background(shape = RoundedCornerShape(16.dp), color = orange)
                        .padding(2.dp))
            }
        }

        Text(
            modifier = Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            text = fullyStationInfo.stationInfoItem.StationName.Zh_tw
        )
        Text(
            modifier = Modifier.padding(top = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            text = fullyStationInfo.stationInfoItem.StationAddress.Zh_tw
        )

        Text(modifier = Modifier.padding(top = 20.dp), text = "距離N公尺", color = MaterialTheme.colorScheme.surfaceVariant)
    }

}

@Composable
private fun BottomRowContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Image(imageVector = Icons.Default.Favorite, contentDescription = "favorite")
                Text(text = "收藏", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Image(imageVector = Icons.Default.Share, contentDescription = "share")
                Text(text = "分享", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Image(imageVector = Icons.Default.Navigation, contentDescription = "navigation")
                Text(text = "導航", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomSheetDialog() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.onPrimaryContainer) {
            DialogContent(
                fullyStationInfo = FullyStationInfo(
                    StationInfoItem(StationAddress = StationAddress(Zh_tw = "測試地址用"), StationName = StationName(Zh_tw = "測試站名用")),
                    AvailableInfoItem()
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomRowContent() {
    AppTheme {
        BottomRowContent()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMap() {
    val viewModel = MainViewModel()
    AppTheme {
        MapScreen(viewModel)
    }
}