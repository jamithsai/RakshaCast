import math

def enrich_alert_context(pred):
    enrichment = {
        "shelterName": None,
        "shelterDistanceKm": None,
        "shelterVerification": None,
        "shelterAvailability": None,
        "radiusKm": None,
        "affectedAreaKm2": None
    }
    
    try:
        from app.api.endpoints import get_shelters, get_risk_map
        
        # 1. Shelter
        shelters = get_shelters()
        if shelters:
            nearest = sorted(shelters, key=lambda s: s.distanceKm)[0]
            enrichment["shelterName"] = nearest.name
            enrichment["shelterDistanceKm"] = str(nearest.distanceKm)
            enrichment["shelterVerification"] = "PROTOTYPE LOCATION"
            enrichment["shelterAvailability"] = "Availability not verified"
            
        # 2. Risk Zone
        zones = get_risk_map()
        for z in zones:
            if z.hazard.lower() == pred.hazardType.lower():
                # match location roughly
                if abs(z.centerLat - pred.location.latitude) < 0.1 and abs(z.centerLng - pred.location.longitude) < 0.1:
                    r = z.radiusKm
                    enrichment["radiusKm"] = str(r)
                    enrichment["affectedAreaKm2"] = str(round(math.pi * r * r, 1))
                    break
    except Exception as e:
        # Graceful degradation if mocks fail or imports fail
        pass
        
    return enrichment
