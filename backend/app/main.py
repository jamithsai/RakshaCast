from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import router as api_router

app = FastAPI(
    title="RakshaCast API",
    description="Backend API for the RakshaCast early warning system prototype.",
    version="1.0.0"
)

# CORS for local development (allow Android emulator/local requests)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router, prefix="/api/v1")

@app.get("/api/v1/health")
def health_check():
    return {
        "status": "ok",
        "service": "RakshaCast API"
    }
