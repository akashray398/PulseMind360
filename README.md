# ⚡ PulseMind 360 - Intelligent System Companion

**PulseMind 360** is a modern, real-time Android system monitoring and contextual automation suite built with **Jetpack Compose**, **Kotlin Coroutines**, **Hilt**, **Room**, **Retrofit**, and **WorkManager**.

Designed with a futuristic glassmorphic UI theme, PulseMind 360 monitors real-time battery dynamics, thermal curves, network telemetry, UI frame rendering performance, and background tasks—all while keeping your data private and offline-first.

---

## ✨ Features Overview

### 📊 1. Real-Time System Dashboard
- **Contextual System Health Score:** Dynamic `0–100%` health score evaluating battery, thermal state, anomalies, network stability, and frame rendering.
- **Live Battery & Thermal Curves:** Custom Compose Canvas charting showing level (%) and temperature (°C) trends over time with direct data point value labels and gradient fills.
- **Unified Network & Geo Diagnostics:** Monitors Wi-Fi/Cellular connectivity, metered state, downstream link speeds, and queries public IP & ISP context via Retrofit API.
- **Smart AI System Advisories:** Contextual health recommendations based on live system diagnostics.
- **Developer Telemetry (Collapsible):** Live UI frame render performance (FPS / frame time) and WorkManager sync worker controls.

### ⚙️ 2. Contextual Automation Engine
- Configure custom **IF-THEN rules** (e.g., *IF Battery <= 20% THEN Trigger Notification* or *IF Metered Wi-Fi THEN Throttle Background Sync*).
- Evaluates active rules reactively whenever battery or network state changes.

### 📜 3. Activity Timeline & History
- Real-time logging of system monitor events, anomaly detections, and rule triggers.
- Persisted locally using an **offline-first Room Database**.

### 🎯 4. Productivity Focus Mode Timer
- Dedicated focus session timer with persistence and completed session history.

### 🔒 5. Settings & Biometric Security Vault
- **Biometric Authentication:** Secure local vault protected by Android Credential / Biometric Hardware (Fingerprint / PIN).
- **Theme Customization:** Toggle between Dark, Light, and System Theme modes.
- **Custom Alert Thresholds:** Low battery notification thresholds and background sync intervals.

---

## 🛠️ Tech Stack & Architecture

- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose (Material 3, Custom Canvas, GraphicsLayer, Animations)
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Dependency Injection:** Dagger Hilt
- **Async & Reactive Flow:** Kotlin Coroutines & `StateFlow` / `collectAsStateWithLifecycle`
- **Local Database:** Room Database
- **Background Tasks:** WorkManager
- **Networking:** Retrofit + OkHttp
- **Security:** Android Biometric API (`androidx.biometric`)
- **Key-Value Storage:** Preferences DataStore

---

## 📁 Project Structure

```text
com.dev.myapplication/
├── data/
│   ├── db/          # Room Entities, DAOs, and Database
│   ├── monitor/     # Real-time Battery & Network state observers
│   ├── remote/      # Retrofit API interface & response models
│   └── worker/      # WorkManager background sync workers
├── di/              # Hilt Modules (AppModule)
├── domain/          # AnomalyDetector, AutomationEngine, SmartInsightsEngine
├── ui/
│   ├── automation/  # Automation Rules Screen & ViewModel
│   ├── components/  # GlassCard, AnimatedBatteryGauge, BatteryTrendVisualizer
│   ├── dashboard/   # Dashboard Screen & ViewModel
│   ├── focustimer/  # Focus Timer Screen & ViewModel
│   ├── settings/    # Settings Screen & ViewModel
│   ├── splash/      # Animated Splash Screen
│   ├── theme/       # Color, Theme, Typography definitions
│   └── timeline/    # Activity History Screen & ViewModel
└── util/            # BiometricHelper, NotificationHelper
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1) or newer
- **JDK:** Java 17
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)

### Build & Run
1. Clone the repository or open the project folder in Android Studio.
2. Sync the project with Gradle files (`gradle_sync`).
3. Connect an Android device or emulator running SDK 26+.
4. Run the `:app` configuration.

```bash
# Build Debug APK via command line:
./gradlew assembleDebug
```

---

## 📄 License
This project is developed under open architectural guidelines for modern Android applications.
