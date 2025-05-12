package com.ymsu.bike_compose

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.data.StationInfoDetail
import com.ymsu.bike_compose.room.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel@Inject constructor(
    private val roomRepository: RoomRepository,
    private val locationRepository: LocationRepository,
    private val bikeRepository: BikeRepository
) : ViewModel() {

    init {
        locationRepository.startRequestLocation()

        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                Log.d("","COLLECT location => $location")
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _userLatLng.value = latLng
                    _mapLatLng.value = latLng
                }
            }
        }
    }

    private val initLatLng = LatLng(25.048874128990544, 121.513878757331) // Taipei station

    // record google map position
    private var _mapLatLng = MutableStateFlow(initLatLng)
    val mapLatLng = _mapLatLng.asStateFlow()

    // record user position
    private var _userLatLng = MutableStateFlow(initLatLng)
    val userLatLng = _userLatLng.asStateFlow()

    private val _state = MutableStateFlow(BikeState())
    val state = _state
        .onStart {
            fetchNearBy()
            fetchAllStation()
            observeFavorite() // update nearby and all station
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _state.value
        )

    private fun fetchNearBy(){
        viewModelScope.launch {
            val result = bikeRepository.getNearByStationFromFlask(
                _mapLatLng.value.latitude.toFloat(),
                _mapLatLng.value.longitude.toFloat(),
                (1000/1000).toFloat()
            )
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(
                            nearFavoriteStations = result.data.map { detail -> StationInfo(stationInfoDetail = detail) }
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(
                            errorMessage = result.message
                        )
                    }
                }
                is ApiResult.Loading -> {
                    _state.update { it.copy(
                        isLoading = true
                        )
                    }
                }
            }
        }
    }

    private fun fetchAllStation(){
        viewModelScope.launch {
            val result = bikeRepository.getAllStationFromFlask()
            when (result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            errorMessage = result.message
                        )
                    }
                }
                is ApiResult.Loading -> {
                    _state.update {
                        it.copy(
                           isLoading = true
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            allFavoriteStations = result.data.map { detail ->
                                StationInfo(stationInfoDetail = detail)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun observeFavorite(){
        viewModelScope.launch {
            roomRepository.getFavoriteList().collect { favorite ->
                val list = favorite.map { it.stationUid }

                _state.update { currentState ->
                    val updatedAllStation = currentState.allFavoriteStations.map { station ->
                        val isFavorite = station.stationInfoDetail.station_uid in list
                        station.copy(isFavorite = isFavorite)
                    }

                    val updatedNearBy = currentState.nearFavoriteStations.map { station ->
                        val isFavorite = station.stationInfoDetail.station_uid in list
                        station.copy(isFavorite = isFavorite)
                    }

                    currentState.copy(
                        allFavoriteStations = updatedAllStation,
                        nearFavoriteStations = updatedNearBy
                    )
                }
            }
        }
    }

    fun updateCurrentLatLng(latitude: Double, longitude: Double) {
        _mapLatLng.value = LatLng(latitude, longitude)
        fetchNearBy()
    }
}