package com.rakshacast.repository

import com.rakshacast.model.*
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getRiskData(): Flow<Result<WeatherRisk>>
    fun getAlerts(): Flow<Result<List<Alert>>>
    fun getShelters(): Flow<Result<List<Shelter>>>
    fun getRiskZones(): Flow<Result<List<RiskZone>>>
    fun getEmergencyContacts(): Flow<Result<List<EmergencyContact>>>
    fun getSafetyGuidance(): Flow<Result<List<SafetyGuidance>>>
}
