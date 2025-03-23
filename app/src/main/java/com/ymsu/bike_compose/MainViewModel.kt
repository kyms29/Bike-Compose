package com.ymsu.bike_compose

import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.ymsu.bike_compose.data.ApiResult
import com.ymsu.bike_compose.data.BikeApiService
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.StationInfo
import com.ymsu.bike_compose.data.StationInfoDetail
import com.ymsu.bike_compose.room.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val TAG = "[MainViewModel]"
    private val bikeService = BikeApiService.create()
    private val repository = BikeRepository(bikeService)

    // handle google map position
    private var _currentLatLng = MutableStateFlow(LatLng(25.048874128990544, 121.513878757331))
    val currentLatLng = _currentLatLng.asStateFlow()

    // handle real position
    private var _realUserLatLng = MutableStateFlow(LatLng(25.048874128990544, 121.513878757331))
    val realUserLatLng = _realUserLatLng.asStateFlow()

    // MapScreen map camera position
    private val _mapCameraPosition = MutableStateFlow<CameraPosition?>(null)
    val mapCameraPosition:StateFlow<CameraPosition?> = _mapCameraPosition.asStateFlow()

    // map location
    private var _mapLocation = LatLng(25.048874128990544, 121.513878757331)

    private val _favoriteStations = MutableStateFlow<Set<String>>(emptySet())

    private var fetchNearByJob: Job? = null
    private var allStationsJob: Job? = null

    private var _allStationFromFlask = MutableStateFlow<ApiResult<List<StationInfoDetail>>>(ApiResult.Loading)
    private var _allStationWithFavorite = MutableStateFlow<ApiResult<List<StationInfo>>>(ApiResult.Loading)

    private var _nearByStationFromFlask = MutableStateFlow<ApiResult<List<StationInfoDetail>>>(ApiResult.Loading)
    private var _nearByStationWithFavorite = MutableStateFlow<ApiResult<List<StationInfo>>>(ApiResult.Loading)
    val nearByStationWithFavorite = _nearByStationWithFavorite.asStateFlow()

    private var _allFavoriteStations = MutableStateFlow<ApiResult<List<StationInfo>>>(ApiResult.Loading)
    val allFavoriteStations = _allFavoriteStations.asStateFlow()

    private val _queryStations = MutableStateFlow("")
    val filterStations = _queryStations
        .flatMapLatest { query ->
            _allStationWithFavorite
                .map { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            if (query.isEmpty()) {
                                ApiResult.Success(emptyList())
                            } else {
                                ApiResult.Success(result.data.filter {
                                    it.stationInfoDetail.station_name.contains(query, ignoreCase = true)
                                }.sortedBy { it.distance })
                            }
                        }
                        is ApiResult.Error -> {
                            ApiResult.Error(result.message)
                        }
                        is ApiResult.Loading -> {
                            ApiResult.Loading
                        }
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, ApiResult.Loading)

    private val _selectedStation = MutableStateFlow<StationInfo?>(null)
    val selectedStation = _selectedStation.asStateFlow()

    private val _range = MutableStateFlow(1000)
    val range = _range.asStateFlow()


    init {
        // get actual location first
        locationRepository.startRequestLocation()

        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                location?.let {
                    Log.d(
                        TAG,
                        "[Launch] _currentLatLng => lat: ${location.latitude}, lon: ${location.longitude}"
                    )
                    _currentLatLng.value = LatLng(location.latitude, location.longitude)
                    _realUserLatLng.value = LatLng(location.latitude, location.longitude)
                }
            }
        }

        // get favorite list
        viewModelScope.launch {
            roomRepository.getFavoriteList()
                .map { stations -> stations.map { it.stationUid }.toSet() ?: emptySet() }
                .collect { _favoriteStations.value = it }
        }

        viewModelScope.launch {
            _range.value = roomRepository.getSettingValue("user_range").toInt()
        }

        combine(
            _nearByStationFromFlask,
            _favoriteStations
        ) { nearByStationFromFlask, favoriteList ->
            when(nearByStationFromFlask) {
                is ApiResult.Success -> {
                    ApiResult.Success(
                        nearByStationFromFlask.data.map { station ->
                            val isFavorite = station.station_uid in favoriteList
                            val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                                latitude = _currentLatLng.value.latitude
                                longitude = _currentLatLng.value.longitude
                            }
                            val distance = getStationDistance(LatLng(station.lat, station.lng), currentLocation)
                            StationInfo(station, isFavorite, distance)
                        }.sortedBy { it.distance }
                    )
                }

                is ApiResult.Error -> {
                    ApiResult.Error(nearByStationFromFlask.message)
                }

                is ApiResult.Loading -> {
                    ApiResult.Loading
                }
            }
        }.onEach {
            _nearByStationWithFavorite.value = it
        }.flowOn(
            Dispatchers.IO
        ).launchIn(
            viewModelScope
        )

        fetchAllStations()
        startPeriodFetchData()

        _allStationFromFlask.combine(_favoriteStations) { allStationFromFlask, favoriteList ->
            when(allStationFromFlask) {
                is ApiResult.Success -> {
                    ApiResult.Success(
                        allStationFromFlask.data.map { station ->
                        val isFavorite = station.station_uid in favoriteList
                        val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                            latitude = _currentLatLng.value.latitude
                            longitude = _currentLatLng.value.longitude
                        }
                        val distance = getStationDistance(
                            LatLng(
                                station.lat,
                                station.lng
                            ), currentLocation
                        )
                        StationInfo(station, isFavorite, distance)
                        }
                    )
                }
                is ApiResult.Error -> {
                    ApiResult.Error(allStationFromFlask.message)
                }
                is ApiResult.Loading -> {
                    ApiResult.Loading
                }
            }
        }.onEach {
            _allStationWithFavorite.value = it
        }.flowOn(
            Dispatchers.IO
        ).launchIn(
            viewModelScope
        )

        _allStationWithFavorite.onEach {
            when(it) {
                is ApiResult.Success -> {
                    Log.d(TAG,"_allStationWithFavorite SUCCESS")
                    val favoriteStations = it.data.filter { item -> item.isFavorite }
                    if (favoriteStations.isEmpty()) {
                        _allFavoriteStations.value = ApiResult.Success(emptyList())
                    } else {
                        _allFavoriteStations.value = ApiResult.Success(favoriteStations.sortedBy { it.distance })
                    }
                }

                is ApiResult.Error -> {
                    Log.d(TAG,"_allStationWithFavorite ERROR")
                    _allFavoriteStations.value = ApiResult.Error(it.message)
                }
                is ApiResult.Loading -> {
                    Log.d(TAG,"_allStationWithFavorite LOADING")
                    _allFavoriteStations.value = ApiResult.Loading
                }
            }
        }.flowOn(
            Dispatchers.IO
        ).launchIn(
            viewModelScope
        )
    }

    private fun startPeriodFetchData() {
        viewModelScope.launch {
            while (true) {
                delay(60 * 1000)
                fetchNearByStationInfo()
                fetchAllStations()
            }
        }
    }

    fun setSelectedStation(stationInfo: StationInfo?) {
        Log.d(TAG,"[setSelectedStation]")
        _selectedStation.value = stationInfo
        if (stationInfo != null) {
            Log.d(
                TAG, "[setSelectedStation] called fetchNearByStationInfo, station " +
                        "lat = ${stationInfo.stationInfoDetail.lat} , lon = ${stationInfo.stationInfoDetail.lng}"
            )
            _currentLatLng.value = LatLng(stationInfo.stationInfoDetail.lat,stationInfo.stationInfoDetail.lng)
        }
    }

    fun updateCameraPosition(cameraPosition: CameraPosition){
        _mapCameraPosition.value = cameraPosition
    }

    fun setupRange(range: Int) {
        _range.value = range
        viewModelScope.launch {
            roomRepository.saveSetting("user_range", range.toString())
        }
    }

    fun updateCurrentLatLng(latitude: Double, longitude: Double) {
        Log.d(TAG, "[setUpMapLocation] latitude = $latitude, longitude = $longitude")
        _mapLocation = LatLng(latitude, longitude)
        _currentLatLng.value = LatLng(latitude, longitude)
        fetchNearByStationInfo()
    }

    private fun fetchNearByStationInfo() {
        Log.d(TAG, "[fetchNearByStationInfo] Distance = ${_range.value}")

        fetchNearByJob?.cancel()
        fetchNearByJob = viewModelScope.launch {
            val nearByStations = repository.getNearByStationFromFlask(
                _mapLocation.latitude.toFloat(),
                _mapLocation.longitude.toFloat(),
                (range.value.toFloat() / 1000)
            )
            _nearByStationFromFlask.value = nearByStations
            Log.d(
                TAG,
                "[fetchNearByStationInfo] _nearByStationFromFlask size  : ${_nearByStationFromFlask.value}"
            )
        }
    }

    private fun fetchAllStations() {
        allStationsJob?.cancel()
        allStationsJob = viewModelScope.launch(Dispatchers.IO) {
            _allStationFromFlask.value = repository.getAllStationFromFlask()
            Log.d(
                TAG,
                "[fetchAllStations] _allStationFromFlask size  : ${_allStationFromFlask.value.toString()}"
            )
        }
    }

    fun clickFavorite(stationUid: String) {
        Log.d(TAG, "[clickFavorite] stationUid = ${stationUid}")
        viewModelScope.launch {
            roomRepository.toggleFavorite(stationUid)
        }
    }

    private fun getStationDistance(stationLatLng: LatLng, current: Location): Float {
        val stationLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = stationLatLng.latitude
            longitude = stationLatLng.longitude
        }
        return stationLocation.distanceTo(current)
    }

    fun queryStations(queryStation: String) {
        _queryStations.value = queryStation
    }
}