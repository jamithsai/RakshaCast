from app.services.notification_service import register_token, test_notification, FCMRegistrationRequest, reset_cooldown
from fastapi import APIRouter, Query
from typing import List, Optional
from app.schemas.api_models import (
    RiskResponse, Alert, RiskZone, Shelter, EmergencyContact, SafetyGuidance,
    Location, RiskFactor, ForecastPoint, WeatherIndicators
)

router = APIRouter()

# Mock data
HYDERABAD_LOC = Location(name="Hyderabad, Telangana", latitude=17.3850, longitude=78.4867)

MOCK_FACTORS = [
    RiskFactor(factor="Atmospheric moisture", status="HIGH", explanation="High moisture provides water vapor to support deep convection.", importance=0.9, trend="RISING"),
    RiskFactor(factor="Atmospheric instability", status="HIGH", explanation="Instability increases the potential for rapid thunderstorms.", importance=0.8, trend="RISING"),
    RiskFactor(factor="Cloud development", status="RAPID", explanation="Rapid changes in cloud conditions suggest active growth.", importance=0.7, trend="STABLE"),
    RiskFactor(factor="Rainfall intensity", status="RISING", explanation="Rainfall intensity is increasing over recent observations.", importance=0.6, trend="RISING")
]

MOCK_ALERTS = [
    Alert(
        id="1", hazard="Severe Thunderstorm", severity="HIGH", location=HYDERABAD_LOC,
        probability=82, expectedTimeframe="Within 2–3 hours", status="ACTIVE",
        timestamp="Just Now", message="Rapid cloud development and increasing instability indicate an elevated thunderstorm risk.",
        explanationFactors=MOCK_FACTORS[:3],
        recommendedActions=["Avoid exposed/open areas", "Stay indoors where possible", "Avoid unnecessary travel", "Monitor further RakshaCast warnings"],
        isRead=False
    ),
    Alert(
        id="2", hazard="Flash Flood", severity="MODERATE", location=HYDERABAD_LOC,
        probability=64, expectedTimeframe="Within 4–6 hours", status="UPCOMING",
        timestamp="20 mins ago", message="Heavy rainfall in catchment areas increasing flood risk.",
        explanationFactors=[
            RiskFactor(factor="Rainfall Intensity", status="STEADY", explanation="Continuous rainfall is slowly saturating the soil.", importance=0.6, trend="STABLE")
        ],
        recommendedActions=["Move away from low-lying areas", "Avoid flooded roads"],
        isRead=True
    )
]

@router.get("/risk/current", response_model=RiskResponse)
def get_current_risk():
    return RiskResponse(
        overallRisk="HIGH",
        primaryHazard="Severe Thunderstorm",
        predictionWindow="Expected within 2–3 hours",
        probability=82,
        explanationFactors=MOCK_FACTORS,
        indicators=WeatherIndicators(
            rainfallMmHr=18.4, temperatureC=28.0, moistureLevel="High",
            instability="High", cloudCondition="Rapid"
        ),
        forecastTimeline=[
            ForecastPoint(timeLabel="NOW", riskLevel="MODERATE"),
            ForecastPoint(timeLabel="+1H", riskLevel="HIGH"),
            ForecastPoint(timeLabel="+2H", riskLevel="HIGH"),
            ForecastPoint(timeLabel="+3H", riskLevel="SEVERE")
        ],
        activeAlerts=[a for a in MOCK_ALERTS if a.status == "ACTIVE"]
    )

@router.get("/risk/map", response_model=List[RiskZone])
def get_risk_map(hazard: Optional[str] = None, forecastHour: Optional[int] = None):
    zones = [
        RiskZone(
            id="z1", name="Hyderabad", hazard="Thunderstorm", riskLevel="HIGH", probability=82,
            expectedWindow="Within 2–3 hours", explanationFactors=[MOCK_FACTORS[0]],
            centerLat=17.3850, centerLng=78.4867, radiusKm=15.0, forecastHour=0
        ),
        RiskZone(
            id="z1_3", name="Hyderabad", hazard="Thunderstorm", riskLevel="SEVERE", probability=95,
            expectedWindow="Now", explanationFactors=[MOCK_FACTORS[1]],
            centerLat=17.3850, centerLng=78.4867, radiusKm=20.0, forecastHour=3
        )
    ]
    
    if hazard:
        zones = [z for z in zones if z.hazard.lower() == hazard.lower()]
    if forecastHour is not None:
        zones = [z for z in zones if z.forecastHour == forecastHour]
        
    return zones

@router.get("/alerts", response_model=List[Alert])
def get_alerts(hazard: Optional[str] = None, status: Optional[str] = None):
    alerts = MOCK_ALERTS
    if hazard:
        alerts = [a for a in alerts if a.hazard.lower() == hazard.lower()]
    if status:
        alerts = [a for a in alerts if a.status.lower() == status.lower()]
    return alerts

@router.get("/emergency/contacts", response_model=List[EmergencyContact])
def get_emergency_contacts():
    return [
        EmergencyContact(name="National Emergency", description="All-in-one emergency service", number="112"),
        EmergencyContact(name="Police", description="Local law enforcement", number="100")
    ]

@router.get("/emergency/shelters", response_model=List[Shelter])
def get_shelters():
    return [
        Shelter(id="s1", name="Community Relief Center", distanceKm=2.4, capacity=120, status="AVAILABLE", availabilityText="Accepting people"),
        Shelter(id="s2", name="District Emergency Shelter", distanceKm=4.1, capacity=250, status="LIMITED", availabilityText="Near capacity")
    ]

@router.get("/emergency/guidance", response_model=List[SafetyGuidance])
def get_safety_guidance():
    return [
        SafetyGuidance(
            hazardType="Thunderstorm",
            title="Severe Thunderstorm Safety",
            instructions=["Stay indoors when possible.", "Avoid open areas."]
        ),
        SafetyGuidance(
            hazardType="Flash Flood",
            title="Flash Flood Safety",
            instructions=["Move away from low-lying or flood-prone areas.", "Never attempt to cross moving water."]
        )
    ]

from app.schemas.data_models import WeatherFeatureRecord, ObservationMetadata

@router.get("/data/status")
def get_data_pipeline_status():
    return {
        "status": "operational",
        "pipeline": "MOSDAC_HDF5_INGESTION",
        "supported_datasets": ["3RSND_L2B_SA1", "3RIMG_L2B_IMC", "3RIMG_L2B_CMK"]
    }

@router.get("/data/features", response_model=List[WeatherFeatureRecord])
def get_extracted_features():
    from app.features.extractor import FeatureExtractor
    extractor = FeatureExtractor()
    metadata = [
        ObservationMetadata(
            filename="3RSND_07SEP2026_0000_L2B_SA1_V01R00.h5",
            dataset_type="SOUNDER",
            sensor="INSAT-3DR",
            processing_level="L2B",
            acquisition_date="07-SEP-2026",
            acquisition_start_time="00:00",
            valid=True
        )
    ]
    return extractor.extract_features({}, {}, {}, metadata)

@router.post("/notifications/register")
def register_fcm_token(request: FCMRegistrationRequest):
    register_token(request.token)
    return {"status": "success", "message": "Token registered successfully"}

@router.post("/notifications/test")
def test_fcm_notification():
    """Test endpoint to trigger a demo FCM notification."""
    result = test_notification()
    return result

@router.post("/notifications/reset")
def reset_fcm_cooldown():
    """Reset the deduplication cooldown state for testing."""
    reset_cooldown()
    return {"status": "success", "message": "Cooldown state reset"}
