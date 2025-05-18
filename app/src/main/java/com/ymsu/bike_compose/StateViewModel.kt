package com.ymsu.bike_compose

import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.room.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StateViewModel@Inject constructor(
    private val roomRepository: RoomRepository,
    private val locationRepository: LocationRepository,
    private val bikeRepository: BikeRepository
) : ViewModel() {
    val TAG = "[TestViewModel]"

    var fetchNearbyStationJob: Job? = null
    var fetchAllStationJob: Job? = null
    var oberveFavoriteJob: Job? = null
    var observeRange:Job? = null
    var updateRangeJob: Job? = null

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
            observeFavorite() // update nearby and all station
            fetchNearBy()
            fetchAllStation()
            observeRange()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _state.value
        )

    init {
        locationRepository.startRequestLocation()

        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                Log.d(TAG,"COLLECT location => $location")
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _userLatLng.value = latLng
                    _mapLatLng.value = latLng
                }
            }
        }

        startPeriodUpdate()

    }
    private fun startPeriodUpdate(){
        viewModelScope.launch {
            while (true) {
                Log.d(TAG,"[startPeriodUpdate]")
                fetchNearBy()
                fetchAllStation()
                delay(60*1000)
            }
        }
    }



    private fun fetchNearBy(){
        Log.d(TAG,"[fetchNearBy] _state.value.range = "+_state.value.range)
        fetchNearbyStationJob?.cancel()
        fetchNearbyStationJob = viewModelScope.launch {
            val result = bikeRepository.getNearByStationFromFlask(
                _mapLatLng.value.latitude.toFloat(),
                _mapLatLng.value.longitude.toFloat(),
                (_state.value.range.toFloat()/1000)
            )

            val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                latitude = _userLatLng.value.latitude
                longitude = _userLatLng.value.longitude
            }
            when (result) {
                is ApiResult.Success -> {
                    _state.update { currentState ->
                        Log.d(TAG,"[fetchNearBy] recordFavoriteList = ${currentState.recordFavoriteList.toString()}")

                        val updatedNearBy = result.data.map { detail ->
                            val favorite = detail.station_uid in currentState.recordFavoriteList
                            val getDistance = getStationDistance(LatLng(detail.lat,detail.lng),currentLocation)
                            StationInfo(stationInfoDetail = detail, isFavorite = favorite,distance = getDistance)
                        }

                        currentState.copy(
                            isLoading = false,
                            errorMessage = "",
                            nearFavoriteStations = updatedNearBy.sortedBy { it.distance }
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is ApiResult.Loading -> {
                    _state.update { it.copy(
                            isLoading = true,
                            errorMessage = "",
                        )
                    }
                }
            }
        }
    }

    private fun fetchAllStation(){
        fetchAllStationJob?.cancel()
        fetchAllStationJob = viewModelScope.launch {
            val result = bikeRepository.getAllStationFromFlask()

            val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                latitude = _userLatLng.value.latitude
                longitude = _userLatLng.value.longitude
            }
            when (result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is ApiResult.Loading -> {
                    _state.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = "",
                        )
                    }
                }
                is ApiResult.Success -> {
                    Log.d(TAG,"[fetchAllStation] size = "+result.data.size)
                    _state.update { currentState ->

                        val updatedAllStation = result.data.map { detail ->
                            val favorite = detail.station_uid in currentState.recordFavoriteList
                            val getDistance = getStationDistance(LatLng(detail.lat,detail.lng),currentLocation)
                            StationInfo(stationInfoDetail = detail,isFavorite = favorite, distance = getDistance)
                        }
                        val favorites = updatedAllStation.filter { it.isFavorite }
                        Log.d(TAG, "Old size: ${currentState.nearFavoriteStations.size}, New size: ${favorites.size}")


                        currentState.copy(
                            errorMessage = "",
                            isLoading = false,
                            allFavoriteStations = updatedAllStation.sortedBy { it.distance },
                            favoriteStations = favorites
                        )

                    }
                }
            }
        }
    }

    private fun observeFavorite(){
        oberveFavoriteJob?.cancel()
        oberveFavoriteJob = viewModelScope.launch {
            roomRepository.getFavoriteList().collect { favorite ->
                val list = favorite.map { it.stationUid }
                Log.d(TAG, "[observeFavorite] list = $list")

                _state.update { currentState ->
                    val updatedAllStation = currentState.allFavoriteStations.map { station ->
                        val isFavorite = station.stationInfoDetail.station_uid in list
                        station.copy(isFavorite = isFavorite)
                    }

                    Log.d(TAG,"[observeFavorite] updatedAllStation size = "+updatedAllStation.size)

                    val updatedNearBy = currentState.nearFavoriteStations.map { station ->
                        val isFavorite = station.stationInfoDetail.station_uid in list
                        Log.d(TAG,"[observeFavorite] nearBy uid = ${station.stationInfoDetail.station_uid}, isFavorite = $isFavorite")
                        station.copy(isFavorite = isFavorite)
                    }

                    Log.d(TAG,"[observeFavorite] updatedNearBy size = "+updatedNearBy.size)

                    val filterFavorite = updatedAllStation.filter { stationInfo -> stationInfo.isFavorite }
                    Log.d(TAG,"[observeFavorite] filterFavorite size = "+filterFavorite.size)

                    currentState.copy(
                        allFavoriteStations = updatedAllStation.sortedBy { it.distance },
                        nearFavoriteStations = updatedNearBy.sortedBy { it.distance },
                        favoriteStations = filterFavorite,
                        recordFavoriteList = list
                    )
                }
            }
        }
    }

    private fun observeRange(){
        observeRange?.cancel()
        observeRange = viewModelScope.launch {
            val result = roomRepository.getSettingValue("user_range").toInt()
            _state.update {
                it.copy(
                    range = result
                )
            }
        }
    }

    fun updateCurrentLatLng(latitude: Double, longitude: Double) {
        _mapLatLng.value = LatLng(latitude, longitude)
        fetchNearBy()
    }

    fun updateRange(newRange:Int) {
        Log.d(TAG,"[updateRange] newRange = $newRange")
        updateRangeJob?.cancel()
        updateRangeJob = viewModelScope.launch {
            roomRepository.saveSetting("user_range", newRange.toString())
        }
        observeRange()
    }

    fun clickFavorite(stationUid: String) {
        Log.d(TAG, "[clickFavorite] stationUid = ${stationUid}")
        viewModelScope.launch {
            roomRepository.toggleFavorite(stationUid)
        }
        observeFavorite()
    }

    fun setQueryString(query: String){
        Log.d(TAG,"[setQueryString]")
        _state.update {
            val queryStations = it.allFavoriteStations.filter { stationInfo ->
                stationInfo.stationInfoDetail.station_name.contains(query)
            }
            it.copy(
                search = query,
                searchResults = queryStations
            )
        }
    }

    fun setSelectedStation(stationInfo: StationInfo?) {
        Log.d(TAG,"[setSelectedStation]")
        _state.update {
            it.copy(
                selectedStation = stationInfo
            )
        }

        if (stationInfo != null) {
            Log.d(TAG, "[setSelectedStation] called fetchNearByStationInfo, station " + "lat = ${stationInfo.stationInfoDetail.lat} , lon = ${stationInfo.stationInfoDetail.lng}")
            _mapLatLng.value = LatLng(stationInfo.stationInfoDetail.lat,stationInfo.stationInfoDetail.lng)
        }
    }

    private fun getStationDistance(stationLatLng: LatLng, current: Location): Float {
        val stationLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = stationLatLng.latitude
            longitude = stationLatLng.longitude
        }
        return stationLocation.distanceTo(current)
    }
}