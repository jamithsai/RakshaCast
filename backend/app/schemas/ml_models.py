from pydantic import BaseModel, Field
from typing import List, Optional
from app.schemas.api_models import Location, RiskFactor

class RiskPrediction(BaseModel):
    hazardType: str
    probability: int  # 0-100
    riskLevel: str
    forecastHour: int
    location: Location
    timestamp: str
    trend: str
    explanationFactors: List[RiskFactor]
    prototypeFlag: bool = True
    model_version: str = "xgboost-baseline-v1.0"

class MLModelMetadata(BaseModel):
    model_name: str
    model_version: str
    training_timestamp: str
    feature_version: str
    prototype_flag: bool
