package com.rakshacast.model

import com.google.gson.annotations.SerializedName

enum class RiskLevel {
    LOW, MODERATE, HIGH, SEVERE, EXTREME
}

data class Location(
    val name: String,
    @SerializedName("latitude") val lat: Double,
    @SerializedName("longitude") val lng: Double
)

data class WeatherIndicators(
    val rainfallMmHr: Double,
    val temperatureC: Double,
    val moistureLevel: String,
    val instability: String,
    val cloudCondition: String
)

enum class AlertStatus {
    ACTIVE, UPCOMING, RESOLVED
}

data class Alert(
    val id: String,
    val hazard: String,
    val severity: RiskLevel,
    val location: Location,
    val probability: Int,
    val expectedTimeframe: String,
    val status: AlertStatus,
    val timestamp: String,
    val message: String,
    val explanationFactors: List<RiskFactor>,
    val recommendedActions: List<String>,
    val isRead: Boolean,
    val modelVersion: String? = null,
    val prototypeFlag: Boolean = true
)

enum class RiskTrend {
    RISING, STABLE, FALLING
}

data class RiskFactor(
    val factor: String,
    val status: String,
    val explanation: String = "",
    val importance: Float = 0f, // 0.0 to 1.0 qualitative importance
    val trend: RiskTrend = RiskTrend.STABLE
)

data class ForecastPoint(
    val timeLabel: String,
    val riskLevel: RiskLevel
)

data class WeatherRisk(
    val overallRisk: RiskLevel,
    val primaryHazard: String,
    val predictionWindow: String,
    val probability: Int,
    val explanationFactors: List<RiskFactor>,
    val indicators: WeatherIndicators,
    val forecastTimeline: List<ForecastPoint>,
    val activeAlerts: List<Alert>,
    val modelVersion: String? = null,
    val prototypeFlag: Boolean = true
)

enum class ShelterStatus {
    AVAILABLE, LIMITED, FULL, CLOSED
}

data class Shelter(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val capacity: Int,
    val status: ShelterStatus,
    val availabilityText: String
)

data class EmergencyContact(
    val name: String,
    val description: String,
    val number: String
)

data class SafetyGuidance(
    val hazardType: String,
    val title: String,
    val instructions: List<String>
)

data class RiskZone(
    val id: String,
    val name: String,
    val hazard: String,
    val riskLevel: RiskLevel,
    val probability: Int,
    val expectedWindow: String,
    val explanationFactors: List<RiskFactor>,
    val centerLat: Double,
    val centerLng: Double,
    val radiusKm: Double,
    val forecastHour: Int // 0 for NOW, 1 for +1H, etc.
)

data class ObservationMetadata(
    val filename: String,
    val dataset_type: String,
    val sensor: String,
    val processing_level: String,
    val acquisition_date: String,
    val acquisition_start_time: String,
    val valid: Boolean,
    val error_message: String? = null
)

data class WeatherFeatureRecord(
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val rainfall_mm_hr: Double? = null,
    val precipitable_water: Double? = null,
    val instability_li: Double? = null,
    val theta_e: Double? = null,
    val cloud_top_temperature: Double? = null,
    val cloud_top_pressure: Double? = null,
    val surface_temperature: Double? = null,
    val cloud_mask_flag: Int? = null,
    val wind_index: Double? = null,
    val source_metadata: List<ObservationMetadata> = emptyList()
)

data class RiskPrediction(
    val hazardType: String,
    val probability: Int,
    val riskLevel: String,
    val forecastHour: Int,
    val location: Location,
    val timestamp: String,
    val trend: String,
    val explanationFactors: List<RiskFactor>,
    val prototypeFlag: Boolean = true,
    val model_version: String
)


