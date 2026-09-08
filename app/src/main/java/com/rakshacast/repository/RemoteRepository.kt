package com.rakshacast.repository

import com.rakshacast.model.*
import com.rakshacast.network.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteRepository : AppRepository {
    private val api = ApiClient.retrofitService

    override fun getRiskData(): Flow<Result<WeatherRisk>> = flow {
        try {
            val baseResponse = api.getCurrentRisk()
            
            // Integrate actual ML prediction
            val mlResponse = api.getDemoPrediction("THUNDERSTORM").firstOrNull()
            
            if (mlResponse != null) {
                val updatedRisk = baseResponse.copy(
                    probability = mlResponse.probability,
                    overallRisk = RiskLevel.valueOf(mlResponse.riskLevel),
                    explanationFactors = mlResponse.explanationFactors
                )
                emit(Result.success(updatedRisk))
            } else {
                emit(Result.success(baseResponse))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getAlerts(): Flow<Result<List<Alert>>> = flow {
        try {
            val response = api.getAlerts()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getShelters(): Flow<Result<List<Shelter>>> = flow {
        try {
            val response = api.getShelters()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getRiskZones(): Flow<Result<List<RiskZone>>> = flow {
        try {
            val response = api.getRiskMap()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getEmergencyContacts(): Flow<Result<List<EmergencyContact>>> = flow {
        try {
            val response = api.getEmergencyContacts()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getSafetyGuidance(): Flow<Result<List<SafetyGuidance>>> = flow {
        try {
            val response = api.getSafetyGuidance()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
