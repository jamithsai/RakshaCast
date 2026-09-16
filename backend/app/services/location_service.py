def resolve_location_display(latitude: float, longitude: float, existing_name: str = "") -> str:
    """
    Resolves geographic coordinates into a human-readable display name.
    Currently uses static matching for the prototype, with a structure 
    ready for future integration with a real reverse-geocoding provider.
    """
    # Simple bounding box / tolerance check for Hyderabad prototype coordinates
    if abs(latitude - 17.385) < 0.1 and abs(longitude - 78.4867) < 0.1:
        return "Hyderabad, Telangana"
        
    # Future reverse-geocoding logic can be injected here
    
    # Fallback
    if existing_name:
        return existing_name
    return f"Lat {latitude:.2f}, Lon {longitude:.2f}"
