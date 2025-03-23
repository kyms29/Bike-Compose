package com.ymsu.bike_compose.data

import okio.IOException
import retrofit2.HttpException

class BikeRepository(private val apiService: BikeApiService) {
    suspend fun getAllStationFromFlask(): ApiResult<List<StationInfoDetail>> {
        return try {
            ApiResult.Success(apiService.getAllStationsFromFlask())
        } catch (exception: Exception) {
            ApiResult.Error(handleException(exception))
        }
    }

    suspend fun getNearByStationFromFlask(lat: Float, lng: Float, range: Float): ApiResult<List<StationInfoDetail>> {
        return try {
            ApiResult.Success(apiService.getNearByStationsFromFlask(lat, lng, range))
        } catch (exception: Exception) {
            ApiResult.Error(handleException(exception))
        }
    }

    private fun handleException(exception: Throwable): String {
        return when (exception) {
            is IOException -> "Connection error: ${exception.message}"
            is HttpException -> "Server error code : ${exception.code()}, message: ${exception.message} "
            else -> "Unknown error: ${exception.localizedMessage}"
        }
    }
}