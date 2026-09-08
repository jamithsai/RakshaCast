import numpy as np
from typing import Tuple, Dict

def get_roi_mask(lat_array: np.ndarray, lon_array: np.ndarray, min_lat: float, max_lat: float, min_lon: float, max_lon: float) -> np.ndarray:
    """
    Generate a boolean mask for the region of interest.
    """
    lat_mask = (lat_array >= min_lat) & (lat_array <= max_lat)
    lon_mask = (lon_array >= min_lon) & (lon_array <= max_lon)
    return lat_mask & lon_mask

def apply_roi(data: np.ndarray, mask: np.ndarray) -> np.ndarray:
    """
    Apply ROI mask to a 2D or 3D array.
    """
    if len(data.shape) == 3 and data.shape[0] == 1:
        data_2d = data[0]
        if data_2d.shape == mask.shape:
            # Flatten or extract points within ROI
            return data_2d[mask]
    elif data.shape == mask.shape:
        return data[mask]
    
    return np.array([])
