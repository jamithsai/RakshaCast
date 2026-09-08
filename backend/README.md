# RakshaCast Backend

This is the FastAPI backend prototype for RakshaCast. It serves the Mock ML and geospatial data to the Android application.

## Local Development Setup

1. **Install Python** (3.9+ recommended).
2. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```
3. **Start the server**:
   ```bash
   uvicorn app.main:app --reload
   ```

## API Documentation
Once running, you can access the interactive OpenAPI/Swagger documentation at:
- http://127.0.0.1:8000/docs

## Android Local Connection
When running the Android App on the official Android Studio Emulator, the host machine is accessible at `10.0.2.2`. 
Configure your Android Retrofit base URL to:
`http://10.0.2.2:8000/`

*(Note: For physical devices, you must use your machine's local IP address, e.g., 192.168.x.x)*
