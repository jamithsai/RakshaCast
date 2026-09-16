from fastapi import APIRouter, HTTPException
from typing import List, Optional
from app.schemas.ml_models import RiskPrediction
from app.schemas.data_models import WeatherFeatureRecord
from app.ml.inference import InferenceService
from app.services.notification_service import evaluate_and_push
import datetime

router = APIRouter()

# Global inference service instance ensures models are loaded once
inference_service = InferenceService()

@router.post("/prediction", response_model=List[RiskPrediction])
def get_prediction(records: List[WeatherFeatureRecord], hazard: str, forecastHour: int = 0):
    if hazard not in ["THUNDERSTORM", "CLOUDBURST", "FLASH_FLOOD"]:
        raise HTTPException(status_code=400, detail="Invalid hazard type requested.")
        
    try:
        predictions = inference_service.predict(records, hazard, forecastHour)
        # Push notification evaluation
        evaluate_and_push(predictions)
        return predictions
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/prediction/demo", response_model=List[RiskPrediction])
def get_demo_prediction(hazard: str = "THUNDERSTORM"):
    """Convenience endpoint to simulate a prediction without POSTing features."""
    # Adjust rainfall based on hazard to ensure the model produces positive demo results
    demo_rainfall = 25.4
    if hazard == "CLOUDBURST":
        demo_rainfall = 65.0
    elif hazard == "FLASH_FLOOD":
        demo_rainfall = 45.0
        
    record = WeatherFeatureRecord(
        timestamp=datetime.datetime.now(datetime.timezone.utc).isoformat(),
        latitude=17.3850,
        longitude=78.4867,
        rainfall_mm_hr=demo_rainfall,
        precipitable_water=60.1,
        instability_li=-5.5,
        theta_e=345.0,
        cloud_top_temperature=-70.0,
        cloud_top_pressure=150.0,
        surface_temperature=29.0,
        cloud_mask_flag=1,
        wind_index=2.5,
        source_metadata=[]
    )
    
    predictions = inference_service.predict([record], hazard, forecast_hour=2)
    # Push notification evaluation
    evaluate_and_push(predictions)
    return predictions
