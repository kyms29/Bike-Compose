package com.ymsu.bike_compose


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ymsu.bike_compose.theme.AppTheme

// 列出我的最愛

@Composable
fun FavoriteScreen(viewModel: MainViewModel) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), text = "Favorite"
    )
    //

//    val favoriteList = viewModel.favoriteStations
}

@Preview(showBackground = true)
@Composable
fun previewFavorite(){
    AppTheme {
//        FavoriteScreen()
    }
}