package com.ymsu.bike_compose.data

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object TokenManager {
    private var accessToken: String? = null
    private var tokenExpiryTime: Long = 0

    suspend fun getAccessToken(): String {
        val currentTime = System.currentTimeMillis()
        if (accessToken == null || currentTime >= tokenExpiryTime) {
            refreshToken()
        }
        return accessToken ?: ""
    }

    private suspend fun refreshToken() {
        val clientId = "f61434-61c72fa6-5118-433c"
        val clientSecret = "5883cba9-2468-4477-bb2a-8cd3389291e2"
        val url = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"

        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.string()?.let {
                val json = JSONObject(it)
                accessToken = json.getString("access_token")
                val expiresIn = json.getLong("expires_in")
                tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000)
            }
        } else {
            throw Exception("Failed to get access token")
        }
    }
}