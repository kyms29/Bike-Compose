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
    @GET("api/basic/v2/Bike/Station/City/{City}")
    suspend fun getStationInfo(@Path("City") city: String): List<StationInfoItem>

    @GET("api/basic/v2/Bike/Availability/City/{City}")
    suspend fun getAvailableInfo(@Path("City") city: String) : List<AvailableInfoItem>

    @GET("api/advanced/v2/Bike/Station/NearBy")
    suspend fun getStationInfoNearBy(@Query("\$spatialFilter") nearBy: String): List<StationInfoItem>

    @GET("api/advanced/v2/Bike/Availability/NearBy")
    suspend fun getAvailabilityInfoNearBy(@Query("\$spatialFilter") nearBy: String): List<AvailableInfoItem>

    companion object {
        private const val TDX_URL = "https://tdx.transportdata.tw/"

        fun create(): BikeApiService {
            val tokenManager = TokenManager
            val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(tokenManager)).build()

            return Retrofit
                .Builder()
                .baseUrl(TDX_URL)
                .client(client)
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
