import sys
sys.path.insert(0, '.')
from app.ml.inference import InferenceService
from app.schemas.data_models import WeatherFeatureRecord
import datetime

service = InferenceService()
record = WeatherFeatureRecord(
        timestamp=datetime.datetime.now(datetime.timezone.utc).isoformat(),
        latitude=17.3850,
        longitude=78.4867,
        rainfall_mm_hr=25.4,
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
print(service.predict([record], 'THUNDERSTORM', 2))
