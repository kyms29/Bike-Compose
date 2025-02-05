package com.ymsu.bike_compose.data

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface BikeApiService {
    @GET("Bike/Station/City/NewTaipei")
    suspend fun getStationInfo(): List<StationInfoItem>
    @GET("Bike/Availability/City/NewTaipei")
    suspend fun getAvailableInfo() : List<AvailableInfoItem>

    companion object {
        private const val TDX_URL = "https://tdx.transportdata.tw/api/basic/v2/"

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
