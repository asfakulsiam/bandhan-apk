# Bandhan 17 Android App

Official Android Application for **Bandhan 17**.

## Features
- **In-App Web Experience**: Native WebView container with pull-to-refresh, offline retry screen, and navigation controls.
- **Authentication**: Supports Google Sign-In and OAuth workflows.
- **Modern Jetpack Compose**: Material 3 theming, Edge-to-Edge display, and system bars integration.
- **Automated CI/CD**: GitHub Actions workflow (`.github/workflows/main.yml`) for automated building, testing, signing, and artifact generation (Debug APK, Release APK, and AAB).

## Building Locally
To assemble the debug APK:
```bash
./gradlew :app:assembleDebug
```

To run unit and robolectric tests:
```bash
./gradlew :app:testDebugUnitTest
```

## CI/CD Pipeline
Every push or pull request to `main` / `master` automatically triggers the GitHub Actions workflow to:
1. Validate the official Gradle Wrapper jar against Gradle checksums.
2. Build and run unit test suites.
3. Generate signed Debug & Release APKs along with the Android App Bundle (AAB).
4. Upload build artifacts for immediate download from the GitHub Actions run summary.
