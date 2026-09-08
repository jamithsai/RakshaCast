import os
import numpy as np
import xgboost as xgb
from app.ml.model import BaselineXGBoostEngine
from app.ml.preprocessing import FeaturePreprocessor
from sklearn.model_selection import train_test_split

class TrainingPipeline:
    def __init__(self, model_dir: str):
        self.model_dir = model_dir
        if not os.path.exists(self.model_dir):
            os.makedirs(self.model_dir)
        self.preprocessor = FeaturePreprocessor()

    def create_prototype_dataset(self, num_samples: int = 1000):
        """Creates a PROTOTYPE/DEMONSTRATION training dataset if real labels don't exist yet."""
        X = np.zeros((num_samples, len(self.preprocessor.EXPECTED_FEATURES)))
        # Generate realistic ranges instead of standard normal
        X[:, 0] = np.random.uniform(0, 80, num_samples)     # rainfall_mm_hr
        X[:, 1] = np.random.uniform(10, 80, num_samples)    # precipitable_water
        X[:, 2] = np.random.uniform(-10, 5, num_samples)    # instability_li
        X[:, 3] = np.random.uniform(300, 360, num_samples)  # theta_e
        X[:, 4] = np.random.uniform(-80, 0, num_samples)    # cloud_top_temperature
        X[:, 5] = np.random.uniform(100, 1000, num_samples) # cloud_top_pressure
        X[:, 6] = np.random.uniform(15, 45, num_samples)    # surface_temperature
        X[:, 7] = np.random.randint(0, 2, num_samples)      # cloud_mask_flag
        X[:, 8] = np.random.uniform(0, 10, num_samples)     # wind_index
        X[:, 9] = np.random.uniform(16.0, 18.0, num_samples)# latitude
        X[:, 10] = np.random.uniform(77.0, 80.0, num_samples)# longitude

        # Fabricate targets roughly based on combinations of random inputs for demo
        y_ts = (X[:, 2] < -2) & (X[:, 0] > 10) # High instability (<-2) and some rain (>10)
        y_cb = (X[:, 0] > 50) & (X[:, 7] > 0)  # Very high rainfall and cloud presence
        y_ff = (X[:, 0] > 30) & (X[:, 1] > 50) # High rain and high precipitable water
        
        return X, y_ts.astype(int), y_cb.astype(int), y_ff.astype(int)

    def train_baseline(self):
        """Trains the XGBoost models on the prototype dataset and saves them."""
        X, y_ts, y_cb, y_ff = self.create_prototype_dataset()
        targets = {
            "THUNDERSTORM": y_ts,
            "CLOUDBURST": y_cb,
            "FLASH_FLOOD": y_ff
        }
        
        for hazard, y in targets.items():
            clf = xgb.XGBClassifier(
                n_estimators=100,
                max_depth=4,
                learning_rate=0.1,
                use_label_encoder=False,
                eval_metric='logloss'
            )
            clf.fit(X, y)
            
            # Save artifact
            model_path = os.path.join(self.model_dir, f"xgboost_{hazard.lower()}.json")
            clf.save_model(model_path)
            print(f"[{hazard}] Prototype model saved to {model_path}")
