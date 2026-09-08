from typing import List, Dict
from app.schemas.api_models import RiskFactor

class ExplanationEngine:
    """Translates numeric features and importances into human-readable Android RiskFactors."""

    def __init__(self, feature_names: List[str]):
        self.feature_names = feature_names

        # Mapping for human-readable outputs
        self.factor_text_map = {
            "precipitable_water": {
                "name": "Atmospheric Moisture",
                "explanation": "High available moisture can support stronger convective development."
            },
            "instability_li": {
                "name": "Atmospheric Instability",
                "explanation": "Atmospheric instability increases the potential for rapidly developing convection."
            },
            "rainfall_mm_hr": {
                "name": "Rainfall Intensity",
                "explanation": "Rainfall intensity is elevated over recent observations."
            },
            "cloud_top_temperature": {
                "name": "Cloud Development",
                "explanation": "Cloud conditions indicate active convective development."
            }
        }

    def generate_explanations(self, features_dict: Dict[str, float], importances: Dict[str, float]) -> List[RiskFactor]:
        # Filter features that have a mapped explanation and sort by model importance
        ranked_features = []
        for feature, imp in importances.items():
            if feature in self.factor_text_map and imp > 0.05: # Threshold for importance
                ranked_features.append((feature, imp))
        
        ranked_features.sort(key=lambda x: x[1], reverse=True)
        
        explanations = []
        for feature, imp in ranked_features:
            val = features_dict.get(feature, 0.0)
            status = "ELEVATED" if val > 0 else "NORMAL"
            trend = "STABLE" # Derived from temporal sequence if available
            
            explanations.append(RiskFactor(
                factor=self.factor_text_map[feature]["name"],
                status=status,
                explanation=self.factor_text_map[feature]["explanation"],
                importance=min(imp * 5.0, 1.0), # Scaling for UI
                trend=trend
            ))
            
        return explanations
