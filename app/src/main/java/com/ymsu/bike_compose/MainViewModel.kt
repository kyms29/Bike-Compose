package com.ymsu.bike_compose

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ymsu.bike_compose.applicationLayer.MyApplication
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.BikeApiService
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.FullyStationInfo
import com.ymsu.bike_compose.data.StationInfoItem
import com.ymsu.bike_compose.room.FavoriteRepository
import com.ymsu.bike_compose.room.FavoriteStationDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val TAG = "[MainViewModel]"

    private val bikeService = BikeApiService.create()
    private val repository = BikeRepository(bikeService)
    private val favoriteRepository = (application as MyApplication).favoriteRepository

    private var _favoriteStations = MutableStateFlow<Set<String>> (emptySet())
    val favoriteStations: StateFlow<Set<String>> = _favoriteStations

    private val _stationInfoList = MutableStateFlow<List<StationInfoItem>>(emptyList())
    val stationList: StateFlow<List<StationInfoItem>> = _stationInfoList

    private val _nearByStationInfo = MutableStateFlow<List<StationInfoItem>> (emptyList())
    val nearByStationInfo: StateFlow<List<StationInfoItem>> = _nearByStationInfo

    private val _nearByAvailableInfo = MutableStateFlow<List<AvailableInfoItem>>(emptyList())
    val nearByAvailableInfo: StateFlow<List<AvailableInfoItem>> = _nearByAvailableInfo

    val nearByCombinedInfo: StateFlow<List<FullyStationInfo>> =
        combine(_nearByStationInfo, _nearByAvailableInfo, _favoriteStations) { stationList, availableList, favoriteList ->
            stationList.map { station ->

                val available = availableList.find { it.StationUID == station.StationUID } ?: AvailableInfoItem()
                val isFavorite = station.StationUID in favoriteList
                if (isFavorite) {
                    Log.d(TAG,"[nearByCombinedInfo] isFavorite = ${station.StationUID}")
                }
                FullyStationInfo(station, available, isFavorite)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // fetch favoriteStations from room database...
        viewModelScope.launch {
            _favoriteStations.value = favoriteRepository.getAll()
        }
    }

    fun fetchNearByStationInfo(latitude: Double,longitude: Double, distance: Int = 1000){
        Log.d(TAG,"[fetchNearByStationInfo]")
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

    fun clickFavorite(stationUid: String){
        Log.d(TAG,"[clickFavorite] stationUid = ${stationUid}")
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(stationUid)
            _favoriteStations.value = favoriteRepository.getAll()
        }
    }
}