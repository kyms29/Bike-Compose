package com.ymsu.bike_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ymsu.bike_compose.theme.AppTheme
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BikeComposeApp()
        }
    }
}

@Composable
fun BikeComposeApp() {
    AppTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "Map",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("Map") { MapScreen() }
                composable("Favorite") { FavoriteScreen() }
                composable("Settings") { SettingsScreen() }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf("Favorite", "Map", "Settings")
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.primary
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.surfaceVariant,
                selected = currentRoute == item,
                onClick = {
                    navController.navigate(item) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (item) {
                            "Favorite" -> Icons.Default.Favorite
                            "Map" -> Icons.Default.LocationOn
                            "Settings" -> Icons.Default.Settings
                            else -> Icons.Default.Settings
                        },
                        contentDescription = item
                    )
                },
                label = { Text(text = item) })
        }
    }
}