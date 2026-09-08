from fastapi import APIRouter
from app.api import endpoints
from app.api import ml_endpoints

router = APIRouter()
router.include_router(endpoints.router)
router.include_router(ml_endpoints.router)
