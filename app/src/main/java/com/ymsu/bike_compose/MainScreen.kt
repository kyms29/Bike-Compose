package com.ymsu.bike_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController,viewModel) }
        composable("detail") { DetailScreen(navController,viewModel)
        composable("settings") { SettingsScreen(navController, viewModel)}}
    }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel){
    Text(text = "Settings")
}

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel){
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = viewModel.text.value, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("detail")},
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colorScheme.surfaceDim
            )
        ) {
            Text("Go to Detail", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun DetailScreen(navController: NavController, viewModel: MainViewModel){
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Detail Screen", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.updateText("Update from detail")
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colorScheme.surfaceDim
            )
        ) {
            Text(text = "Update text and go back",color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun previewMainScreen(){
    MainScreen()
}