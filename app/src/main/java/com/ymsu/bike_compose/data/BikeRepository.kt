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
    // 用state flow? or flow?
    suspend fun getStationInfo(city: String): List<StationInfoItem> =  apiService.getStationInfo(city)
    suspend fun getAvailableInfo(city: String):List<AvailableInfoItem> = apiService.getAvailableInfo(city)
    suspend fun getNearByStationInfo(nearBy: String): List<StationInfoItem> =  apiService.getStationInfoNearBy(nearBy)
    suspend fun getNearByAvailableInfo(nearBy: String): List<AvailableInfoItem> =  apiService.getAvailabilityInfoNearBy(nearBy)

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
            is IOException -> "網路連線錯誤"
            is HttpException -> "Server error: ${exception.code()}"
            else -> "Unknown error: ${exception.localizedMessage}"
        }
    }
}