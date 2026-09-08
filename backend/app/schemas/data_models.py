from pydantic import BaseModel
from typing import Optional, Dict, Any, List

class ObservationMetadata(BaseModel):
    filename: str
    dataset_type: str
    sensor: str
    processing_level: str
    acquisition_date: str
    acquisition_start_time: str
    valid: bool
    error_message: Optional[str] = None

class WeatherFeatureRecord(BaseModel):
    timestamp: str
    latitude: float
    longitude: float
    rainfall_mm_hr: Optional[float] = None
    precipitable_water: Optional[float] = None
    instability_li: Optional[float] = None
    theta_e: Optional[float] = None
    cloud_top_temperature: Optional[float] = None
    cloud_top_pressure: Optional[float] = None
    surface_temperature: Optional[float] = None
    cloud_mask_flag: Optional[int] = None
    wind_index: Optional[float] = None
    source_metadata: List[ObservationMetadata] = []
