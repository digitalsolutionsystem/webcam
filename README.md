# DroidCam Pro Custom (Android to Linux Low-Latency Webcam)

This project provides a fully custom implementation of a low-latency webcam system for Android 9 (Samsung Galaxy Note 8) and Linux (OBS Studio).

## Deliverables

### 1. Android Project Structure
The full Android Studio project is located in the `/android` directory.
- `app/` - Main application module.
- `build.gradle` - Project-level configuration.
- `settings.gradle` - Module inclusion.

### 2. Core Modules (Kotlin)
- `MainActivity.kt` - Camera2 integration and UI logic.
- `ConnectCheckerRtsp.kt` - Interface for stream monitoring.
- `MjpegServer.kt` - Fallback HTTP MJPEG implementation.

### 3. Build Instructions
1. Clone the repository and navigate to the `/android` folder.
2. Open in **Android Studio Hedgehog** or later.
3. Wait for Gradle sync (Dependencies: PedroSG94's RootEncoder).
4. Connect the Galaxy Note 8 via USB.
5. Click **Run** to install the APK.

### 4. OBS Connection (Linux)
1. Launch the app on the phone and click **Start Stream**.
2. Note the RTSP URL (e.g., `rtsp://192.168.1.50:8554/live`).
3. In OBS, add a **Media Source**.
4. Uncheck **Local File**.
5. Input the **RTSP URL** in "Input".
6. In "Input Format", type `rtsp`.
7. Click OK.

### 5. Network Troubleshooting
- **Firewall**: Ensure port `8554` is open on your Linux machine if you are streaming over WiFi.
- **USB Tethering**: Recommended for <100ms latency.
- **Latency**: If latency is high, reduce resolution to 720p or decrease bitrate in `MainActivity.kt`.

### 6. Architecture
- **Camera Layer**: Camera2 API for high-frame-rate capture.
- **Encoder Layer**: MediaCodec H.264 (Hardware) for efficient compression.
- **Stream Layer**: RTSP (main) and MJPEG (fallback) servers running on-device.
