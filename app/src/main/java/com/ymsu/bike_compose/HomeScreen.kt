package com.ymsu.bike_compose


import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.CompleteStationInfo
import com.ymsu.bike_compose.data.FlaskItemWithFavorite
import com.ymsu.bike_compose.data.StationAddress
import com.ymsu.bike_compose.data.StationInfoFromFlaskItem
import com.ymsu.bike_compose.data.StationInfoItem
import com.ymsu.bike_compose.data.StationName
import com.ymsu.bike_compose.theme.AppTheme
import com.ymsu.bike_compose.theme.gray_100
import com.ymsu.bike_compose.theme.gray_300
import kotlin.math.abs


@Composable
fun HomeScreen(viewModel: MainViewModel, navController:NavController) {
    val allFavoriteStations by viewModel.allFavoriteStations.collectAsStateWithLifecycle()
    val nearByStations by viewModel.nearByStationWithFavorite.collectAsStateWithLifecycle()
    ColumnScreen(allFavoriteStations, nearByStations,viewModel,navController)
}

@Composable
private fun ColumnScreen(
    allFavoriteStations: List<FlaskItemWithFavorite>,
    nearByStations: List<FlaskItemWithFavorite>,
    viewModel: MainViewModel,
    navController: NavController
) {
    val onClick:(FlaskItemWithFavorite) -> Unit = { flaskItemWithFavorite ->
        Log.d("","[onClick] station uid = "+flaskItemWithFavorite.stationInfoFromFlaskItem.station_uid)
        // navigate to map screen
        // call viewmodel function to set selected station
        viewModel.setSelectedStation(flaskItemWithFavorite)
        navController.navigate("Map")
    }

    HomeMainView(nearByStations, allFavoriteStations, onClick)

    Box(modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center) {
        SearchBar(modifier = Modifier
            .offset(y = 110.dp), viewModel = viewModel,navController
        )
    }
}

@Composable
private fun HomeMainView(
    nearByStations: List<FlaskItemWithFavorite>,
    allFavoriteStations: List<FlaskItemWithFavorite>,
    onClick: (FlaskItemWithFavorite) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy((-1).dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(MaterialTheme.colorScheme.primary.copy(0.9f))
        ) {
            Text(
                text = "尋找YouBike站點",
                color = Color.DarkGray,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(20.dp)
                    .offset(y = 20.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(26.dp)
        )

        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = "附近的站點",
            color = Color.DarkGray,
            style = MaterialTheme.typography.titleMedium,
        )

        BikeStationList(stations = nearByStations,onClick)

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "收藏站點",
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 20.dp),
            style = MaterialTheme.typography.titleMedium,
        )

        FavoriteStationList(stations = allFavoriteStations,onClick)
    }
}

@Composable
fun SearchBar(
    modifier: Modifier,
    viewModel: MainViewModel,
    navController: NavController
) {


    val filterStations by viewModel.filterStations.collectAsState()

    val onQueryChange: (String)->Unit = {query->
        Log.d("","[onQueryChange] query = $query")
        viewModel.queryStations(query)
    }

    val onClick:(FlaskItemWithFavorite) -> Unit = { completeStationInfo ->
        Log.d("","[onClick] station uid = "+completeStationInfo.stationInfoFromFlaskItem.station_uid)
        // navigate to map screen
        // call viewmodel function to set selected station
        viewModel.setSelectedStation(completeStationInfo)
        navController.navigate("Map")
    }

    SearchBarDetail(modifier, filterStations, onQueryChange,onClick)
}

@Composable
private fun SearchBarDetail(
    modifier: Modifier,
    filterStations: List<FlaskItemWithFavorite>,
    onQueryChange: (String) -> Unit,
    onClick: (FlaskItemWithFavorite)->Unit
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    var searchString by remember {
        mutableStateOf("")
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box {
        TextField(
            value = searchString,
            onValueChange = {
                searchString = it
                onQueryChange(it)
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            placeholder = {
                androidx.compose.material3.Text(text = "搜尋站名...")
            },
            modifier = modifier
                .width(350.dp)
                .heightIn(min = 56.dp)
                .clickable { keyboardController?.hide() },
            shape = if (isExpanded) RoundedCornerShape(20.dp, 20.dp)
            else RoundedCornerShape(20.dp),
            interactionSource =
            remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                // works like onClick
                                isExpanded = !isExpanded
                                keyboardController?.apply {
                                    if (isExpanded) show() else hide()
                                }
                            }
                        }
                    }
                }
        )

        val height by animateDpAsState(
            targetValue = if (isExpanded) 220.dp else 0.dp,
            animationSpec = tween(durationMillis = 500), label = "heightAnimation"
        )

        if (isExpanded) {
            Card(
                modifier = Modifier
                    .offset(y = 156.dp)
                    .width(350.dp)
                    .height(height)
                    .zIndex(-1f),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {

                    items(filterStations.size) { index ->
                        // TODO: 這邊要設計成card + 利用viewmodel的all city list 列出相關的站名
                        SearchStationCard(filterStations, index, onClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchStationCard(
    filterStations: List<FlaskItemWithFavorite>,
    index: Int,
    onClick: (FlaskItemWithFavorite) -> Unit
) {
    Card(
        modifier = Modifier
            .width(350.dp)
            .clickable {
                // handle click here
                onClick(filterStations[index])
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                Row {
                    Text(
                        modifier = Modifier.weight(1.5f),
                        text = filterStations[index].stationInfoFromFlaskItem.station_name.substringAfter(
                            "_"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val distanceString = if (filterStations[index].distance > 1000) {
                        "%.2f".format(filterStations[index].distance / 1000).toString() + "公里"
                    } else {
                        filterStations[index].distance.toInt().toString() + "公尺"
                    }

                    Text(
                        modifier = Modifier
                            .weight(0.5f)
                            .align(Alignment.CenterVertically),
                        text = distanceString,
                        style = MaterialTheme.typography.bodySmall, color = Color.Gray,
                        textAlign = TextAlign.Right
                    )
                }

                Row{
                    Row(modifier = Modifier.weight(0.3f)) {
                        Icon(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.CenterVertically)
                                .border(width = 1.dp, color = Color.Gray, shape = CircleShape)
                                .padding(4.dp),
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "Bike",
                            tint = Color.Gray
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = filterStations[index].stationInfoFromFlaskItem.available_bikes.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(modifier = Modifier.weight(0.3f)) {
                        Icon(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.CenterVertically)
                                .border(width = 1.dp, color = Color.Gray, shape = CircleShape)
                                .padding(4.dp),
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Electric",
                            tint = Color.Gray
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = filterStations[index].stationInfoFromFlaskItem.available_e_bikes.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(modifier = Modifier.weight(0.3f)) {
                        Icon(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.CenterVertically)
                                .border(width = 1.dp, color = Color.Gray, shape = CircleShape)
                                .padding(4.dp),
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = "Parking",
                            tint = Color.Gray
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = filterStations[index].stationInfoFromFlaskItem.available_return.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 20.dp, max = 50.dp)
                            .align(Alignment.Bottom),
                        text = filterStations[index].stationInfoFromFlaskItem.station_address,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteStationList(stations: List<FlaskItemWithFavorite>, onClick: (FlaskItemWithFavorite) -> Unit) {
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

        itemsIndexed(if (isLoading) List(1) {
            FlaskItemWithFavorite(
                StationInfoFromFlaskItem()
            )
        } else stations) { index, station ->
            val layoutInfo = lazyListState.layoutInfo
            val viewportCenter =
                (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
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
                        .clickable {
                            onClick(stations[index])
                        }
                        .size(width = 200.dp, height = 250.dp)
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                            text = if (isLoading) "Loading..." else station.stationInfoFromFlaskItem.station_name.substringAfter(
                                "_"
                            ),
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
                            text = if (isLoading) "Loading..." else station.stationInfoFromFlaskItem.station_address
                        )
                        station.let {
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
                                text = if (isLoading) "Loading..." else "可借${it.stationInfoFromFlaskItem.available_bikes+it.stationInfoFromFlaskItem.available_e_bikes}" +
                                        "  /  可還: ${it.stationInfoFromFlaskItem.available_return}"
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
fun BikeStationList(stations: List<FlaskItemWithFavorite>, onClick: (FlaskItemWithFavorite) -> Unit) {
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

        itemsIndexed(
            if (isLoading) List(10) { FlaskItemWithFavorite(StationInfoFromFlaskItem())
        } else stations) { index, station ->
            val layoutInfo = lazyListState.layoutInfo
            val viewportCenter =
                (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
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
                    .clickable {
                        onClick(stations[index])
                    }
                    .size(width = 200.dp, height = 250.dp)
                    .padding(horizontal = 8.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                        text = if (isLoading) "Loading..." else station.stationInfoFromFlaskItem.station_name.substringAfter(
                            "_"
                        ),
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
                        text = if (isLoading) "Loading..." else station.stationInfoFromFlaskItem.station_address
                    )
                    station.let {
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
                            text = if (isLoading) "Loading..." else "可借${it.stationInfoFromFlaskItem.available_bikes+it.stationInfoFromFlaskItem.available_e_bikes}  " +
                                    "/  可還: ${it.stationInfoFromFlaskItem.available_return}"
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
fun PreviewSearchStationCard(){
    val list = mutableListOf<CompleteStationInfo>()
    list.add(
        CompleteStationInfo(
            StationInfoItem(
                StationName = StationName(Zh_tw = "YouBike2.0_幸福路753巷口"),
                StationAddress = StationAddress(Zh_tw = "幸福路738號(前)")
            ), AvailableInfoItem(), true, 100.0f
        )
    )
    list.add(
        CompleteStationInfo(
            StationInfoItem(
                StationName = StationName(Zh_tw = "YouBike2.0_幸福路753巷口"),
                StationAddress = StationAddress(Zh_tw = "幸福路738號(前)")
            ), AvailableInfoItem(), true, 100.0f
        )
    )

    AppTheme {
//        SearchStationCard(filterStations = list, index = 0)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchBarDetails(){
    val list = mutableListOf<FlaskItemWithFavorite>()
    list.add(
        FlaskItemWithFavorite(
            StationInfoFromFlaskItem(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        FlaskItemWithFavorite(
            StationInfoFromFlaskItem(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        FlaskItemWithFavorite(
            StationInfoFromFlaskItem(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    AppTheme {
        SearchBarDetail(modifier = Modifier, filterStations = list.toList(), onQueryChange = {
                query ->
            }, onClick = {completeStationInfo ->  })
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeMainView() {
    val list = mutableListOf<FlaskItemWithFavorite>()
    list.add(
        FlaskItemWithFavorite(
            StationInfoFromFlaskItem(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        FlaskItemWithFavorite(
            StationInfoFromFlaskItem(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        FlaskItemWithFavorite(
            StationInfoFromFlaskItem(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    AppTheme {
        HomeMainView(allFavoriteStations = list, nearByStations = list, onClick = {})
    }
}