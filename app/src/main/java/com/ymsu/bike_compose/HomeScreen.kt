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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.data.StationInfoDetail
import com.ymsu.bike_compose.theme.AppTheme
import com.ymsu.bike_compose.theme.gray_100
import com.ymsu.bike_compose.theme.gray_300
import kotlin.math.abs


@Composable
fun HomeScreen(navController: NavController, stateViewModel: StateViewModel) {
    val state by stateViewModel.state.collectAsStateWithLifecycle()
    ColumnScreen(state, stateViewModel, navController)
}

@Composable
private fun ColumnScreen(
    state: BikeState,
    viewModel: StateViewModel,
    navController: NavController
) {
    val onClick: (StationInfo) -> Unit = { flaskItemWithFavorite ->
        Log.d("", "[onClick] station uid = " + flaskItemWithFavorite.stationInfoDetail.station_uid)
        // navigate to map screen
        // call viewmodel function to set selected station
        Log.d("HomeScreen", "allFavoriteStations call viewModel.setSelectedStation")
        viewModel.setSelectedStation(flaskItemWithFavorite)
        navController.navigate("Map")
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            keyboardController?.hide()
            isExpanded = false
        }
    ) {
        HomeMainView(state, onClick)

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SearchBar(
                modifier = Modifier.offset(y = 110.dp),
                viewModel = viewModel,
                state,
                navController,
                isExpanded,
                {isExpanded = it}
            )
        }
    }
}

@Composable
private fun HomeMainView(
    state: BikeState,
    onClick: (StationInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        var showFavoriteList by remember {
            mutableStateOf(false)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy((-1).dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primary.copy(0.9f))
        ) {
            Text(
                text = "尋找YouBike站點",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(20.dp)
                    .offset(y = 20.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = R.drawable.arc_2_),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(0.9f))
            )

            Column {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    Card(modifier = Modifier
                        .width(100.dp)
                        .height(150.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { showFavoriteList = false }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                                    .padding(8.dp),
                                    imageVector = if (showFavoriteList) Icons.Outlined.NearMe else Icons.Filled.NearMe,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(modifier = Modifier.fillMaxWidth(), text = "鄰近站點", textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Card(modifier = Modifier
                        .width(100.dp)
                        .height(150.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { showFavoriteList = true }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                                    .padding(8.dp),
                                    imageVector = if (showFavoriteList) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(modifier = Modifier.fillMaxWidth(), text = "收藏站點", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(50.dp)
                )

                Text(
                    text = if (showFavoriteList) "收藏站點列表" else "鄰近站點列表",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 20.dp),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (showFavoriteList)
                    FavoriteStationList(state, onClick)
                else
                    BikeStationList(state, onClick)
            }
        }

    }
}

@Composable
fun SearchBar(
    modifier: Modifier,
    viewModel: StateViewModel,
    state: BikeState,
    navController: NavController,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
//    val filterStations = state.searchResults

    val onQueryChange: (String) -> Unit = { query ->
        Log.d("", "[onQueryChange] query = $query")
        viewModel.setQueryString(query)
    }

    val onClick: (StationInfo) -> Unit = { completeStationInfo ->
        Log.d("", "[onClick] station uid = " + completeStationInfo.stationInfoDetail.station_uid)
        // navigate to map screen
        // call viewmodel function to set selected station
        Log.d("HomeScreen", "SearchBar call viewModel.setSelectedStation")
        viewModel.setSelectedStation(completeStationInfo)
        navController.navigate("Map")
    }

    SearchBarDetail(modifier, state, onQueryChange, onClick, isExpanded, onExpandedChange)
}

@Composable
private fun SearchBarDetail(
    modifier: Modifier,
    state: BikeState,
//    filterStations: List<StationInfo>,
    onQueryChange: (String) -> Unit,
    onClick: (StationInfo) -> Unit,
    isExpanded:Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var searchString = state.search

    val filterStations = state.searchResults
    val errorMessage = state.errorMessage
    val loading = state.isLoading

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
                androidx.compose.material3.Text(text = if (state.search.isEmpty())"搜尋站名..." else "")
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
            }),
            modifier = modifier
                .width(350.dp)
                .heightIn(min = 56.dp),
            shape = if (isExpanded) RoundedCornerShape(20.dp, 20.dp) else RoundedCornerShape(20.dp),
            interactionSource =
            remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                // works like onClick
                                var change = !isExpanded
                                onExpandedChange(change)
                                keyboardController?.apply {
                                    if (change) show() else hide()
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
                if (filterStations.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "無符合條件之站點",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (loading) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "載入中...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (errorMessage.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "資料錯誤，請稍後再試",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .clip(
                                shape = RoundedCornerShape(
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            )
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
}

@Composable
private fun SearchStationCard(
    filterStations: List<StationInfo>,
    index: Int,
    onClick: (StationInfo) -> Unit
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
                        text = filterStations[index].stationInfoDetail.station_name.substringAfter(
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

                Row {
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
                            text = filterStations[index].stationInfoDetail.available_bikes.toString(),
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
                            text = filterStations[index].stationInfoDetail.available_e_bikes.toString(),
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
                            text = filterStations[index].stationInfoDetail.available_return.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 20.dp, max = 50.dp)
                            .align(Alignment.Bottom),
                        text = filterStations[index].stationInfoDetail.station_address,
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
fun FavoriteStationList(state: BikeState, onClick: (StationInfo) -> Unit) {
    val lazyListState = rememberLazyListState()

    val isLoading = state.isLoading
    val isError = state.errorMessage.length > 2
    val data = state.favoriteStations
    Log.d("","[FavoriteStationList] isLoading : $isLoading, isError : $isError")

    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 顯示 loading 項目，或正常顯示資料
        itemsIndexed(if (isLoading) List(2) { StationInfo(StationInfoDetail()) } else data) { index, station ->
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
                        .clickable { onClick(data[index]) }
                        .size(width = 200.dp, height = 250.dp)
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = rememberAsyncImagePainter(station.stationInfoDetail.image_url),
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
                            text = if (isLoading) "Loading..." else station.stationInfoDetail.station_name.substringAfter("_"),
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
                            text = if (isLoading) "Loading..." else station.stationInfoDetail.station_address
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
                            text = if (isLoading) "Loading..." else "可借${station.stationInfoDetail.available_bikes + station.stationInfoDetail.available_e_bikes} / 可還: ${station.stationInfoDetail.available_return}"
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

        if (isError) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "載入失敗，請稍後再試。",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (data.isEmpty() && !isError && !isLoading) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "無收藏站點",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}


@Composable
fun BikeStationList(state: BikeState, onClick: (StationInfo) -> Unit) {
    val lazyListState = rememberLazyListState()

    // 判斷 loading 狀態
    val isLoading = state.isLoading
    val isError = state.errorMessage.length > 2
    val data = state.nearFavoriteStations

    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            if (isLoading)
                List(2) { StationInfo(StationInfoDetail()) }
            else
                data
        ) { index, station ->
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
                        onClick(data[index])
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
                        painter = rememberAsyncImagePainter(station.stationInfoDetail.image_url),
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
                        text = if (isLoading) "Loading..." else station.stationInfoDetail.station_name.substringAfter(
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
                        text = if (isLoading) "Loading..." else station.stationInfoDetail.station_address
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
                            text = if (isLoading) "Loading..." else "可借${it.stationInfoDetail.available_bikes + it.stationInfoDetail.available_e_bikes}  " +
                                    "/  可還: ${it.stationInfoDetail.available_return}"
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
        if (isError) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center) {
                    Text(
                        text = "載入失敗，請稍後再試",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchStationCard() {
    val list = mutableListOf<StationInfo>()
    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "YouBike2.0_幸福路753巷口",
                station_address = "幸福路738號(前)"
            ), true, 100.0f
        )
    )
    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "YouBike2.0_幸福路753巷口",
                station_address = "幸福路738號(前)"
            ), true, 100.0f
        )
    )

    AppTheme {
//        SearchStationCard(filterStations = list, index = 0)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchBarDetails() {
    val list = mutableListOf<StationInfo>()
    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    val result = ApiResult.Success(list)

    AppTheme {
//        SearchBarDetail(
//            modifier = Modifier,
//            filterStations = result,
//            onQueryChange = { query ->
//            },
//            onClick = { completeStationInfo -> },
//            false,
//            {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeMainView() {
    val list = mutableListOf<StationInfo>()
    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    list.add(
        StationInfo(
            StationInfoDetail(
                station_name = "測試用站名",
                station_address = "測試用地址測試用地址測試用地址測試用地址"
            ), true, 1f
        )
    )

    val result = ApiResult.Success(list)

    AppTheme {
//        HomeMainView(
//            allFavoriteStations = ApiResult.Success(emptyList()),
//            nearByStations = ApiResult.Error(""), onClick = {}
//        )
    }
}