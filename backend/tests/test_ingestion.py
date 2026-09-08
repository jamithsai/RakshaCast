import pytest
import numpy as np
from app.processing.spatial import get_roi_mask, apply_roi
from app.features.extractor import FeatureExtractor
from app.schemas.data_models import ObservationMetadata

def test_roi_mask():
    lats = np.array([[15.0, 16.5], [17.5, 19.0]])
    lons = np.array([[76.0, 77.5], [78.5, 81.0]])
    mask = get_roi_mask(lats, lons, min_lat=16.0, max_lat=18.5, min_lon=77.0, max_lon=80.0)
    
    expected_mask = np.array([[False, True], [True, False]])
    np.testing.assert_array_equal(mask, expected_mask)

def test_apply_roi():
    data = np.array([[[1.0, 2.0], [3.0, 4.0]]])
    mask = np.array([[False, True], [True, False]])
    result = apply_roi(data, mask)
    np.testing.assert_array_equal(result, np.array([2.0, 3.0]))

def test_feature_extractor():
    extractor = FeatureExtractor()
    meta = [ObservationMetadata(
        filename="test.h5", dataset_type="SOUNDER", sensor="INSAT",
        processing_level="L2B", acquisition_date="", acquisition_start_time="", valid=True
    )]
    records = extractor.extract_features({}, {}, {}, meta)
    assert len(records) == 1
    assert records[0].latitude == 17.3850
