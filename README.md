# SpidyEngine 🕷️

SpidyEngine is a native Android utility designed to help users play classic Android games (like *The Amazing Spider-Man 2*) on modern Android devices. It automates the tedious process of installing APKs and correctly placing OBB data files.

## Features
- **Native Android App**: Built with Kotlin for performance and stability.
- **Fully Offline**: No internet connection required after installation.
- **Automated OBB Handling**: Automatically detects the package name from the OBB file and moves it to the correct `/Android/obb/` directory.
- **Easy Installation**: Simplifies the setup of older games that require manual data placement.
- **Modern UI**: Clean, Red & Black themed interface.

## How to Use
1. **Download the APK**: Get the latest `SpidyEngine.apk` from the [Actions](https://github.com/sheisdilshana2424-ctrl/SpidyEngine/actions) tab or the provided download link.
2. **Install & Open**: Install the app on your phone and grant the necessary storage and installation permissions.
3. **Add Game**:
   - Click the **"Add Game"** button.
   - Select your game's **APK file**.
   - Select your game's **OBB file** (usually named `main.xxxx.package.name.obb`).
4. **Play**: SpidyEngine will move the OBB to the correct folder and trigger the APK installation. Once done, you can launch your game!

## Permissions Required
- **Storage Access**: To read game files and move OBB data.
- **Install Unknown Apps**: To trigger the installation of your game APKs.

## Technical Details
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Build System**: Gradle with GitHub Actions CI/CD

---
*Built with ❤️ for classic game enthusiasts.*
