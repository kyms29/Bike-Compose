package com.ymsu.bike_compose

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.BikeApiService
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.CompleteStationInfo
import com.ymsu.bike_compose.data.StationInfoItem
import com.ymsu.bike_compose.room.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val favoriteRepository: FavoriteRepository,
                                        val locationRepository: LocationRepository): ViewModel() {
    private val TAG = "[MainViewModel]"
    private val bikeService = BikeApiService.create()
    private val repository = BikeRepository(bikeService)

    // request location
    private val _currentLatLng = MutableStateFlow(LatLng(25.048874128990544, 121.513878757331))
    val currentLatLng = _currentLatLng.asStateFlow()

    private val _favoriteStations =  MutableStateFlow<Set<String>>(emptySet())

    private val _nearByStations = MutableStateFlow<List<StationInfoItem>> (emptyList())

    private val _nearByAvailable = MutableStateFlow<List<AvailableInfoItem>>(emptyList())

    private val _completeStationInfo = MutableStateFlow<List<CompleteStationInfo>>(emptyList())
    val completeStationInfo = _completeStationInfo.asStateFlow()

    private var fetchJob: Job? = null

    init {
        locationRepository.startRequestLocation()

        viewModelScope.launch {
            locationRepository.currentLocation.collect { location->
                location?.let {
                    _currentLatLng.value = LatLng(location.latitude,location.longitude)
                }
            }
        }
        viewModelScope.launch {
            favoriteRepository.getAll()
                .map { stations -> stations.map { it.stationUid }.toSet() }
                .collect { _favoriteStations.value = it }
        }

        combine(
            _nearByStations,
            _nearByAvailable,
            _favoriteStations
        ) { nearByStations, nearByAvailibles ,favorites->
            Log.d(TAG,"combine flow trigger")
            nearByStations.map { stationInfo ->
                val isFavorite = stationInfo.StationUID in favorites
                val available = nearByAvailibles.find { it.StationUID == stationInfo.StationUID } ?: AvailableInfoItem()
                CompleteStationInfo(stationInfo, available, isFavorite)
            }
        }
            .onEach { _completeStationInfo.value = it }
            .launchIn(viewModelScope)

    }

    fun fetchNearByStationInfo(latitude: Double, longitude: Double, distance: Int = 1000) {
        val nearbyString = "nearby($latitude, $longitude, $distance)"

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            Log.d(TAG,"[fetchNearByStationInfo] called , nearbyString : ${nearbyString}")
            val data = repository.getNearByStationInfo(nearbyString)
            _nearByStations.value = data
            Log.d(TAG,"[fetchNearByStationInfo] _nearByStations size  : ${_nearByStations.value.size}")
            val data2 = repository.getNearByAvailableInfo(nearbyString)
            _nearByAvailable.value = data2
            Log.d(TAG,"[fetchNearByStationInfo] _nearByAvailable size  : ${_nearByAvailable.value.size}")
        }
    }


    fun clickFavorite(stationUid: String){
        Log.d(TAG,"[clickFavorite] stationUid = ${stationUid}")
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(stationUid)
        }
    }
}