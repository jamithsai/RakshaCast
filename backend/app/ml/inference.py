import os
from typing import List, Dict
from app.schemas.data_models import WeatherFeatureRecord
from app.schemas.ml_models import RiskPrediction
from app.schemas.api_models import Location
from app.ml.model import BaselineXGBoostEngine, classify_risk_level
from app.ml.preprocessing import FeaturePreprocessor
from app.ml.explanations import ExplanationEngine

class InferenceService:
    _instance = None

    def __init__(self, model_dir: str = "artifacts/"):
        self.preprocessor = FeaturePreprocessor()
        self.engine = BaselineXGBoostEngine(self.preprocessor.EXPECTED_FEATURES)
        self.engine.load(model_dir)
        self.explainer = ExplanationEngine(self.preprocessor.EXPECTED_FEATURES)

    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    def predict(self, records: List[WeatherFeatureRecord], hazard: str, forecast_hour: int) -> List[RiskPrediction]:
        if not records:
            return []
            
        X = self.preprocessor.transform_batch(records)
        probs = self.engine.predict_probabilities(X, hazard)
        importances = self.engine.get_feature_importances(hazard)

        predictions = []
        for i, record in enumerate(records):
            prob_percent = int(probs[i] * 100)
            risk_level = classify_risk_level(probs[i])
            
            # Re-map record array back to dict for the explainer
            features_dict = {f: X[i][j] for j, f in enumerate(self.preprocessor.EXPECTED_FEATURES)}
            explanations = self.explainer.generate_explanations(features_dict, importances)
            
            predictions.append(RiskPrediction(
                hazardType=hazard,
                probability=prob_percent,
                riskLevel=risk_level,
                forecastHour=forecast_hour,
                location=Location(name=f"Lat {record.latitude:.2f}, Lon {record.longitude:.2f}", latitude=record.latitude, longitude=record.longitude),
                timestamp=record.timestamp,
                trend="STABLE", # Needs historical records to calculate actual trend
                explanationFactors=explanations,
                prototypeFlag=True,
                model_version="xgboost-baseline-v1.0"
            ))
            
        return predictions
