package com.ymsu.bike_compose.data

import com.ymsu.bike_compose.room.FavoriteStationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Dispatcher
import okio.IOException
import retrofit2.HttpException
import java.lang.Exception
import javax.inject.Inject

class BikeRepository(private val apiService: BikeApiService) {
    suspend fun getAllStationFromFlask(): List<StationInfoFromFlaskItem> = apiService.getAllStationsFromFlask()
    suspend fun getNearByStationFromFlask(lat:Float,lng:Float,range:Float): List<StationInfoFromFlaskItem> =
        apiService.getNearByStationsFromFlask(lat, lng, range)

    private fun <T> safeApiCall( call : suspend ()->T): Flow<ApiResult<T>> = flow {
        emit(ApiResult.Loading)
        val response = call()
        emit(ApiResult.Success(response))
    }.flowOn(
        Dispatchers.IO
    ).catch { exception ->
        emit(ApiResult.Error(handleException(exception)))
    }

    private fun handleException(exception: Throwable): String{
        return when(exception) {
            is IOException -> "Connection error: ${exception.message}"
            is HttpException -> "Server error: ${exception.code()}"
            else -> "Unknown error: ${exception.localizedMessage}"
        }
    }
}