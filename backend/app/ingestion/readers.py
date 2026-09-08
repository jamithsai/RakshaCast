import h5py
import numpy as np
import pandas as pd
from typing import Dict, Any, Tuple, Optional
from app.schemas.data_models import ObservationMetadata

class HDF5Reader:
    """Base class for reading MOSDAC INSAT-3DR HDF5 files."""
    
    def __init__(self, filepath: str):
        self.filepath = filepath
        self.metadata = self._extract_metadata()
        
    def _extract_metadata(self) -> ObservationMetadata:
        try:
            with h5py.File(self.filepath, 'r') as f:
                acq_date = f.attrs.get("Acquisition_Date", b"").decode("utf-8") if "Acquisition_Date" in f.attrs else ""
                acq_start = f.attrs.get("Acquisition_Start_Time", b"").decode("utf-8") if "Acquisition_Start_Time" in f.attrs else ""
                dataset_type = self._determine_type(self.filepath)
                return ObservationMetadata(
                    filename=self.filepath.split("/")[-1].split("\\")[-1],
                    dataset_type=dataset_type,
                    sensor="INSAT-3DR",
                    processing_level="L2B",
                    acquisition_date=acq_date,
                    acquisition_start_time=acq_start,
                    valid=True
                )
        except Exception as e:
            return ObservationMetadata(
                filename=self.filepath.split("/")[-1].split("\\")[-1],
                dataset_type="UNKNOWN",
                sensor="UNKNOWN",
                processing_level="UNKNOWN",
                acquisition_date="",
                acquisition_start_time="",
                valid=False,
                error_message=str(e)
            )

    def _determine_type(self, filepath: str) -> str:
        if "3RSND" in filepath:
            return "SOUNDER"
        elif "IMC" in filepath:
            return "RAINFALL"
        elif "CMK" in filepath:
            return "CLOUD_MASK"
        return "UNKNOWN"

    def read_dataset(self, var_name: str) -> Optional[np.ndarray]:
        try:
            with h5py.File(self.filepath, 'r') as f:
                if var_name in f:
                    data = f[var_name][()]
                    # Replace fill values with NaN. Assuming commonly used -999.0 or similar.
                    data = np.where(data == -999.0, np.nan, data)
                    return data
                return None
        except Exception:
            return None

    def read_coordinates(self) -> Tuple[Optional[np.ndarray], Optional[np.ndarray]]:
        lat = self.read_dataset("Latitude")
        lon = self.read_dataset("Longitude")
        return lat, lon

class SounderReader(HDF5Reader):
    """Specific reader for 3RSND_L2B_SA1"""
    SUPPORTED_VARS = ["totH2O", "LI", "theta-e", "CTT", "CTP", "TSurfPhy", "WI"]
    
    def get_features(self) -> Dict[str, np.ndarray]:
        features = {}
        for var in self.SUPPORTED_VARS:
            data = self.read_dataset(var)
            if data is not None:
                features[var] = data
        return features

class RainfallReader(HDF5Reader):
    """Specific reader for 3RIMG_L2B_IMC"""
    def get_rainfall(self) -> Optional[np.ndarray]:
        return self.read_dataset("IMC")

class CloudMaskReader(HDF5Reader):
    """Specific reader for 3RIMG_L2B_CMK"""
    def get_cloud_mask(self) -> Optional[np.ndarray]:
        return self.read_dataset("CMK")
