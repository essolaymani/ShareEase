# ShareEase - WiFi File & Text Sharing App

A modern Android application for sharing files, multiple files, and text over WiFi between devices. Features QR code generation for quick sharing with a sleek dark mode UI.

## Features

- **File Sharing**: Share multiple files over WiFi
- **Text Sharing**: Share text messages instantly
- **QR Code**: Generate QR codes for quick connection sharing
- **QR Scanning**: Scan QR codes to connect (requires camera permission)
- **Dark Mode**: Beautiful dark theme with greenish accent colors
- **Linear Style**: Modern linear design with gradient cards

## Requirements

- Android Studio Hedgehog or later
- Android SDK 34
- Gradle 8.4+
- Java 17+

## Building

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the `ShareEase` folder
4. Wait for Gradle sync to complete
5. Build the project: `Build > Make Project` or `Ctrl+F9`

Or from terminal:

```bash
cd ShareEase
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
ShareEase/
├── app/
│   └── src/main/
│       ├── java/com/sharease/app/
│       │   ├── ShareEaseApp.kt          # Application class
│       │   ├── data/model/             # Data models
│       │   ├── network/                # Network utilities
│       │   └── ui/                     # UI components
│       │       ├── MainActivity.kt
│       │       ├── screens/            # Fragments
│       │       └── components/         # Adapters
│       └── res/
│           ├── layout/                 # XML layouts
│           ├── drawable/               # Vector icons
│           └── values/                 # Colors, strings, themes
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **Language**: Kotlin
- **UI**: Material Design 3 with dark theme
- **Networking**: Socket-based file transfer
- **QR Generation**: ZXing library

## Permissions

- `INTERNET` - For network communication
- `ACCESS_WIFI_STATE` - To check WiFi status
- `CHANGE_WIFI_STATE` - To create WiFi hotspot (if needed)
- `ACCESS_NETWORK_STATE` - To check network status
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_*` - To access files
- `CAMERA` - For QR code scanning

## Usage

1. **Send Mode**: Select files or enter text, then share
2. **Receive Mode**: Start server and wait for incoming files
3. **QR Code**: Generate QR with connection info for quick sharing

## Color Scheme

- Primary: #4CAF50 (Green)
- Primary Dark: #388E3C
- Accent: #00E676
- Background: #121212
- Surface: #1E1E1E
- Card: #2D2D2D
