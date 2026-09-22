<div align="center">

# 🫧 FocusBubble Frontend

### Android Productivity and Digital Wellbeing Application

### Kotlin + Jetpack Compose Focus Session Application with App Blocking

<br/>

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-34A853?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-Local%20Database-6A1B9A?style=for-the-badge)](https://developer.android.com/training/data-storage/room)
[![Retrofit](https://img.shields.io/badge/Retrofit-REST%20API-2E7D32?style=for-the-badge)](https://square.github.io/retrofit/)
[![Hilt](https://img.shields.io/badge/Hilt-Dependency%20Injection-FF6F00?style=for-the-badge)](https://developer.android.com/training/dependency-injection/hilt-android)
[![Firebase](https://img.shields.io/badge/Firebase-Authentication-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)

</div>

---

# 📑 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Permissions](#permissions)
- [Backend Integration](#backend-integration)
- [Getting Started](#getting-started)
- [Build and Run](#build-and-run)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Demo](#demo)
- [Author](#author)

---

<a id="overview"></a>

# 🔍 Overview

FocusBubble is an Android productivity and digital-wellbeing application that helps users focus by temporarily blocking selected distracting applications.

Users can select applications such as:

- WhatsApp
- YouTube
- Instagram
- Amazon
- Other installed applications

During an active focus session, FocusBubble monitors the current foreground application using Android `AccessibilityService`. When a selected application is detected, FocusBubble displays a full-screen blocking overlay and prevents normal interaction with the blocked app.

The application includes:

- Timed focus sessions.
- Blocked-app selection.
- Room local database persistence.
- Full-screen blocking overlay.
- Accessibility-based foreground detection.
- Resume while already inside a blocked app.
- Leave to Home.
- Emergency Use.
- Floating session timer.
- Focus statistics.
- Backend synchronization.
- User-specific blocked-app data.

---

<a id="features"></a>

# ✨ Features

## Focus Sessions

- Start timed focus sessions.
- Pause and resume sessions.
- Store remaining time.
- Track completed focus time.
- Display session completion information.

## Application Blocking

- Select installed applications.
- Detect selected packages in the foreground.
- Display a full-screen overlay.
- Move the user to the Android Home screen.
- Restore the overlay if it disappears while the blocked app remains active.

## Resume Behavior

FocusBubble checks the foreground app immediately when a paused session is resumed.

```text
WhatsApp chat
    ↓
Emergency Use
    ↓
Continue chatting
    ↓
Click to Resume
    ↓
Session becomes active
    ↓
Foreground package is checked
    ↓
Blocking overlay appears immediately
```

## Leave to Home

Leave to Home:

- Removes the overlay.
- Keeps the session active.
- Opens the Android Home screen.
- Does not pause the session.
- Does not reopen the blocked app.
- Temporarily suppresses stale accessibility events.

## Emergency Use

Emergency Use:

- Removes the overlay.
- Pauses the focus session.
- Reopens the previously blocked app.
- Allows the user to use the app.
- Requires Resume to continue blocking.

---

<a id="architecture"></a>

# 🏗️ Architecture

```text
┌──────────────────────────────────────────────┐
│              Android Application             │
│                                              │
│  Jetpack Compose UI                          │
│       │                                      │
│       ├── ViewModels                         │
│       ├── Room Database                      │
│       ├── Retrofit / OkHttp                  │
│       ├── SessionStateManager                │
│       ├── BlockerService                     │
│       ├── FloatingTimerService               │
│       ├── AccessibilityService               │
│       └── BlockOverlayService                │
└──────────────────────┬───────────────────────┘
                       │
                       │ REST API
                       ▼
┌──────────────────────────────────────────────┐
│               FastAPI Backend                │
│                                              │
│  Authentication                               │
│  User Management                              │
│  Sessions                                     │
│  Schedules                                    │
│  Block Synchronization                        │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
              Hosted Relational Database
```

---

<a id="technology-stack"></a>

# 🛠️ Technology Stack

| Category | Technology | Purpose |
|---|---|---|
| Language | Kotlin | Android application and services |
| UI | Jetpack Compose | Declarative Android UI |
| UI Components | Material 3 | Buttons, cards, dialogs, and sheets |
| Local Database | Room / SQLite | Local persistence |
| Networking | Retrofit | REST API communication |
| HTTP Client | OkHttp | HTTP requests and logs |
| Dependency Injection | Hilt | Dependency management |
| Authentication | Firebase/Google Sign-In | User authentication |
| Blocking | AccessibilityService | Foreground-app detection |
| Overlay | WindowManager | Full-screen overlay |
| Build System | Gradle | APK compilation |
| Version Control | Git/GitHub | Source management |

---

<a id="project-structure"></a>

# 📁 Project Structure

```text
Focusbubble-Frontend/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── focusbubble/
│       │   │           ├── FocusBubbleAccessibilityService.kt
│       │   │           ├── FocusBubbleApplication.kt
│       │   │           ├── FocusBubbleNotificationListener.kt
│       │   │           ├── MainActivity.kt
│       │   │           ├── data/
│       │   │           ├── di/
│       │   │           ├── service/
│       │   │           └── ui/
│       │   └── res/
│       │       ├── drawable/
│       │       ├── layout/
│       │       ├── mipmap/
│       │       ├── values/
│       │       └── xml/
│       └── test/
├── build.gradle.kts
├── gradle.properties
├── gradle/
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

---

<a id="permissions"></a>

# 🔐 Required Permissions

## Overlay Permission

Required for:

- `BlockOverlayService`
- `FloatingTimerService`

```xml
<uses-permission
    android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

## Accessibility Service

Required for:

- Foreground-app detection.
- Blocked-package detection.
- Home navigation.
- Key-event filtering where supported.

```xml
<service
    android:name=".FocusBubbleAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="false">

    <intent-filter>
        <action
            android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>

    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

## Usage Access

```xml
<uses-permission
    android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />
```

## Notifications

```xml
<uses-permission
    android:name="android.permission.POST_NOTIFICATIONS" />
```

---

<a id="backend-integration"></a>

# 🔗 Backend Integration

The Android application communicates with the separate backend repository:

```text
[https://github.com/rozanaim2026/Focusbubble-Backend]
```

Android network files:

```text
app/src/main/java/com/focusbubble/data/network/FocusBubbleApi.kt
app/src/main/java/com/focusbubble/data/network/RetrofitClient.kt
app/src/main/java/com/focusbubble/data/model/ApiModels.kt
```

Repository files:

```text
app/src/main/java/com/focusbubble/data/repository/UserRepository.kt
app/src/main/java/com/focusbubble/data/repository/SessionRepository.kt
app/src/main/java/com/focusbubble/data/repository/ScheduleRepository.kt
app/src/main/java/com/focusbubble/data/repository/BlockedAppsRepository.kt
```

## Production Base URL

```kotlin
[https://your-render-service.onrender.com/]
```

The Retrofit base URL should end with `/`.

## API Flow

```text
Android Retrofit Request
        ↓
FastAPI Route
        ↓
Pydantic Validation
        ↓
CRUD Operation
        ↓
SQLAlchemy Model
        ↓
Database
        ↓
JSON Response
        ↓
Android ApiModels
```

---

<a id="getting-started"></a>

# 🚀 Getting Started

## Clone the Repository

```bash
git clone <frontend-repository-url>
cd Focusbubble-Frontend
```

## Open the Project

1. Open Android Studio.
2. Select **Open**.
3. Choose `Focusbubble-Frontend`.
4. Wait for Gradle sync.
5. Install required Android SDK components.
6. Connect an emulator or physical device.

---

<a id="build-and-run"></a>

# 🏗️ Build and Run

## Build the Debug APK

```bash
./gradlew clean
./gradlew compileDebugKotlin
./gradlew assembleDebug
```

## Install the Application

```bash
./gradlew installDebug
```

## Check the Device

```bash
adb devices
```

Expected:

```text
List of devices attached
emulator-5554    device
```

## Launch the Application

```bash
adb shell am start \
    -n com.focusbubble/.MainActivity
```

## Emulator Offline Fix

```bash
adb kill-server
adb start-server
adb devices
```

If necessary, use Android Studio Device Manager:

1. Stop the emulator.
2. Select **Cold Boot Now**.
3. Restart it.
4. Run `adb devices` again.

---

<a id="testing"></a>

# ✅ Testing

## Blocking Test

1. Grant all permissions.
2. Select WhatsApp.
3. Start a short focus session.
4. Open WhatsApp.
5. Confirm the overlay appears.
6. Confirm WhatsApp cannot be used normally.

## Resume Test

1. Open WhatsApp.
2. Tap Emergency Use.
3. Continue using WhatsApp.
4. Tap the floating timer.
5. Tap Resume.
6. Confirm the overlay appears immediately.

## Leave to Home Test

1. Open a blocked application.
2. Wait for the overlay.
3. Tap Leave to Home.
4. Confirm the overlay disappears.
5. Confirm Home opens.
6. Confirm the session remains active.
7. Open the blocked app again.
8. Confirm the overlay returns.

## Emergency Use Test

1. Open a blocked application.
2. Wait for the overlay.
3. Tap Emergency Use.
4. Confirm the session pauses.
5. Confirm the previous blocked app reopens.
6. Tap Resume later.
7. Confirm the blocked app is blocked again.

---

<a id="demo"></a>

## 🎥 Demo

Watch the FocusBubble application demonstration:

[▶️ Watch the FocusBubble Demo](https://drive.google.com/file/d/1huUHpc7xJlbaWn8B0FC9OGSYItKeiGed/view?usp=sharing)
---

<a id="author"></a>

# 👩‍💻 Author

<div align="center">

## Rozana IM

Android Developer • Backend Developer • Cloud and DevOps Enthusiast

GitHub: [https://github.com/rozanaim2026](https://github.com/rozanaim2026)

Backend Repository: [FocusBubble Backend](https://github.com/rozanaim2026/Focusbubble-Backend)

</div>

---

# ⭐ Support

If you found this project useful, please consider giving the repository a ⭐ on GitHub.

<div align="center">

### Built with Kotlin • Jetpack Compose • Room • Retrofit • FastAPI • SQLAlchemy • Render

</div>
