import pandas as pd
import numpy as np
from typing import List, Dict, Any
from app.schemas.data_models import WeatherFeatureRecord

class FeaturePreprocessor:
    """Handles identical feature selection, ordering, and missing value logic for training and inference."""
    
    EXPECTED_FEATURES = [
        "rainfall_mm_hr",
        "precipitable_water",
        "instability_li",
        "theta_e",
        "cloud_top_temperature",
        "cloud_top_pressure",
        "surface_temperature",
        "cloud_mask_flag",
        "wind_index",
        "latitude",
        "longitude"
    ]

    def transform_single(self, record: WeatherFeatureRecord) -> np.ndarray:
        """Transforms a single record into an array matching EXPECTED_FEATURES."""
        record_dict = record.model_dump()
        arr = []
        for feature in self.EXPECTED_FEATURES:
            val = record_dict.get(feature)
            if val is None:
                val = np.nan
            arr.append(val)
        return np.array(arr, dtype=np.float32).reshape(1, -1)

    def transform_batch(self, records: List[WeatherFeatureRecord]) -> np.ndarray:
        if not records:
            return np.empty((0, len(self.EXPECTED_FEATURES)))
        
        arr = []
        for record in records:
            arr.append(self.transform_single(record)[0])
        return np.array(arr, dtype=np.float32)
