from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    RAKSHA_DATA_DIR: str = "data/"
    
    # Region of Interest (Default is around Hyderabad)
    ROI_MIN_LAT: float = 16.0
    ROI_MAX_LAT: float = 18.5
    ROI_MIN_LON: float = 77.0
    ROI_MAX_LON: float = 80.0
    
    class Config:
        env_file = ".env"

settings = Settings()
