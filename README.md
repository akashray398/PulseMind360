# ⚡ PulseMind 360 - Intelligent System Companion

**PulseMind 360** is a modern, real-time Android system monitoring and contextual automation suite built with **Jetpack Compose**, **Kotlin Coroutines**, **Hilt**, **Room**, **Retrofit**, and **WorkManager**.

Designed with a futuristic glassmorphic UI theme, PulseMind 360 monitors real-time battery dynamics, thermal curves, network telemetry, UI frame rendering performance, and background tasks—all while keeping your data private and offline-first.

---

## ✨ Features Overview

### 📊 1. Real-Time System Dashboard
- **🧠 PulseMind Sentinel (Predictive Anomaly Intelligence):** Self-learning statistical AI engine ([SentinelEngine.kt](file:///D:/AndroidStudioProjects/PulseMind360/app/src/main/java/com/dev/myapplication/domain/SentinelEngine.kt)) that computes rolling 7-day Z-Score baselines to detect abnormal battery drain velocities, persistent thermal rises, and traffic spikes with confidence ratings & root cause evidence.
- **🛡️ Android KeyStore AES-256 Encrypted Vault:** Zero-trust security vault using hardware-backed `AndroidKeyStore` AES/GCM/NoPadding encryption for local biometric credentials & token protection.
- **📶 Network Bandwidth & Traffic Inspector:** Real-time upload/download speed gauge (KB/s & MB/s) and session data consumption counter via native `android.net.TrafficStats`.
- **⚡ RAM & Cache Memory Optimizer:** Inspects `ActivityManager.MemoryInfo` to show real-time RAM allocation (GB Used vs Total) and features a "Trim RAM" action clearing system caches.
- **📶 Offline Sync Queue with Exponential Backoff:** WorkManager background queue configured with `NetworkType.CONNECTED` constraints and `BackoffPolicy.EXPONENTIAL` for resilient retries.
- **📲 Home Screen App Widget:** System Health Home Screen Widget displaying System Health Score (`0–100%`), Battery Gauge %, and Network Connectivity State directly on the launcher.
- **🌙 Ambient Desk Monitor Mode:** Ultra-dark OLED nightstand display with `FLAG_KEEP_SCREEN_ON`, animated digital clock, battery gauge, and live thermal curve for desk/dock charging.
- **Contextual System Health Score:** Dynamic `0–100%` health score evaluating battery, thermal state, anomalies, network stability, and frame rendering.
- **🤖 AI Battery Depletion & Thermal Prediction:** Real-time discharge velocity (%/hr), predictive time-to-empty, and thermal slope forecasting.
- **⚡ 80% Smart Charge Lifespan Guard & Overheat Alarm:** Automated notification triggers when battery reaches 80% while charging or when temperature exceeds 40°C to preserve long-term battery cell health.
- **📄 Native PDF Audit Report Generator:** Instant 1-tap generation of a formatted 2D PDF Diagnostic Report using `android.graphics.pdf.PdfDocument` and Android `FileProvider` with native Share Intent integration.
- **Live Battery & Thermal Curves:** Custom Compose Canvas charting showing level (%) and temperature (°C) trends over time with direct data point value labels and gradient fills.
- **📶 Unified Network & Bandwidth Hub:** Integrated live upload/download speed gauges (KB/s & MB/s via `TrafficStats`), metered Wi-Fi/Cellular connectivity status, link speeds, and public IP & ISP location context via Retrofit API in a single non-duplicated card.
- **Smart AI System Advisories:** Contextual health recommendations based on live system diagnostics.
- **Developer Telemetry (Collapsible):** Live UI frame render performance (FPS / frame time) and WorkManager sync worker controls.

### ⚙️ 2. Contextual Automation Engine
- **⚡ 1-Click Smart Automation Recipes:** Quick-add pre-configured rules (`Low Battery Saver`, `Wi-Fi Sync Guard`, `Charging Alert`, `Offline Event Log`) with 1 tap.
- **📊 Live Execution Counter & Status:** Real-time tracking of rule execution counts (`⚡ Triggered X times`), active status indicators, and live evaluation feedback.
- Configure custom **IF-THEN rules** (e.g., *IF Battery <= 20% THEN Trigger Notification* or *IF Metered Wi-Fi THEN Throttle Background Sync*).
- Evaluates active rules reactively whenever battery or network state changes.

### 📜 3. Activity Timeline & History
- Real-time logging of system monitor events, anomaly detections, and rule triggers.
- Persisted locally using an **offline-first Room Database**.

### 🎯 4. Productivity Focus Mode & Habit Analytics
- Dedicated focus session timer with persistence and completed session history.
- **🔥 Focus Habit Streak & Analytics Card:** Tracks total focus minutes, completed session counts, and consecutive daily productivity streaks (`🔥 4 Day Streak`).
- **📊 7-Day Weekly Focus Bar Chart:** Custom Compose Canvas bar chart mapping focus minutes logged per day over the past week (Mon–Sun).
- **🎵 Ambient Focus Audio Generator:** Built-in PCM acoustic generator (`Rain White Noise` & `Cyber Binaural Pulse`) powered by `AudioTrack`—100% offline with zero MP3 assets.
- **🎯 Custom Duration Slider:** Interactive slider setting custom focus durations from 5m up to 120m.

### 🔔 5. System Notification Shade Controls
- **📱 Branded System Notification Cards:** System shade notifications formatted with the app's real branded icon (`setLargeIcon`), expandable `BigTextStyle` card animations, high-priority sound/vibration alerts, and interactive action buttons (`[📄 Export PDF Report]`) powered by `BroadcastReceiver` and `PendingIntent`.

### 🔒 6. Settings & Biometric Security Vault
- **🔒 Persistent Real-Time Biometric Vault:** Fingerprint/PIN authentication persisted via DataStore; enforces real-time biometric verification before purging data or running KeyStore tests.
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
