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
}

@Preview(showBackground = true)
@Composable
fun previewSettings(){
    AppTheme {
        SettingsScreen()
    }
}