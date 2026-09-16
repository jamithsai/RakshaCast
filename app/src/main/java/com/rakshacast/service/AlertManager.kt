package com.rakshacast.service

import com.rakshacast.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.UUID

class AlertManager {
    private val _generatedAlerts = MutableStateFlow<List<Alert>>(emptyList())
    val generatedAlerts: StateFlow<List<Alert>> = _generatedAlerts.asStateFlow()

    fun evaluateRiskAndGenerateAlert(risk: WeatherRisk, minSeveritySetting: String) {
        val severityThreshold = when (minSeveritySetting) {
            "ALL" -> 0
            "MODERATE OR HIGHER" -> RiskLevel.MODERATE.ordinal
            "HIGH / SEVERE" -> RiskLevel.HIGH.ordinal
            "EXTREME ONLY" -> RiskLevel.EXTREME.ordinal
            else -> RiskLevel.HIGH.ordinal
        }

        if (risk.overallRisk.ordinal >= severityThreshold && risk.overallRisk != RiskLevel.LOW) {
            val existingAlert = _generatedAlerts.value.find { 
                it.hazard == risk.primaryHazard && it.status == AlertStatus.ACTIVE
            }

            if (existingAlert == null || existingAlert.probability < risk.probability) {
                val newAlert = Alert(
                    id = existingAlert?.id ?: UUID.randomUUID().mostSignificantBits.toString(),
                    hazard = risk.primaryHazard,
                    severity = risk.overallRisk,
                    location = Location("Current Area", 17.3850, 78.4867),
                    probability = risk.probability,
                    expectedTimeframe = risk.predictionWindow,
                    status = AlertStatus.ACTIVE,
                    timestamp = "Just Now",
                    message = "System generated alert based on prototype model output.",
                    explanationFactors = risk.explanationFactors,
                    recommendedActions = listOf("Follow official instructions", "Avoid unnecessary travel"),
                    isRead = false,
                    modelVersion = risk.modelVersion ?: "XGBoost baseline v1.0",
                    prototypeFlag = true
                )
                
                val currentList = _generatedAlerts.value.toMutableList()
                existingAlert?.let { currentList.remove(it) }
                currentList.add(0, newAlert)
                _generatedAlerts.value = currentList
            }
        }
    }
}
