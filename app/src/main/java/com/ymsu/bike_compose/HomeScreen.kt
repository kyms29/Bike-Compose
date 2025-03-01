package com.ymsu.bike_compose


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.CompleteStationInfo
import com.ymsu.bike_compose.data.StationAddress
import com.ymsu.bike_compose.data.StationInfoItem
import com.ymsu.bike_compose.data.StationName
import com.ymsu.bike_compose.theme.AppTheme
import com.ymsu.bike_compose.theme.gray_100
import com.ymsu.bike_compose.theme.gray_300
import kotlin.math.abs


@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val allFavoriteStations by viewModel.allFavoriteStations.collectAsStateWithLifecycle()
    val nearByStations by viewModel.completeStationInfo.collectAsStateWithLifecycle()
    Screen(allFavoriteStations,nearByStations)
}

@Composable
private fun Screen(allFavoriteStations: List<CompleteStationInfo>, nearByStations: List<CompleteStationInfo>) {
    Scaffold(
        topBar = {
            TopBarWithSearch()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        modifier = Modifier.padding(start = 20.dp),
                        text = "附近的站點",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    BikeStationList(stations = nearByStations)
                }

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "收藏站點",
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 20.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    FavoriteStationList(stations = allFavoriteStations)
                }
            }
        }
    }
}

@Composable
private fun TopBarWithSearch() {
    Column(
        verticalArrangement = Arrangement.spacedBy((-1).dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(MaterialTheme.colorScheme.primary.copy(0.9f))
    ) {
        Text(
            text = "尋找YouBike站點", color = Color.DarkGray, fontSize = 24.sp, modifier = Modifier
                .padding(20.dp)
                .offset(y = 20.dp)
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SearchBar(modifier = Modifier.offset(y = 40.dp))
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier
) {
    TextField(
        value = "",
        onValueChange = {},
        trailingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "")
        },
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        placeholder = {
            androidx.compose.material3.Text(text = "Search...")
        },
        modifier = modifier
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun FavoriteStationList(stations: List<CompleteStationInfo>) {
    val lazyListState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = stations) {
        isLoading = stations.isEmpty()
    }

    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        itemsIndexed(if (isLoading) List(1){ CompleteStationInfo(StationInfoItem(), AvailableInfoItem())} else stations) { index, station ->
            val layoutInfo = lazyListState.layoutInfo
            val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
            val scale = remember { mutableStateOf(1f) }

            itemInfo?.let {
                val itemCenter = it.offset + (it.size / 2f) // 計算 item 中心點
                val distance = abs(viewportCenter - itemCenter) // 計算距離中心點的距離
                val scaleValue = (1.2f - (distance / viewportCenter)).coerceIn(1f, 1.2f)
                scale.value = scaleValue
            }
            val animatedScale by animateFloatAsState(
                targetValue = scale.value,
                animationSpec = tween(durationMillis = 100)
            )
            Box {
                Card(
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .size(width = 200.dp, height = 250.dp)
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()){
                        Image(
                            painter = painterResource(id = R.drawable.pic),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .size(100.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally)
                                .placeholder(
                                    visible = isLoading,
                                    color = gray_300,
                                    highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                )
                        )
                        Text(
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                                .placeholder(
                                    visible = isLoading,
                                    color = gray_300,
                                    highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                ),
                            text = if (isLoading) "Loading..." else station.stationInfoItem.StationName.Zh_tw.substringAfter("_"),
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                                .placeholder(
                                    visible = isLoading,
                                    color = gray_300,
                                    highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = if (isLoading) "Loading..." else station.stationInfoItem.StationAddress.Zh_tw
                        )
                        station.availableInfoItem.let {
                            Text(
                                modifier = Modifier
                                    .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                                    .placeholder(
                                        visible = isLoading,
                                        color = gray_300,
                                        highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray,
                                text = if (isLoading) "Loading..." else "可借${it.AvailableRentBikes}  /  可還: ${it.AvailableReturnBikes}"
                            )
                            val distanceString = if (station.distance > 1000) {
                                "%.2f".format(station.distance / 1000).toString() + "公里"
                            } else {
                                station.distance.toInt().toString() + "公尺"
                            }

                            Text(
                                modifier = Modifier
                                    .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                                    .placeholder(
                                        visible = isLoading,
                                        color = gray_300,
                                        highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray,
                                text = if (isLoading) "Loading..." else distanceString
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "favorite",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-15).dp, y = (-10).dp)
                )
            }
        }
    }
}

@Composable
fun BikeStationList(stations: List<CompleteStationInfo>) {
    val lazyListState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = stations) {
        isLoading = stations.isEmpty()
    }

    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        itemsIndexed(if (isLoading) List(10){ CompleteStationInfo(StationInfoItem(), AvailableInfoItem())} else stations) { index, station ->
            val layoutInfo = lazyListState.layoutInfo
            val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
            val scale = remember { mutableStateOf(1f) }

            itemInfo?.let {
                val itemCenter = it.offset + (it.size / 2f) // 計算 item 中心點
                val distance = abs(viewportCenter - itemCenter) // 計算距離中心點的距離
                val scaleValue = (1.2f - (distance / viewportCenter)).coerceIn(1f, 1.2f)
                scale.value = scaleValue
            }
            val animatedScale by animateFloatAsState(
                targetValue = scale.value,
                animationSpec = tween(durationMillis = 100)
            )

            Card(
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(width = 200.dp, height = 250.dp)
                    .padding(horizontal = 8.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()){
                    Image(
                        painter = painterResource(id = R.drawable.pic),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                            .size(100.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .placeholder(
                                visible = isLoading,
                                color = gray_300,
                                highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                            )
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                            .placeholder(
                                visible = isLoading,
                                color = gray_300,
                                highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                            ),
                        text = if (isLoading) "Loading..." else station.stationInfoItem.StationName.Zh_tw.substringAfter("_"),
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                            .placeholder(
                                visible = isLoading,
                                color = gray_300,
                                highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = if (isLoading) "Loading..." else station.stationInfoItem.StationAddress.Zh_tw
                    )
                    station.availableInfoItem.let {
                        Text(
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                                .placeholder(
                                    visible = isLoading,
                                    color = gray_300,
                                    highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            text = if (isLoading) "Loading..." else "可借${it.AvailableRentBikes}  /  可還: ${it.AvailableReturnBikes}"
                        )
                        val distanceString = if (station.distance > 1000) {
                            "%.2f".format(station.distance / 1000).toString() + "公里"
                        } else {
                            station.distance.toInt().toString() + "公尺"
                        }

                        Text(
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                                .placeholder(
                                    visible = isLoading,
                                    color = gray_300,
                                    highlight = PlaceholderHighlight.shimmer(highlightColor = gray_100)
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            text = if (isLoading) "Loading..." else distanceString
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun previewFavorite() {
    val list = mutableListOf<CompleteStationInfo>()
    list.add(
        CompleteStationInfo(
            StationInfoItem(
                StationName = StationName(Zh_tw = "測試用站名"),
                StationAddress = StationAddress(Zh_tw = "測試用地址測試用地址測試用地址測試用地址")
            ), AvailableInfoItem(), true
        )
    )
    list.add(
        CompleteStationInfo(
            StationInfoItem(
                StationName = StationName(Zh_tw = "測試用站名"),
                StationAddress = StationAddress(Zh_tw = "測試用地址測試用地址測試用地址測試用地址")
            ), AvailableInfoItem(), true
        )
    )
    list.add(
        CompleteStationInfo(
            StationInfoItem(
                StationName = StationName(Zh_tw = "測試用站名"),
                StationAddress = StationAddress(Zh_tw = "測試用地址測試用地址測試用地址測試用地址")
            ), AvailableInfoItem(), true
        )
    )
    AppTheme {
        Screen(allFavoriteStations = list, nearByStations = list)
//        Column() {
//            topBarWithSearch()
//            BikeStationList(list)
//        }
    }
}