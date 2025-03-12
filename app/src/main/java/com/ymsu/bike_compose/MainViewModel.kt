package com.ymsu.bike_compose

import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ymsu.bike_compose.data.AvailableInfoItem
import com.ymsu.bike_compose.data.BikeApiService
import com.ymsu.bike_compose.data.BikeRepository
import com.ymsu.bike_compose.data.CompleteStationInfo
import com.ymsu.bike_compose.data.FlaskItemWithFavorite
import com.ymsu.bike_compose.data.StationInfoFromFlaskItem
import com.ymsu.bike_compose.data.StationInfoItem
import com.ymsu.bike_compose.room.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
    private val favoriteRepository: FavoriteRepository,
    val locationRepository: LocationRepository
) : ViewModel() {
    private val TAG = "[MainViewModel]"
    private val bikeService = BikeApiService.create()
    private val repository = BikeRepository(bikeService)

    // request actual location
    private val _currentLatLng = MutableStateFlow(LatLng(25.048874128990544, 121.513878757331))
    val currentLatLng = _currentLatLng.asStateFlow()
    // map location
    private var _mapLocation = LatLng(25.048874128990544, 121.513878757331)

    private val _favoriteStations = MutableStateFlow<Set<String>>(emptySet())

    private var fetchNearByJob: Job? = null
    private var allStationsJob: Job? = null

    private var _allStationFromFlask = MutableStateFlow<List<StationInfoFromFlaskItem>>(emptyList())
    private var _allStationWithFavorite = MutableStateFlow<List<FlaskItemWithFavorite>>(emptyList())

    private var _nearByStationFromFlask = MutableStateFlow<List<StationInfoFromFlaskItem>>(emptyList())
    private var _nearByStationWithFavorite = MutableStateFlow<List<FlaskItemWithFavorite>>(emptyList())
    val nearByStationWithFavorite = _nearByStationWithFavorite.asStateFlow()

    private var _allFavoriteStations = MutableStateFlow<List<FlaskItemWithFavorite>>(emptyList())
    val allFavoriteStations = _allFavoriteStations.asStateFlow()

    private val _queryStations = MutableStateFlow("")
    val filterStations = _queryStations
        .flatMapLatest { query ->
            _allStationWithFavorite.map { stations ->
                if (query.isEmpty()) {
                    emptyList()
                } else {
                    _allStationWithFavorite.value.filter {
                        it.stationInfoFromFlaskItem.station_name.contains(query, ignoreCase = true)
                    }.sortedBy { it.distance }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedStation = MutableStateFlow<FlaskItemWithFavorite?>(null)
    val selectedStation = _selectedStation.asStateFlow()

    private val _range = MutableStateFlow(1000)
    val range = _range.asStateFlow()

    init {
        // get actual location first
        locationRepository.startRequestLocation()

        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                location?.let {
                    Log.d(TAG, "[Launch] _currentLatLng => lat: ${location.latitude}, lon: ${location.longitude}")
                    _currentLatLng.value = LatLng(location.latitude, location.longitude)
                }
            }
        }

        // get favorite list
        viewModelScope.launch {
            favoriteRepository.getAll()
                .map { stations -> stations.map { it.stationUid }.toSet() }
                .collect { _favoriteStations.value = it }
        }

        combine(_nearByStationFromFlask,_favoriteStations) { nearByStationFromFlask,favoriteList->
            nearByStationFromFlask.map { station->
                val isFavorite = station.station_uid in favoriteList
                val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                    latitude = _currentLatLng.value.latitude
                    longitude = _currentLatLng.value.longitude
                }
                val distance = getStationDistance(LatLng(station.lat, station.lng), currentLocation)
                FlaskItemWithFavorite(station,isFavorite,distance)
            }.sortedBy { it.distance }
        }.onEach {
            _nearByStationWithFavorite.value = it
        }.flowOn(
            Dispatchers.IO
        ).launchIn(
            viewModelScope
        )

        fetchAllStations()
        startPeriodFetchData()

        _allStationFromFlask.combine(_favoriteStations) { allStationFromFlask,favoriteList ->
            allStationFromFlask.map { station ->
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
                FlaskItemWithFavorite(station,isFavorite,distance)
            }
        }.onEach {
            _allStationWithFavorite.value = it
        }.flowOn(
            Dispatchers.IO
        ).launchIn(
            viewModelScope
        )

        _allStationWithFavorite.onEach {
            val favoriteStations = it.filter { item -> item.isFavorite }
            _allFavoriteStations.value = favoriteStations.sortedBy { it.distance }
        }.flowOn(
            Dispatchers.IO
        ).launchIn(
            viewModelScope
        )
    }

    private fun startPeriodFetchData(){
        viewModelScope.launch {
            while (true) {
                delay(60*1000)
                fetchNearByStationInfo()
                fetchAllStations()
            }
        }
    }

    fun setSelectedStation(stationInfo: FlaskItemWithFavorite?) {
        _selectedStation.value = stationInfo
        if (stationInfo != null) {
            Log.d(
                TAG, "[setSelectedStation] called fetchNearByStationInfo, station " +
                        "lat = ${stationInfo.stationInfoFromFlaskItem.lat} , lon = ${stationInfo.stationInfoFromFlaskItem.lng}"
            )
        }
    }

    fun setupRange(range: Int) {
        _range.value = range
    }

    fun setUpMapLocation(latitude: Double,longitude: Double){
        Log.d(TAG,"[setUpMapLocation] latitude = $latitude, longitude = $longitude")
        _mapLocation = LatLng(latitude,longitude)
        fetchNearByStationInfo()
    }

    private fun fetchNearByStationInfo() {
        Log.d(TAG,"[fetchNearByStationInfo] Distance = ${_range.value}")

        fetchNearByJob?.cancel()
        fetchNearByJob = viewModelScope.launch {
            val nearByStations = repository.getNearByStationFromFlask(_mapLocation.latitude.toFloat()
                ,_mapLocation.longitude.toFloat(),(range.value.toFloat()/1000))
            _nearByStationFromFlask.value = nearByStations
            Log.d(TAG, "[fetchNearByStationInfo] _nearByStationFromFlask size  : ${_nearByStationFromFlask.value.size}")
        }
    }

    private fun fetchAllStations() {
        allStationsJob?.cancel()
        allStationsJob = viewModelScope.launch(Dispatchers.IO) {
            val allStations = mutableListOf<StationInfoFromFlaskItem>()
            try {
                delay(400)
                val response = repository.getAllStationFromFlask()
                allStations.addAll(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _allStationFromFlask.value = allStations
            Log.d(TAG, "[fetchAllStations] _allStationFromFlask size  : ${_allStationFromFlask.value.size}")
        }
    }

//    private fun fetchAllStations() {
//        allStationsJob?.cancel()
//        allStationsJob = viewModelScope.launch(Dispatchers.IO) {
//            val allStations = mutableListOf<StationInfoItem>()
//            for (city in cities) {
//                try {
//                    delay(400)
//                    val response = repository.getStationInfo(city)
//                    allStations.addAll(response)
//                    val response = repository.getAvailableInfo(city)
//                    allStations.addAll(response)
//
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//            _allStations.value = allStations
//            Log.d(TAG, "[fetchAllStations] _allStations size  : ${_allStations.value.size}")
//        }
//    }

//    private fun fetchAllAvailability() {
//        allAvailableJob?.cancel()
//        allAvailableJob = viewModelScope.launch(Dispatchers.IO) {
//            val allAvailabilities = mutableListOf<AvailableInfoItem>()
//            for (city in cities) {
//                try {
//                    delay(400)
//                    val response = repository.getAvailableInfo(city)
//                    allAvailabilities.addAll(response)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//            _allAvailable.value = allAvailabilities
//            Log.d(TAG, "[fetchAllAvailability] _allAvailable size  : ${_allAvailable.value.size}")
//        }
//    }


    fun clickFavorite(stationUid: String) {
        Log.d(TAG, "[clickFavorite] stationUid = ${stationUid}")
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(stationUid)
        }
    }

    private fun getStationDistance(stationLatLng: LatLng, current: Location): Float {
        var stationLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = stationLatLng.latitude
            longitude = stationLatLng.longitude
        }
        return stationLocation.distanceTo(current)
    }

    fun queryStations(queryStation: String) {
        _queryStations.value = queryStation
    }
}