package com.ymsu.bike_compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.LatLng
import com.ymsu.bike_compose.theme.AppTheme

@Composable
fun SettingsScreen(viewModel: MainViewModel) {

    val onRangeChanged: (Int) -> Unit = { range ->
        viewModel.setupRange(range)
    }

    val range = viewModel.range.collectAsStateWithLifecycle()

    Content(onRangeChanged, range)
}

@Composable
private fun Content(onRangeChanged: (Int) -> Unit, range: State<Int>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp)
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        SetupRange(onRangeChanged, range)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "關於應用程式",
            color = MaterialTheme.colorScheme.primary
        )

        val context = LocalContext.current
        val version = context.packageManager.getPackageInfo(context.packageName,0).versionName

        Text(
            modifier = Modifier.padding(16.dp),
            text = "版本$version"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupRange(onRangeChanged: (Int) -> Unit, range: State<Int>) {
    Text(
        text = "站點顯示範圍",
        color = MaterialTheme.colorScheme.primary
    )

    var expanded by remember {
        mutableStateOf(false)
    }

    val rangeMap = mapOf(
        "1公里" to 1000,
        "500公尺" to 500,
        "250公尺" to 250
    )

    var selectedRange by remember {
        mutableIntStateOf(range.value)
    }

    ExposedDropdownMenuBox(expanded = expanded,
        onExpandedChange = { expanded = !expanded }) {

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = rangeMap.entries.find { it.value == selectedRange }?.key ?: "",
            onValueChange = {},
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            readOnly = true
        )

        ExposedDropdownMenu(
            containerColor = Color.White,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rangeMap.forEach { range ->
                DropdownMenuItem(
                    text = { Text(text = range.key) },
                    onClick = {
                        selectedRange = range.value
                        onRangeChanged(range.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettings() {
    val fakeRange = remember {
        mutableStateOf(1000)
    }
    AppTheme {
        Content({}, fakeRange)
    }
}