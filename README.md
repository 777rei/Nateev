# Nateev Camera

Nateev Camera is a professional-grade, offline-first photography application for Android. Built with a focus on privacy, performance, and granular control, it empowers photographers to capture high-quality images without relying on cloud services or telemetry.

## Key Features

- **Pro Manual Controls:** Full control over ISO, shutter speed, and manual focus distance for precise artistic intent.
- **On-Device Processing:** Real-time LUT (Look-Up Table) grading and AI-driven optimizations, all running locally on your device for zero-latency preview.
- **Advanced Viewfinder:** Real-time histogram, focus peaking, zebra patterns, and a virtual horizon level.
- **Privacy First:** Absolutely zero cloud dependencies, no tracking, and no external data telemetry. Your photos stay on your device.
- **Cinematic Workflow:** Support for high-quality video encoding (HEVC/AVC/AV1) with customizable bitrates.

## Building the Project

Nateev Camera is built using Kotlin, Jetpack Compose, and the CameraX library.

### Prerequisites
- Android Studio (latest stable version recommended)
- Android SDK (compiled with SDK version 36+)
- JDK 11 or higher

### Build Instructions
1. Clone this repository.
2. Open the project in Android Studio.
3. The project uses Gradle (Kotlin DSL). Sync your project dependencies as prompted.
4. Build the application using the standard Gradle build process:
   ```bash
   ./gradlew :app:assembleDebug
   ```

## Contributing

Contributions are welcome! Please fork the repository, make your improvements, and submit a pull request. Ensure all new code adheres to the existing architecture (MVVM) and follows the established Kotlin coding conventions.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## About the Project

Nateev Camera is a professional-grade, offline-first photography application developed by 777rei (reialwaysfreemium).
