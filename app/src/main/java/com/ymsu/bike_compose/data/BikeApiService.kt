package com.ymsu.bike_compose.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface BikeApiService {
    @GET("all_stations")
    suspend fun getAllStationsFromFlask(): List<StationInfoDetail>

    @GET("nearby_stations")
    suspend fun getNearByStationsFromFlask(
        @Query("lat") lat: Float,
        @Query("lng") lng: Float,
        @Query("range") range: Float
    ): List<StationInfoDetail>

    companion object {
        private const val FLASK_URL = "https://192.168.50.70:5000/"

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
