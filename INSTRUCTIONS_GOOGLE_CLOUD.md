# Build ShareEase APK on Google Cloud Shell

This guide explains how to build the ShareEase Android app using Google Cloud Shell.

## Prerequisites

- A Google Cloud account
- Access to [Google Cloud Shell](https://shell.cloud.google.com)

## Step-by-Step Instructions

### 1. Open Google Cloud Shell

1. Go to [Google Cloud Shell](https://shell.cloud.google.com)
2. Sign in with your Google account
3. Wait for the terminal to load

### 2. Clone the Repository

```bash
git clone https://github.com/essolaymani/ShareEase.git
cd ShareEase
```

### 3. Install Android SDK

Run these commands to download and set up the Android SDK:

```bash
# Create directory for Android SDK
mkdir -p android-sdk/cmdline-tools

# Download command line tools
curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

# Extract and organize
unzip -q cmdline-tools.zip
mv cmdline-tools android-sdk/cmdline-tools/latest
rm cmdline-tools.zip

# Set environment variables
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

### 4. Accept Licenses and Install SDK Components

```bash
# Accept all SDK licenses
yes | sdkmanager --licenses

# Install required SDK components
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

### 5. Configure the Project

```bash
# Create local.properties with SDK path
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 6. Build the Debug APK

```bash
# Clean and build
./gradlew clean assembleDebug

# Or build without cleaning
./gradlew assembleDebug
```

### 7. Locate the APK

The built APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 8. Download the APK to Your Computer

In Cloud Shell, click the **three dots menu** (⋮) in the top right corner, then select **Download file** and enter:
```
app/build/outputs/apk/debug/app-debug.apk
```

Or use the Cloud Shell editor to download:
1. Click **Open Editor** (pencil icon)
2. Navigate to `app/build/outputs/apk/debug/`
3. Right-click on `app-debug.apk` and download

## Alternative: Using Android Studio in Cloud Shell

Google Cloud Shell also supports Android Studio:

```bash
# Install Android Studio
sudo apt update
sudo apt install android-studio -y

# Launch Android Studio
studio.sh
```

## Troubleshooting

### Java Version Issues

If you encounter Java compatibility issues:

```bash
# Check Java version
java -version

# Install Java 17 if needed
sudo apt install openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### SDK Not Found

If the SDK is not found during build:

```bash
# Verify SDK installation
ls -la $ANDROID_HOME

# Re-export variables
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

### Build Fails with AAPT2

If AAPT2 fails, try clearing the Gradle cache:

```bash
rm -rf ~/.gradle/caches/
./gradlew clean assembleDebug
```

## Quick Command Summary

Copy and paste this entire block into Cloud Shell:

```bash
git clone https://github.com/essolaymani/ShareEase.git
cd ShareEase

mkdir -p android-sdk/cmdline-tools
curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q cmdline-tools.zip
mv cmdline-tools android-sdk/cmdline-tools/latest
rm cmdline-tools.zip

export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

yes | sdkmanager --licenses
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

echo "sdk.dir=$ANDROID_HOME" > local.properties

./gradlew assembleDebug
```

## Install APK on Your Device

After downloading the APK:

1. Enable **Install from unknown sources** in your Android settings
2. Transfer the APK to your device
3. Open the APK file to install
4. Launch ShareEase and start sharing!

## Build Release APK

For a release build (signed):

```bash
./gradlew assembleRelease
```

You'll need to configure signing in `app/build.gradle` for release builds.

---

**Repository:** https://github.com/essolaymani/ShareEase
