package com.ymsu.bike_compose

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.BikeApiService
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.FullyStationInfo
import com.ymsu.bike_compose.data.StationInfoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val bikeService = BikeApiService.create()
    private val repository = BikeRepository(bikeService)

    private val _stationInfoList = MutableStateFlow<List<StationInfoItem>>(emptyList())
    val stationList: StateFlow<List<StationInfoItem>> = _stationInfoList

    private val _nearByStationInfo = MutableStateFlow<List<StationInfoItem>> (emptyList())
    val nearByStationInfo: StateFlow<List<StationInfoItem>> = _nearByStationInfo

    private val _nearByAvailableInfo = MutableStateFlow<List<AvailableInfoItem>>(emptyList())
    val nearByAvailableInfo: StateFlow<List<AvailableInfoItem>> = _nearByAvailableInfo

    val nearByCombinedInfo: StateFlow<List<FullyStationInfo>> =
        combine(_nearByStationInfo, _nearByAvailableInfo) { stationList, availableList ->
            stationList.map { station ->
                val available = availableList.find { it.StationUID == station.StationUID } ?: AvailableInfoItem()
                FullyStationInfo(station, available)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
//        fetchStationInfo()
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

    fun fetchNearByStationInfo(latitude: Double,longitude: Double, distance: Int = 1000){
        Log.d("[ViewModel]","[fetchNearByStationInfo]")
        viewModelScope.launch {
            val data = repository.getNearByStationInfo("nearby($latitude, $longitude, $distance)")
            _nearByStationInfo.value = data
        }
    }

    fun fetchNearByAvailableInfo(latitude: Double,longitude: Double, distance: Int = 1000){
        viewModelScope.launch {
            val data = repository.getNearByAvailableInfo("nearby($latitude, $longitude, $distance)")
            _nearByAvailableInfo.value = data
        }
    }
}