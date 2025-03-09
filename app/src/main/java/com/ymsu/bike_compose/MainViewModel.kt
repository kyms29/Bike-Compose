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

    // request location
    private val _currentLatLng = MutableStateFlow(LatLng(25.048874128990544, 121.513878757331))
    val currentLatLng = _currentLatLng.asStateFlow()

    private val _favoriteStations = MutableStateFlow<Set<String>>(emptySet())

    private val _nearByStations = MutableStateFlow<List<StationInfoItem>>(emptyList())

    private val _nearByAvailable = MutableStateFlow<List<AvailableInfoItem>>(emptyList())

    private val _completeStationInfo = MutableStateFlow<List<CompleteStationInfo>>(emptyList())
    val completeStationInfo = _completeStationInfo.asStateFlow()

    private var fetchNearByJob: Job? = null

    private val cities = listOf(
        "Taichung", "Hsinchu", "MiaoliCounty", "ChanghuaCounty", "NewTaipei", "YunlinCounty", "ChiayiCounty",
        "PingtungCounty", "TaitungCounty", "Taoyuan", "Taipei", "Kaohsiung", "Tainan", "Chiayi", "HsinchuCounty"
    )
    private val _allStations = MutableStateFlow<List<StationInfoItem>>(emptyList())
    private val _allAvailable = MutableStateFlow<List<AvailableInfoItem>>(emptyList())
    private val _allFavoriteStations = MutableStateFlow<List<CompleteStationInfo>>(emptyList())
    val allFavoriteStations = _allFavoriteStations.asStateFlow()
    private var allStationsJob: Job? = null
    private var allAvailableJob: Job? = null

    private val _allCityStationsInfo = MutableStateFlow<List<CompleteStationInfo>>(emptyList())

    private val _queryStations = MutableStateFlow("")
    val filterStations = _queryStations
        .flatMapLatest { query ->
            _allCityStationsInfo.map { stations ->
                if (query.isEmpty()) {
                    emptyList()
                } else {
                    _allCityStationsInfo.value.filter {
                        it.stationInfoItem.StationName.Zh_tw.contains(query, ignoreCase = true)
                    }.sortedBy { it.distance }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedStation = MutableStateFlow<CompleteStationInfo?>(null)
    val selectedStation = _selectedStation.asStateFlow()

    private val _range = MutableStateFlow(1000)
    val range = _range.asStateFlow()

    init {
        locationRepository.startRequestLocation()

        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                location?.let {
                    Log.d(TAG, "[Launch] _currentLatLng => lat: ${location.latitude}, lon: ${location.longitude}")
                    _currentLatLng.value = LatLng(location.latitude, location.longitude)
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
        ) { nearByStations, nearByAvailibles, favorites ->
            Log.d(TAG, "combine flow trigger")
            nearByStations.map { stationInfo ->
                val isFavorite = stationInfo.StationUID in favorites
                val available = nearByAvailibles.find { it.StationUID == stationInfo.StationUID } ?: AvailableInfoItem()
                val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                    latitude = _currentLatLng.value.latitude
                    longitude = _currentLatLng.value.longitude
                }
                val distance = getStationDistance(LatLng(stationInfo.StationPosition.PositionLat, stationInfo.StationPosition.PositionLon), currentLocation)
                Log.d(TAG, "combine flow station name => " + stationInfo.StationName.Zh_tw)
                CompleteStationInfo(stationInfo, available, isFavorite, distance)
            }.sortedBy { it.distance }
        }
            .onEach {
                Log.d(TAG, "Processed stations: ${it.size}")
                _completeStationInfo.value = it
            }.flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)

        fetchAllStations()
        fetchAllAvailability()

        // combine _allStations and _allAvailable first
        combine(_allStations, _allAvailable) { allStations, allAvailable ->
            allStations.map { stationInfo ->
                val availableInfo = allAvailable.find { it.StationUID == stationInfo.StationUID } ?: AvailableInfoItem()
                val currentLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
                    latitude = _currentLatLng.value.latitude
                    longitude = _currentLatLng.value.longitude
                }
                val distance = getStationDistance(
                    LatLng(
                        stationInfo.StationPosition.PositionLat,
                        stationInfo.StationPosition.PositionLon
                    ), currentLocation
                )
                CompleteStationInfo(stationInfo, availableInfo, false, distance)
            }
        }.onEach { _allCityStationsInfo.value = it }.flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)

        _allCityStationsInfo.combine(_favoriteStations) { _allStationAndAvailable, favoriteList ->
            _allStationAndAvailable.map { stationAndAvailable ->
                val isFavorite = stationAndAvailable.stationInfoItem.StationUID in favoriteList
                CompleteStationInfo(stationAndAvailable.stationInfoItem, stationAndAvailable.availableInfoItem, isFavorite, stationAndAvailable.distance)
            }.filter { it.isFavorite }.sortedBy { it.distance }
        }.onEach { _allFavoriteStations.value = it }.flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun setSelectedStation(stationInfo: CompleteStationInfo?) {
        _selectedStation.value = stationInfo
        if (stationInfo != null) {
            Log.d(
                TAG, "[setSelectedStation] called fetchNearByStationInfo, station " +
                        "lat = ${stationInfo.stationInfoItem.StationPosition.PositionLat} , lon = ${stationInfo.stationInfoItem.StationPosition.PositionLon}"
            )
        }
    }

    fun setupRange(range: Int) {
        _range.value = range
    }

    fun fetchNearByStationInfo(latitude: Double, longitude: Double, distance: MutableStateFlow<Int> = _range) {
        Log.d(TAG,"[fetchNearByStationInfo] Distance = ${distance.value}")
        val nearbyString = "nearby($latitude, $longitude, ${distance.value})"

        fetchNearByJob?.cancel()
        fetchNearByJob = viewModelScope.launch {
            Log.d(TAG, "[fetchNearByStationInfo] called , nearbyString : ${nearbyString}")
            val nearByStations = repository.getNearByStationInfo(nearbyString)
            _nearByStations.value = nearByStations
            Log.d(TAG, "[fetchNearByStationInfo] _nearByStations size  : ${_nearByStations.value.size}")
            val nearByAvailable = repository.getNearByAvailableInfo(nearbyString)
            _nearByAvailable.value = nearByAvailable
            Log.d(TAG, "[fetchNearByStationInfo] _nearByAvailable size  : ${_nearByAvailable.value.size}")
        }
    }

    private fun fetchAllStations() {
        allStationsJob?.cancel()
        allStationsJob = viewModelScope.launch(Dispatchers.IO) {
            val allStations = mutableListOf<StationInfoItem>()
            for (city in cities) {
                try {
                    delay(400)
                    val response = repository.getStationInfo(city)
                    allStations.addAll(response)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _allStations.value = allStations
            Log.d(TAG, "[fetchAllStations] _allStations size  : ${_allStations.value.size}")
        }
    }

    private fun fetchAllAvailability() {
        allAvailableJob?.cancel()
        allAvailableJob = viewModelScope.launch(Dispatchers.IO) {
            val allAvailabilities = mutableListOf<AvailableInfoItem>()
            for (city in cities) {
                try {
                    delay(400)
                    val response = repository.getAvailableInfo(city)
                    allAvailabilities.addAll(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _allAvailable.value = allAvailabilities
            Log.d(TAG, "[fetchAllAvailability] _allAvailable size  : ${_allAvailable.value.size}")
        }
    }


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