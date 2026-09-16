import logging
import os
import time
from typing import Dict, List, Optional
from pydantic import BaseModel
import firebase_admin
from firebase_admin import credentials, messaging
from app.services.location_service import resolve_location_display
from app.services.enrichment_service import enrich_alert_context

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

_device_tokens = set()
_alert_cooldowns: Dict[str, dict] = {}

def _initialize_firebase():
    if not firebase_admin._apps:
        cred_path = os.getenv('GOOGLE_APPLICATION_CREDENTIALS')
        if cred_path and os.path.exists(cred_path):
            try:
                cred = credentials.Certificate(cred_path)
                firebase_admin.initialize_app(cred)
                logger.info("Firebase Admin initialized using GOOGLE_APPLICATION_CREDENTIALS")
            except Exception as e:
                logger.error(f"Failed to initialize Firebase Admin: {e}")
        else:
            logger.warning("No Firebase credentials found. Push notifications will be simulated.")

_initialize_firebase()

class FCMRegistrationRequest(BaseModel):
    token: str
    
def register_token(token: str):
    _device_tokens.add(token)
    logger.info(f"Registered device token. Total devices: {len(_device_tokens)}")

def _get_severity_ordinal(risk_level: str) -> int:
    levels = {"LOW": 1, "MODERATE": 2, "HIGH": 3, "SEVERE": 4, "EXTREME": 5}
    return levels.get(risk_level, 0)

def evaluate_and_push(predictions: List[any]):
    logger.info(f"[ALERT] evaluate_and_push invoked with {len(predictions)} predictions")
    for pred in predictions:
        hazard = pred.hazardType
        risk_level = pred.riskLevel
        prob = pred.probability
        fh = pred.forecastHour
        loc_name = pred.location.name
        
        logger.info(f"[PREDICTION] hazard={hazard} probability={prob} risk={risk_level}")
        
        if risk_level == "LOW":
            logger.info(f"[ALERT] severity rejected: {risk_level}")
            continue
            
        logger.info(f"[ALERT] severity accepted: {risk_level}")
            
        alert_key = f"{hazard}_{loc_name}_{fh}"
        
        current_severity = _get_severity_ordinal(risk_level)
        
        should_push = False
        now = time.time()
        
        if alert_key not in _alert_cooldowns:
            should_push = True
            logger.info(f"[ALERT] deduplication result: NEW ALERT")
        else:
            last_record = _alert_cooldowns[alert_key]
            last_time = last_record['last_sent_time']
            last_severity = last_record['severity_ordinal']
            
            if current_severity > last_severity:
                should_push = True
                logger.info(f"[ALERT] deduplication result: SEVERITY INCREASED ({last_severity} -> {current_severity})")
            elif (now - last_time) > 3600:
                should_push = True
                logger.info(f"[ALERT] deduplication result: COOLDOWN EXPIRED")
            else:
                logger.info(f"[ALERT] deduplication result: BLOCKED BY COOLDOWN")
                
        if should_push:
            if not _device_tokens:
                logger.info("[NOTIFICATION] No device tokens registered. Skipping push and not recording cooldown.")
                continue
                
            _alert_cooldowns[alert_key] = {
                'last_sent_time': now,
                'severity_ordinal': current_severity
            }
            
            _send_push_notification(pred)

def _send_push_notification(pred):
    resolved_location = resolve_location_display(pred.location.latitude, pred.location.longitude, pred.location.name)
    
    title = "RakshaCast Severe Weather Risk"
    body = f"{pred.probability}% probability within {pred.forecastHour} hours - {resolved_location}"
    
    # Serialize explanation factors
    import json
    factors_list = []
    if hasattr(pred, "explanationFactors") and pred.explanationFactors:
        for factor in pred.explanationFactors:
            factors_list.append({
                "factor": factor.factor,
                "status": factor.status,
                "explanation": factor.explanation,
                "importance": str(factor.importance),
                "trend": factor.trend
            })
    explanations_json = json.dumps(factors_list)
    
    logger.info(f"[NOTIFICATION PAYLOAD] explanations_count={len(factors_list)}")
    logger.info(f"[NOTIFICATION PAYLOAD] explanations={explanations_json}")
    logger.info(f"[NOTIFICATION] Attempting FCM send... {title}")
    
    if not _device_tokens:
        logger.info("[NOTIFICATION] No device tokens registered. Skipping push.")
        return
        
    # Fetch enriched data
    enriched = enrich_alert_context(pred)
    
    payload = {
        "type": "SEVERE_WEATHER_ALERT",
        "hazardType": pred.hazardType,
        "riskLevel": pred.riskLevel,
        "probability": str(pred.probability),
        "forecastHour": str(pred.forecastHour),
        "location": resolved_location,
        "title": title,
        "body": body,
        "explanations": explanations_json,
        "timestamp": str(pred.timestamp),
        "trend": str(pred.trend),
        "prototypeFlag": "true",
        "model_version": getattr(pred, 'model_version', 'xgboost-baseline-v1.0')
    }
    
    # Add optional enrichment fields to payload
    for k, v in enriched.items():
        if v is not None:
            payload[k] = v
    
    if not firebase_admin._apps:
        logger.info(f"[NOTIFICATION] Simulated FCM success... payload={payload}")
        return
        
    for token in list(_device_tokens):
        try:
            message = messaging.Message(
                data=payload,
                token=token
            )
            response = messaging.send(message)
            logger.info(f"[NOTIFICATION] FCM success: {response}")
        except Exception as e:
            logger.error(f"[NOTIFICATION] FCM failure for token {token}: {e}")

def test_notification():
    title = "RakshaCast Prototype Alert"
    body = "TEST MESSAGE: Verification of FCM Delivery via RakshaCast Test Endpoint."
    payload = {
        "type": "TEST_ALERT",
        "title": title,
        "body": body
    }
    logger.info(f"[NOTIFICATION] Attempting TEST FCM send... {title}")
    
    if not firebase_admin._apps:
        logger.info(f"[NOTIFICATION] Simulated FCM success... payload={payload}")
        return {"status": "simulated", "payload": payload, "devices": len(_device_tokens)}
        
    success_count = 0
    for token in list(_device_tokens):
        try:
            message = messaging.Message(
                data=payload,
                token=token
            )
            response = messaging.send(message)
            success_count += 1
            logger.info(f"[NOTIFICATION] TEST FCM success: {response}")
        except Exception as e:
            logger.error(f"[NOTIFICATION] TEST FCM failure for token {token}: {e}")
            
    return {"status": "sent", "devices_attempted": len(_device_tokens), "success_count": success_count}

def reset_cooldown():
    _alert_cooldowns.clear()
    logger.info("[NOTIFICATION] Alert cooldowns reset.")
