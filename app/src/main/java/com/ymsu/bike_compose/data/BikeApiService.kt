package com.ymsu.bike_compose.data

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BikeApiService {
    @GET("all_stations")
    suspend fun getAllStationsFromFlask() : List<StationInfoFromFlaskItem>

    @GET("nearby_stations")
    suspend fun getNearByStationsFromFlask(
        @Query("lat") lat: Float,
        @Query("lng") lng: Float,
        @Query("range") range: Float
    ): List<StationInfoFromFlaskItem>

    companion object {
        private const val FLASK_URL = "http://10.113.161.59:5000/"

        fun create(): BikeApiService {
            return Retrofit
                .Builder()
                .baseUrl(FLASK_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BikeApiService::class.java)
        }
    }
}

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getAccessToken() }  // 確保 token 是最新的
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
