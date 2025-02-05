package com.ymsu.bike_compose

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ymsu.bike_compose.data.BikeApiService
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.StationInfoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val bikeService = BikeApiService.create()
    private val repository = BikeRepository(bikeService)

    private val _stationInfoList = MutableStateFlow<List<StationInfoItem>>(emptyList())
    val stationList: StateFlow<List<StationInfoItem>> = _stationInfoList

    init {
        fetchStationInfo()
//        fetchAvailableInfo()
    }

    private fun fetchAvailableInfo() {
        TODO("Not yet implemented")
    }

    private fun fetchStationInfo() {
        viewModelScope.launch {
            try {
                val data = repository.getStationInfo()
                _stationInfoList.value = data
            } catch (e: Exception) {
                Log.e("MainViewModel", "API 錯誤: ${e.message}")
            }
        }
    }
}