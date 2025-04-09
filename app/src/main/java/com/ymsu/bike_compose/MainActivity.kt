package com.ymsu.bike_compose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.ymsu.bike_compose.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "[MainActivity]"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val hasNetwork = MutableLiveData(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isNetworkConnect()
        setContent {
            CheckPermissions(hasNetwork, onRetry = { isNetworkConnect() })
        }
    }

    private fun goToSettingsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun CheckPermissions(
        hasNetwork: MutableLiveData<Boolean>,
        onRetry: () -> Unit,
    ) {
        val locationPermissionsState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
        var showSettingDialog by remember { mutableStateOf(false) }
        var isPermissionGranted = locationPermissionsState.permissions.all { it.status.isGranted }

        LaunchedEffect(Unit) {
            if (!isPermissionGranted) {
                locationPermissionsState.launchMultiplePermissionRequest()
                showSettingDialog = true
            }
        }

        if (isPermissionGranted) {
            BikeComposeApp(hasNetwork, onRetry)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text("無法使用位置服務，請至設定中授予應用程式位置權限")

                if (showSettingDialog) {
                    GoToSettingsDialog(
                        onDismiss = {
                            showSettingDialog = false
                            Log.d(TAG,"[onDismiss] showSettingDialog = ${showSettingDialog}")
                                    },
                        onConfirm = { goToSettingsPage() }
                    )
                }
            }
        }
    }

    @Composable
    private fun GoToSettingsDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("需要位置權限") },
            text = { Text("應用程式需要使用精確位置，請至設定給予位置權限") },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                { Text(text = "前往設定", color = MaterialTheme.colorScheme.surfaceVariant) }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                { Text(text = "取消", color = MaterialTheme.colorScheme.surfaceVariant) }
            }
        )
    }

    private fun isNetworkConnect() {
        Log.d(TAG, "isNetworkConnect function called ")
        val connectivityManager =
            baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val status = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        hasNetwork.value = status
    }
}

@Composable
fun BikeComposeApp(hasNetwork: LiveData<Boolean>, onRetry: () -> Unit) {
    val networkStatus by hasNetwork.observeAsState(true)

    AppTheme {
        val navController = rememberNavController()
        val viewModel: MainViewModel = hiltViewModel()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screens.Map.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screens.Map.route) { MapScreen(viewModel) }
                composable(Screens.Home.route) { HomeScreen(viewModel, navController) }
                composable(Screens.Settings.route) { SettingsScreen(viewModel) }
            }

            Log.d(TAG, "[BikeComposeApp] networkStatus = $networkStatus")

            if (!networkStatus) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { }) {

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "請檢查網路連線",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onRetry() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "重新整理")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screens.Home,Screens.Map,Screens.Settings)
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.primary
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.surfaceVariant,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) })
        }
    }
}