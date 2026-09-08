from pydantic import BaseModel
from typing import List, Optional

class Location(BaseModel):
    name: str
    latitude: float
    longitude: float

class RiskFactor(BaseModel):
    factor: str
    status: str
    explanation: str
    importance: float
    trend: str

class ForecastPoint(BaseModel):
    timeLabel: str
    riskLevel: str

class WeatherIndicators(BaseModel):
    rainfallMmHr: float
    temperatureC: float
    moistureLevel: str
    instability: str
    cloudCondition: str

class Alert(BaseModel):
    id: str
    hazard: str
    severity: str
    location: Location
    probability: int
    expectedTimeframe: str
    status: str
    timestamp: str
    message: str
    explanationFactors: List[RiskFactor]
    recommendedActions: List[str]
    isRead: bool

class RiskResponse(BaseModel):
    overallRisk: str
    primaryHazard: str
    predictionWindow: str
    probability: int
    explanationFactors: List[RiskFactor]
    indicators: WeatherIndicators
    forecastTimeline: List[ForecastPoint]
    activeAlerts: List[Alert]

class RiskZone(BaseModel):
    id: str
    name: str
    hazard: str
    riskLevel: str
    probability: int
    expectedWindow: str
    explanationFactors: List[RiskFactor]
    centerLat: float
    centerLng: float
    radiusKm: float
    forecastHour: int

class Shelter(BaseModel):
    id: str
    name: str
    distanceKm: float
    capacity: int
    status: str
    availabilityText: str

class EmergencyContact(BaseModel):
    name: str
    description: str
    number: str

class SafetyGuidance(BaseModel):
    hazardType: str
    title: str
    instructions: List[str]
