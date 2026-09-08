import pytest
import numpy as np
from app.ml.preprocessing import FeaturePreprocessor
from app.schemas.data_models import WeatherFeatureRecord
from app.ml.model import classify_risk_level

def test_feature_preprocessor_single():
    preprocessor = FeaturePreprocessor()
    record = WeatherFeatureRecord(
        timestamp="2026-09-07T12:00:00Z", latitude=17.0, longitude=78.0,
        rainfall_mm_hr=10.0, cloud_mask_flag=1 # other features will be NaN
    )
    arr = preprocessor.transform_single(record)
    assert arr.shape == (1, 11)
    # Check that rainfall is 10.0 and others are NaN appropriately
    assert arr[0][0] == 10.0
    assert np.isnan(arr[0][1]) # precipitable_water

def test_classify_risk_level():
    assert classify_risk_level(0.9) == "EXTREME"
    assert classify_risk_level(0.75) == "SEVERE"
    assert classify_risk_level(0.6) == "HIGH"
    assert classify_risk_level(0.4) == "MODERATE"
    assert classify_risk_level(0.1) == "LOW"
