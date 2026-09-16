package com.rakshacast.network

import com.rakshacast.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RakshaCastApiService {
    @retrofit2.http.POST("api/v1/notifications/register")
    suspend fun registerFcmToken(@retrofit2.http.Body request: Map<String, String>): retrofit2.Response<Unit>

    @GET("api/v1/risk/current")
    suspend fun getCurrentRisk(): WeatherRisk

    @GET("api/v1/risk/map")
    suspend fun getRiskMap(
        @Query("hazard") hazard: String? = null,
        @Query("forecastHour") forecastHour: Int? = null
    ): List<RiskZone>

    @GET("api/v1/alerts")
    suspend fun getAlerts(
        @Query("hazard") hazard: String? = null,
        @Query("status") status: String? = null
    ): List<Alert>

    @GET("api/v1/emergency/contacts")
    suspend fun getEmergencyContacts(): List<EmergencyContact>

    @GET("api/v1/emergency/shelters")
    suspend fun getShelters(): List<Shelter>

    @GET("api/v1/emergency/guidance")
    suspend fun getSafetyGuidance(): List<SafetyGuidance>

    @retrofit2.http.POST("api/v1/prediction")
    suspend fun getPrediction(
        @retrofit2.http.Body records: List<WeatherFeatureRecord>,
        @Query("hazard") hazard: String,
        @Query("forecastHour") forecastHour: Int
    ): List<RiskPrediction>

    @GET("api/v1/prediction/demo")
    suspend fun getDemoPrediction(
        @Query("hazard") hazard: String
    ): List<RiskPrediction>
}

object ApiClient {
    // 10.0.2.2 is the localhost loopback for Android Emulator
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val retrofitService: RakshaCastApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RakshaCastApiService::class.java)
    }
}
