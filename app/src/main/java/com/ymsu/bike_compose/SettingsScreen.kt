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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ymsu.bike_compose.theme.AppTheme

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val onRangeChanged: (Int) -> Unit = { range ->
        viewModel.setupRange(range)
    }

    val range = viewModel.range.collectAsStateWithLifecycle()

    Content(onRangeChanged,range)
}

@Composable
private fun Content(onRangeChanged: (Int) -> Unit, range: State<Int>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp)
    ) {

        SetupCity()

        Spacer(modifier = Modifier.height(24.dp))

        SetupRange(onRangeChanged,range)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "關於應用程式",
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            modifier = Modifier.padding(16.dp),
            text = "版本 v1.0"
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

    val rangeList = listOf(1000,500,250)

    var selectedRange by remember {
        mutableIntStateOf(range.value)
    }

    ExposedDropdownMenuBox(expanded = expanded,
        onExpandedChange = {expanded = !expanded}) {

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = selectedRange.toString(),
            onValueChange = {},
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor =  Color.Transparent,
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
            rangeList.forEach { range ->
                DropdownMenuItem(
                    text = { Text(text = range.toString()) },
                    onClick = {
                        selectedRange = range
                        onRangeChanged(range)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupCity() {
    var expanded by remember {
        mutableStateOf(false)
    }
    val cityList = listOf(
        "Taichung", "Hsinchu", "MiaoliCounty", "ChanghuaCounty", "NewTaipei",
        "YunlinCounty", "ChiayiCounty", "PingtungCounty", "TaitungCounty", "Taoyuan", "Taipei",
        "Kaohsiung", "Tainan", "Chiayi", "HsinchuCounty"
    )

    var selectedCity by remember {
        mutableStateOf("NewTaipei")
    }

    Text(
        text = "切換地區",
        color = MaterialTheme.colorScheme.primary
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedCity,
            onValueChange = {},
            readOnly = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            containerColor = Color.White,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cityList.forEach { city ->
                DropdownMenuItem(
                    text = { Text(text = city) },
                    onClick = {
                        selectedCity = city
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