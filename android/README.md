# PHI Platform Android App

> **Population Health Intelligence Platform for the Philippines**

A comprehensive mobile health screening application enabling Barangay Health Workers (BHWs) to register residents, perform health scans using facial analysis, conduct surveys, and manage referrals - all while operating fully offline.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Documentation](#documentation)
- [Development Phases](#development-phases)

---

## 🎯 Overview

The PHI Platform Android App is designed for population health screening in remote areas of the Philippines. It enables community health workers to:

- **Register residents** with comprehensive demographic data
- **Perform health scans** using BiosenseSignal SDK (34 vital signs from face camera)
- **Conduct structured surveys** (NCD, Maternal Health, Infectious Disease, Mental Health)
- **Generate automatic referrals** for high-risk patients
- **Track incentive earnings** (₱3 per validated scan, ₱150 daily cap)
- **Operate fully offline** with periodic cloud sync

### Target Users
- **Barangay Health Workers (BHWs)** - Primary app users conducting screenings
- **Barangay Health Stations (BHS)** - Receive referrals, provide care
- **Rural Health Units (RHU)** - Provincial health management
- **Residents** - Health screening recipients

---

## ✨ Key Features

### 1. Patient Registration
- Phone number-based identification
- Comprehensive demographics (name, DOB, sex, barangay)
- PhilHealth and PhilSys integration
- Pregnancy status tracking with gestational age
- Messenger opt-in for results notifications
- Duplicate detection

### 2. Health Scanning (34 Biomarkers)
- **Cardiovascular** - BP, HR, HRV, cardiac output, stroke volume, MAP
- **Respiratory** - RR, SpO2, perfusion index, lung capacity, respiratory efficiency
- **Metabolic** - Blood glucose, HbA1c, cholesterol, triglycerides, hemoglobin, etc.
- **Vascular** - Arterial stiffness, vascular age, peripheral resistance
- **Autonomic** - Sympathetic/parasympathetic tone, stress index
- **Body Composition** - BMI, body fat %, visceral fat level
- **Cardiac Function** - LVEF, diastolic function, QRS duration
- Real-time signal quality monitoring
- Automatic risk assessment

### 3. Risk Assessment
- Three-tier risk classification (NORMAL, ELEVATED, HIGH)
- Automatic high-risk flag detection
- Risk score calculation (0-100)
- Specific risk factor identification
- Maternal high-risk flagging for pregnant patients

### 4. Structured Surveys
- **NCD Survey** - Non-communicable disease screening
- **Maternal Health Survey** - Prenatal care assessment
- **Infectious Disease Survey** - Symptom tracking
- **Mental Health Survey** - PHQ-9 scale
- Structured inputs (no free text)
- Progress tracking and validation

### 5. Three-Tier Referral System
- **BHW → BHS** - Initial referrals for high-risk patients
- **BHS → RHU** - Escalation for complex cases
- **RHU → Hospital** - Critical care referrals
- Automatic referral generation on high-risk scans
- 48-hour due dates with overdue tracking
- Status management (PENDING, CONFIRMED, RESOLVED, OVERDUE)

### 6. BHW Incentive Tracking
- ₱3.00 per validated scan
- ₱150.00 daily cap (50 scans maximum)
- Real-time earnings display
- Progress ring showing scans toward cap
- Incentive history tracking

### 7. Offline-First Architecture
- All data operations work offline
- Automatic background sync every 15 minutes (when online)
- Manual sync trigger
- Network status indicators
- Conflict resolution strategies
- Sync queue with priority

### 8. Patient History
- Complete scan history with timeline
- Survey response tracking
- Referral status monitoring
- Risk flag evolution
- Tab-based organization (Overview, Scans, Surveys, Referrals)

### 9. Scan Details
- All 34 biomarkers displayed
- Category filtering (Cardiovascular, Respiratory, etc.)
- Risk assessment breakdown
- Signal quality metrics
- Patient context header

---

## 🏗️ Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                │
│  (Jetpack Compose UI + ViewModels + MVI)   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│            Domain Layer                     │
│  (Use Cases + Repository Interfaces         │
│   + Domain Models + Business Logic)         │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│             Data Layer                      │
│  (Room Database + Ktor API Client +        │
│   Repository Implementations + DTOs)        │
└─────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Clean Architecture** - Separation of concerns, testability, maintainability
2. **MVI Pattern** - Unidirectional data flow in presentation layer
3. **Offline-First** - Local database is source of truth, sync is secondary
4. **Repository Pattern** - Abstract data sources from business logic
5. **Use Case Pattern** - Encapsulate business rules in single-responsibility classes
6. **Dependency Injection** - Hilt for compile-time DI
7. **Reactive Streams** - Kotlin Flow for reactive data

---

## 🛠️ Tech Stack

### Core
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Coroutines & Flow** - Asynchronous programming
- **Hilt** - Dependency injection

### Data & Storage
- **Room** - Local SQLite database
- **DataStore** - Preferences storage (auth tokens, sync state)
- **Kotlinx Serialization** - JSON serialization

### Networking
- **Ktor Client** - HTTP client for Android
- **Azure Functions** - Backend API (Python)
- **PostgreSQL** - Cloud database (Bronze/Silver/Gold schema)

### Background Processing
- **WorkManager** - Periodic sync scheduling
- **ConnectivityManager** - Network state monitoring

### Testing
- **JUnit 4** - Unit testing framework
- **MockK** - Mocking library for Kotlin
- **Turbine** - Flow testing utilities
- **Compose UI Test** - UI testing

### Additional
- **CameraX** - Camera integration
- **ML Kit** - Face detection
- **Material 3** - Design system
- **BiosenseSignal SDK** - Facial analysis for vital signs

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog | 2023.1.1 or newer
- **JDK** 17 or newer
- **Android SDK** 34 (API 34)
- **Minimum Android Version** 8.0 (API 26)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/phi-platform-android.git
   cd phi-platform-android/android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to `phi-platform-android/android`

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Configure API Endpoint** (Optional)
   ```kotlin
   // app/src/main/java/com/globaloutcomes/phi/data/remote/ApiConfig.kt
   const val BASE_URL = "https://your-api-endpoint.azurewebsites.net/api/"
   ```

5. **Run the app**
   - Connect Android device or start emulator
   - Click Run ▶️ or press Shift+F10
   - App will install and launch

### Build Variants

- **Debug** - Development build with logging
- **Release** - Production build with ProGuard

---

## 📁 Project Structure

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/globaloutcomes/phi/
│   │   │   │   ├── data/              # Data layer
│   │   │   │   │   ├── local/         # Room database, DAOs, entities
│   │   │   │   │   ├── remote/        # API client, DTOs
│   │   │   │   │   └── repository/    # Repository implementations
│   │   │   │   ├── domain/            # Domain layer
│   │   │   │   │   ├── model/         # Domain models
│   │   │   │   │   ├── repository/    # Repository interfaces
│   │   │   │   │   ├── usecase/       # Business logic use cases
│   │   │   │   │   └── sync/          # Sync infrastructure
│   │   │   │   ├── presentation/      # Presentation layer
│   │   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   │   ├── home/          # Home screen
│   │   │   │   │   ├── patient/       # Patient screens
│   │   │   │   │   ├── scan/          # Scan screens
│   │   │   │   │   ├── survey/        # Survey screens
│   │   │   │   │   ├── referral/      # Referral screens
│   │   │   │   │   ├── sync/          # Sync UI
│   │   │   │   │   └── theme/         # Material 3 theme
│   │   │   │   └── di/                # Hilt modules
│   │   │   └── AndroidManifest.xml
│   │   └── test/                      # Unit tests
│   │       └── java/com/globaloutcomes/phi/
│   ├── build.gradle.kts               # App-level Gradle
│   └── proguard-rules.pro             # ProGuard configuration
├── docs/                              # Documentation
│   ├── phases/                        # Phase completion docs
│   ├── ARCHITECTURE.md                # Architecture guide
│   ├── TESTING.md                     # Testing guide
│   └── API.md                         # API documentation
├── build.gradle.kts                   # Project-level Gradle
├── settings.gradle.kts                # Gradle settings
└── README.md                          # This file
```

---

## 🧪 Testing

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTest --coverage

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests SyncManagerTest
```

### Test Coverage

Current test coverage:
- **SyncManager** - ✅ 12 tests (upload, download, error handling)
- **NetworkConnectivityObserver** - ✅ 10 tests (connectivity detection)
- **SyncViewModel** - ✅ 8 tests (state management)
- **Target**: 70%+ coverage on domain layer

### Test Structure

```
src/test/java/
├── domain/
│   ├── sync/
│   │   ├── SyncManagerTest.kt
│   │   └── NetworkConnectivityObserverTest.kt
│   └── usecase/
│       ├── RescanEligibilityUseCaseTest.kt
│       └── IncentiveTrackingUseCaseTest.kt
└── presentation/
    ├── sync/
    │   └── SyncViewModelTest.kt
    └── patient/
        └── PatientDetailViewModelTest.kt
```

---

## 📚 Documentation

### Available Documentation

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Detailed architecture guide
- **[TESTING.md](docs/TESTING.md)** - Testing strategies and guidelines
- **[API.md](docs/API.md)** - Backend API reference
- **[SDLC.md](docs/SDLC.md)** - Software development lifecycle tracking

### Phase Documentation

Each development phase has detailed documentation:

- [Phase 9: Backend Sync Integration](docs/phases/PHASE_9_COMPLETE.md)
- [Phase 10: WorkManager Integration & Sync UI](docs/phases/PHASE_10_COMPLETE.md)
- [Phase 11: Patient Detail Screen](docs/phases/PHASE_11_COMPLETE.md)
- [Phase 12: Scan Detail Screen](docs/phases/PHASE_12_COMPLETE.md)

---

## 🗓️ Development Phases

### Completed Phases

- ✅ **Phase 0** - Project Scaffolding
- ✅ **Phase 1** - Core Data Layer (Room database, all 9 entities)
- ✅ **Phase 2** - Theme System (Material 3, custom components)
- ✅ **Phase 3** - Scan Module (stub SDK, camera integration)
- ✅ **Phase 4** - Survey Screens (NCD, Maternal surveys)
- ✅ **Phase 5** - Rescan Eligibility Engine
- ✅ **Phase 6** - BHW Incentive Tracker
- ✅ **Phase 7** - Clinical Encounters (text input)
- ✅ **Phase 8** - Three-Tier Referral System
- ✅ **Phase 9** - Backend Sync Integration
- ✅ **Phase 10** - WorkManager & Sync UI
- ✅ **Phase 11** - Patient Detail Screen (enhanced)
- ✅ **Phase 12** - Scan Detail Screen
- ✅ **Phase 13** - Testing & Documentation (in progress)

### Roadmap

- 🔄 **Phase 13** - Testing & Documentation (current)
- 📋 **Phase 14** - Survey Detail Screen
- 📋 **Phase 15** - Export & Share (PDF reports)
- 📋 **Phase 16** - Real BiosenseSignal SDK Integration
- 📋 **Phase 17** - Voice Input (clinical notes)
- 📋 **Phase 18** - Facebook Messenger Integration
- 📋 **Phase 19** - BHS/RHU Dashboards
- 📋 **Phase 20** - Production Hardening

---

## 🤝 Contributing

### Development Guidelines

1. **Follow Clean Architecture** - Keep layers separated
2. **Write tests** - Unit tests for business logic, UI tests for screens
3. **Use MVI pattern** - Unidirectional data flow in ViewModels
4. **Document your code** - KDoc for public APIs
5. **Follow Kotlin conventions** - Idiomatic Kotlin code
6. **Create feature branches** - Branch from `develop`, merge via PR

### Git Workflow

```bash
# Create feature branch
git checkout develop
git pull
git checkout -b feature/your-feature-name

# Make changes, commit
git add .
git commit -m "feat: Add your feature description"

# Push and create PR
git push origin feature/your-feature-name
```

### Commit Convention

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `test:` - Adding or updating tests
- `refactor:` - Code refactoring
- `style:` - Formatting changes
- `chore:` - Maintenance tasks

---

## 📄 License

This project is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited.

**© 2024 Global Outcomes PHI Platform. All rights reserved.**

---

## 📞 Support

For questions or issues:
- **Technical Issues** - [GitHub Issues](https://github.com/your-org/phi-platform-android/issues)
- **Email** - support@globaloutcomes.com
- **Documentation** - [Full Documentation Site](https://docs.phi-platform.com)

---

## 🙏 Acknowledgments

- **BiosenseSignal** - Facial analysis SDK for vital sign measurement
- **Philippine Department of Health** - Healthcare standards and guidelines
- **Barangay Health Workers** - Field testing and feedback
- **Global Outcomes Team** - Development and support

---

**Built with ❤️ for population health in the Philippines**
