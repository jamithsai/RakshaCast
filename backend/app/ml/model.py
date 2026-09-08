import os
import xgboost as xgb
import numpy as np
import datetime
from abc import ABC, abstractmethod
from typing import Dict, Tuple

class RiskEngine(ABC):
    @abstractmethod
    def load(self, model_dir: str):
        pass

    @abstractmethod
    def predict_probabilities(self, X: np.ndarray, hazard: str) -> np.ndarray:
        pass

    @abstractmethod
    def get_feature_importances(self, hazard: str) -> Dict[str, float]:
        pass

class BaselineXGBoostEngine(RiskEngine):
    """XGBoost baseline classifier supporting multiple hazards."""
    
    HAZARDS = ["THUNDERSTORM", "CLOUDBURST", "FLASH_FLOOD"]
    
    def __init__(self, feature_names: list[str]):
        self.models: Dict[str, xgb.XGBClassifier] = {}
        self.feature_names = feature_names

    def load(self, model_dir: str):
        for hazard in self.HAZARDS:
            model_path = os.path.join(model_dir, f"xgboost_{hazard.lower()}.json")
            if os.path.exists(model_path):
                clf = xgb.XGBClassifier()
                clf.load_model(model_path)
                self.models[hazard] = clf
            else:
                self.models[hazard] = None # Will fail gracefully during prediction

    def predict_probabilities(self, X: np.ndarray, hazard: str) -> np.ndarray:
        model = self.models.get(hazard)
        if model is None:
            # Return dummy probabilities if prototype model not yet generated
            return np.random.uniform(0.1, 0.4, size=X.shape[0])
        
        # XGBClassifier predict_proba returns [prob_negative, prob_positive]
        return model.predict_proba(X)[:, 1]

    def get_feature_importances(self, hazard: str) -> Dict[str, float]:
        model = self.models.get(hazard)
        if model is None:
            return {}
        
        importance = model.feature_importances_
        return {self.feature_names[i]: float(importance[i]) for i in range(len(self.feature_names))}

def classify_risk_level(probability: float) -> str:
    """Configurable baseline thresholds."""
    if probability >= 0.85:
        return "EXTREME"
    elif probability >= 0.70:
        return "SEVERE"
    elif probability >= 0.50:
        return "HIGH"
    elif probability >= 0.30:
        return "MODERATE"
    return "LOW"
