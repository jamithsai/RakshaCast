import uuid
from typing import List
from app.schemas.api_models import Alert, RiskZone
from app.schemas.ml_models import RiskPrediction

class PredictionIntegrator:
    @staticmethod
    def prediction_to_risk_zone(prediction: RiskPrediction) -> RiskZone:
        return RiskZone(
            id=str(uuid.uuid4()),
            name=prediction.location.name,
            hazard=prediction.hazardType,
            riskLevel=prediction.riskLevel,
            probability=prediction.probability,
            expectedWindow=f"Within {prediction.forecastHour} hours",
            explanationFactors=prediction.explanationFactors,
            centerLat=prediction.location.latitude,
            centerLng=prediction.location.longitude,
            radiusKm=15.0, # Default prototype radius
            forecastHour=prediction.forecastHour
        )

    @staticmethod
    def prediction_to_alert_candidate(prediction: RiskPrediction) -> Alert:
        # Only generates an active alert candidate; system rules would normally gate this
        severity_map = {
            "EXTREME": "HIGH",
            "SEVERE": "HIGH",
            "HIGH": "HIGH",
            "MODERATE": "MODERATE",
            "LOW": "LOW"
        }
        
        return Alert(
            id=str(uuid.uuid4()),
            hazard=prediction.hazardType,
            severity=severity_map.get(prediction.riskLevel, "MODERATE"),
            location=prediction.location,
            probability=prediction.probability,
            expectedTimeframe=f"Within {prediction.forecastHour} hours",
            status="ACTIVE",
            timestamp=prediction.timestamp,
            message=f"Elevated risk of {prediction.hazardType} predicted by baseline model.",
            explanationFactors=prediction.explanationFactors,
            recommendedActions=["Monitor official warnings", "Prepare for severe weather"],
            isRead=False
        )
