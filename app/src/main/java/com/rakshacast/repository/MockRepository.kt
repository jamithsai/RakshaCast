package com.rakshacast.repository

import com.rakshacast.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockRepository(val scenario: String = "Severe Thunderstorm") : AppRepository {
    private val hyderabad = Location("Hyderabad, Telangana", 17.3850, 78.4867)
    private val hills = Location("Munnar, Kerala", 10.0889, 77.0595)

    override fun getRiskData(): Flow<Result<WeatherRisk>> = flow {
        delay(500) // Simulate network delay
        val indicators = WeatherIndicators(
            rainfallMmHr = 18.4,
            temperatureC = 28.0,
            moistureLevel = "High",
            instability = "High",
            cloudCondition = "Rapid"
        )
        val alerts = listOf(
            Alert(
                id = "1", hazard = "Severe Thunderstorm", severity = RiskLevel.HIGH, location = hyderabad,
                probability = 82, expectedTimeframe = "Within 2–3 hours", status = AlertStatus.ACTIVE,
                timestamp = "Just Now", message = "Rapid cloud development and increasing instability indicate an elevated thunderstorm risk.",
                explanationFactors = listOf(
                    RiskFactor("Atmospheric Moisture", "HIGH", "High moisture availability can support stronger convective development.", 0.9f, RiskTrend.RISING),
                    RiskFactor("Instability", "HIGH", "Instability increases the potential for rapidly developing thunderstorms.", 0.85f, RiskTrend.RISING),
                    RiskFactor("Cloud Development", "RAPID", "Rapid cloud development indicates active convective growth.", 0.7f, RiskTrend.STABLE)
                ),
                recommendedActions = listOf("Avoid exposed/open areas", "Stay indoors where possible", "Avoid unnecessary travel"),
                isRead = false
            ),
            Alert(
                id = "2", hazard = "Flash Flood", severity = RiskLevel.MODERATE, location = hyderabad,
                probability = 64, expectedTimeframe = "Within 4–6 hours", status = AlertStatus.UPCOMING,
                timestamp = "20 mins ago", message = "Heavy rainfall in catchment areas increasing flood risk.",
                explanationFactors = listOf(
                    RiskFactor("Rainfall Intensity", "STEADY", "Continuous rainfall is slowly saturating the soil.", 0.6f, RiskTrend.STABLE),
                    RiskFactor("Terrain Vulnerability", "ELEVATED", "Local terrain may increase the potential impact of runoff.", 0.5f, RiskTrend.STABLE)
                ),
                recommendedActions = listOf("Move away from low-lying areas", "Avoid flooded roads"),
                isRead = true
            ),
            Alert(
                id = "3", hazard = "Cloudburst", severity = RiskLevel.SEVERE, location = hills,
                probability = 78, expectedTimeframe = "Within 1 hour", status = AlertStatus.ACTIVE,
                timestamp = "1 hour ago", message = "Extreme localized rainfall expected.",
                explanationFactors = listOf(
                    RiskFactor("Rainfall Intensity", "EXTREME", "Rainfall intensity is increasing rapidly over recent observations.", 0.95f, RiskTrend.RISING),
                    RiskFactor("Cloud Development", "RAPID", "Deep convective cells are stalling over the terrain.", 0.8f, RiskTrend.STABLE)
                ),
                recommendedActions = listOf("Move toward safer/elevated locations", "Avoid streams and drainage channels"),
                isRead = false
            ),
            Alert(
                id = "4", hazard = "Heavy Rain", severity = RiskLevel.MODERATE, location = hyderabad,
                probability = 100, expectedTimeframe = "Ended 45 minutes ago", status = AlertStatus.RESOLVED,
                timestamp = "06 SEP", message = "Heavy rain risk has passed.",
                explanationFactors = listOf(), recommendedActions = listOf(),
                isRead = true
            )
        )
        emit(Result.success(
            WeatherRisk(
                overallRisk = RiskLevel.HIGH,
                primaryHazard = "Severe Thunderstorm",
                predictionWindow = "Expected within 2–3 hours",
                probability = 82,
                explanationFactors = listOf(
                    RiskFactor("Atmospheric moisture", "HIGH", "High moisture provides water vapor to support deep convection.", 0.9f, RiskTrend.RISING),
                    RiskFactor("Atmospheric instability", "HIGH", "Instability increases the potential for rapid thunderstorms.", 0.8f, RiskTrend.RISING),
                    RiskFactor("Cloud development", "RAPID", "Rapid changes in cloud conditions suggest active growth.", 0.7f, RiskTrend.STABLE),
                    RiskFactor("Rainfall intensity", "RISING", "Rainfall intensity is increasing over recent observations.", 0.6f, RiskTrend.RISING)
                ),
                indicators = indicators,
                forecastTimeline = listOf(
                    ForecastPoint("NOW", RiskLevel.MODERATE),
                    ForecastPoint("+1H", RiskLevel.HIGH),
                    ForecastPoint("+2H", RiskLevel.HIGH),
                    ForecastPoint("+3H", RiskLevel.SEVERE),
                    ForecastPoint("+4H", RiskLevel.HIGH),
                    ForecastPoint("+5H", RiskLevel.MODERATE),
                    ForecastPoint("+6H", RiskLevel.LOW)
                ),
                activeAlerts = alerts.filter { it.status == AlertStatus.ACTIVE }
            )
        ))
    }

    override fun getAlerts(): Flow<Result<List<Alert>>> = flow {
        delay(300)
        emit(Result.success(listOf(
            Alert(
                id = "1", hazard = "Severe Thunderstorm", severity = RiskLevel.HIGH, location = hyderabad,
                probability = 82, expectedTimeframe = "Within 2–3 hours", status = AlertStatus.ACTIVE,
                timestamp = "Just Now", message = "Rapid cloud development and increasing instability indicate an elevated thunderstorm risk.",
                explanationFactors = listOf(
                    RiskFactor("Atmospheric Moisture", "HIGH", "High moisture availability can support stronger convective development.", 0.9f, RiskTrend.RISING),
                    RiskFactor("Instability", "HIGH", "Instability increases the potential for rapidly developing thunderstorms.", 0.85f, RiskTrend.RISING),
                    RiskFactor("Cloud Development", "RAPID", "Rapid cloud development indicates active convective growth.", 0.7f, RiskTrend.STABLE)
                ),
                recommendedActions = listOf("Avoid exposed/open areas", "Stay indoors where possible", "Avoid unnecessary travel", "Monitor further RakshaCast warnings"),
                isRead = false
            ),
            Alert(
                id = "2", hazard = "Flash Flood", severity = RiskLevel.MODERATE, location = hyderabad,
                probability = 64, expectedTimeframe = "Within 4–6 hours", status = AlertStatus.UPCOMING,
                timestamp = "20 mins ago", message = "Heavy rainfall in catchment areas increasing flood risk.",
                explanationFactors = listOf(
                    RiskFactor("Rainfall Intensity", "STEADY", "Continuous rainfall is slowly saturating the soil.", 0.6f, RiskTrend.STABLE),
                    RiskFactor("Terrain Vulnerability", "ELEVATED", "Local terrain may increase the potential impact of runoff.", 0.5f, RiskTrend.STABLE)
                ),
                recommendedActions = listOf("Move away from low-lying areas", "Avoid flooded roads", "Do not attempt to cross moving water"),
                isRead = true
            ),
            Alert(
                id = "3", hazard = "Cloudburst", severity = RiskLevel.SEVERE, location = hills,
                probability = 78, expectedTimeframe = "Within 1 hour", status = AlertStatus.ACTIVE,
                timestamp = "1 hour ago", message = "Extreme localized rainfall expected.",
                explanationFactors = listOf(
                    RiskFactor("Rainfall Intensity", "EXTREME", "Rainfall intensity is increasing rapidly over recent observations.", 0.95f, RiskTrend.RISING),
                    RiskFactor("Cloud Development", "RAPID", "Deep convective cells are stalling over the terrain.", 0.8f, RiskTrend.STABLE)
                ),
                recommendedActions = listOf("Move toward safer/elevated locations where appropriate", "Avoid streams, drainage channels, and low-lying areas", "Follow local evacuation guidance"),
                isRead = false
            ),
            Alert(
                id = "4", hazard = "Heavy Rain / Flood Risk", severity = RiskLevel.MODERATE, location = hyderabad,
                probability = 100, expectedTimeframe = "Ended 45 minutes ago", status = AlertStatus.RESOLVED,
                timestamp = "06 SEP", message = "Heavy rain risk has passed.",
                explanationFactors = listOf(), recommendedActions = listOf(),
                isRead = true
            )
        )))
    }

    override fun getShelters(): Flow<Result<List<Shelter>>> = flow {
        emit(Result.success(listOf(
            Shelter("s1", "Community Relief Center", 2.4, 120, ShelterStatus.AVAILABLE, "Accepting people"),
            Shelter("s2", "District Emergency Shelter", 4.1, 250, ShelterStatus.LIMITED, "Near capacity"),
            Shelter("s3", "Govt School Block B", 5.0, 100, ShelterStatus.CLOSED, "Not yet active")
        )))
    }

    override fun getEmergencyContacts(): Flow<Result<List<EmergencyContact>>> = flow {
        emit(Result.success(listOf(
            EmergencyContact("National Emergency", "All-in-one emergency service", "112"),
            EmergencyContact("Police", "Local law enforcement", "100"),
            EmergencyContact("Fire & Rescue", "Fire and disaster rescue", "101"),
            EmergencyContact("Ambulance", "Medical emergencies", "102"),
            EmergencyContact("NDMA Helpline", "National Disaster Management", "1078")
        )))
    }

    override fun getSafetyGuidance(): Flow<Result<List<SafetyGuidance>>> = flow {
        emit(Result.success(listOf(
            SafetyGuidance(
                "Thunderstorm",
                "Severe Thunderstorm Safety",
                listOf(
                    "Stay indoors when possible.",
                    "Avoid open areas.",
                    "Avoid isolated trees and exposed structures.",
                    "Avoid unnecessary travel.",
                    "Follow official warnings."
                )
            ),
            SafetyGuidance(
                "Flash Flood",
                "Flash Flood Safety",
                listOf(
                    "Move away from low-lying or flood-prone areas.",
                    "Never attempt to cross moving water.",
                    "Avoid flooded roads.",
                    "Move to safer/elevated locations where instructed.",
                    "Follow evacuation guidance."
                )
            ),
            SafetyGuidance(
                "Cloudburst",
                "Cloudburst Safety",
                listOf(
                    "Move away from streams, drainage channels, and low-lying areas.",
                    "Seek safer/elevated shelter where appropriate.",
                    "Avoid unnecessary movement during extreme rainfall.",
                    "Follow local evacuation instructions."
                )
            )
        )))
    }

    override fun getRiskZones(): Flow<Result<List<RiskZone>>> = flow {
        delay(200)
        emit(Result.success(listOf(
            // NOW
            RiskZone("z1", "Hyderabad", "Thunderstorm", RiskLevel.HIGH, 82, "Within 2–3 hours", listOf(RiskFactor("Moisture", "HIGH", "High moisture fuels convection.", 0.9f, RiskTrend.RISING)), 17.3850, 78.4867, 15.0, 0),
            RiskZone("z2", "Surrounding Area", "Flash Flood", RiskLevel.MODERATE, 64, "Within 4-6 hours", listOf(RiskFactor("Rainfall", "STEADY", "Continuous rain accumulating.", 0.6f, RiskTrend.STABLE)), 17.5, 78.6, 25.0, 0),
            RiskZone("z3", "Nearby Area", "Cloudburst", RiskLevel.SEVERE, 78, "Within 1 hour", listOf(RiskFactor("Cloud", "RAPID", "Deep convection stalling.", 0.8f, RiskTrend.RISING)), 17.2, 78.3, 10.0, 0),
            // +1H
            RiskZone("z1_1", "Hyderabad", "Thunderstorm", RiskLevel.HIGH, 85, "Within 1-2 hours", listOf(RiskFactor("Moisture", "HIGH", "High moisture fueling storm.", 0.9f, RiskTrend.RISING)), 17.3850, 78.4867, 18.0, 1),
            // +3H
            RiskZone("z1_3", "Hyderabad", "Thunderstorm", RiskLevel.SEVERE, 95, "Now", listOf(RiskFactor("Instability", "EXTREME", "Very high CAPE values.", 1.0f, RiskTrend.RISING)), 17.3850, 78.4867, 20.0, 3)
        )))
    }
}
