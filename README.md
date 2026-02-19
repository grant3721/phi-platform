# Global Outcomes PHI Platform

Population Health Intelligence Platform for the Philippines

## Overview

The Global Outcomes PHI Platform is a comprehensive Android application designed to enable Barangay Health Workers (BHWs) to conduct population health screening in remote areas of the Philippines. The platform operates offline-first and syncs with an Azure backend when connectivity is available.

## Key Features

- **Health Scanning**: Uses BiosenseSignal SDK to capture 34 vital signs from face camera
- **Patient Registration**: Complete demographic data capture with deduplication
- **Health Surveys**: Structured surveys for NCD, Infectious Diseases, Maternal Health, and Mental Health
- **Risk Assessment**: Automatic high-risk flagging and referral generation
- **Incentive Tracking**: BHW earnings tracker (₱3 per scan, ₱150 daily cap)
- **Offline-First**: Full functionality without internet, periodic cloud sync
- **Clinical Documentation**: Voice-enabled clinical encounter forms
- **Three-Tier Referrals**: BHW → BHS → RHU escalation system
- **Outbreak Detection**: AI-powered outbreak analysis with Claude

## Architecture

- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (data / domain / presentation)
- **State Management**: MVVM + MVI
- **Database**: Room (SQLite)
- **Networking**: Ktor
- **Dependency Injection**: Hilt
- **Backend**: Azure Functions (Python) + PostgreSQL

## Project Structure

```
PHI_V1/
├── app/                         # Android app
│   ├── src/main/java/com/globaloutcomes/phi/
│   │   ├── data/                # Data layer
│   │   │   ├── local/           # Room database
│   │   │   ├── remote/          # API clients
│   │   │   ├── repository/      # Repository implementations
│   │   │   └── sync/            # Sync logic
│   │   ├── domain/              # Business logic layer
│   │   │   ├── model/           # Domain models
│   │   │   ├── repository/      # Repository interfaces
│   │   │   └── usecase/         # Use cases
│   │   ├── presentation/        # UI layer
│   │   │   ├── theme/           # Material 3 theme
│   │   │   ├── components/      # Reusable components
│   │   │   ├── home/            # Home screen
│   │   │   ├── patient/         # Patient screens
│   │   │   ├── scan/            # Scan screens
│   │   │   ├── survey/          # Survey screens
│   │   │   ├── referral/        # Referral screens
│   │   │   └── clinical/        # Clinical screens
│   │   └── di/                  # Dependency injection modules
├── backend/                     # Azure Functions (Python)
├── database/                    # PostgreSQL migrations
└── docs/                        # SDLC documentation
    ├── SDLC.md                  # Master SDLC tracker
    ├── ARCHITECTURE.md          # Architecture documentation
    ├── API_REFERENCE.md         # API documentation
    └── phases/                  # Per-phase documentation
```

## Development Status

**Current Sprint**: Sprint 1 (Weeks 1-2)
**Current Phase**: Phase 0 (Scaffolding)

See `docs/SDLC.md` for detailed progress tracking.

## Build Requirements

- Android Studio Ladybug or later
- JDK 17
- Android SDK 35
- Gradle 8.7+
- BiosenseSignal SDK v5.11 (contact vendor for AAR)

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Obtain BiosenseSignal SDK AAR and place in `app/libs/`
5. Run on device (Min SDK 26, Target SDK 34)

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Test coverage
./gradlew testDebugUnitTestCoverage
```

## Documentation

All project documentation is maintained in the `docs/` directory:

- **SDLC.md**: Master tracker updated after every phase
- **ARCHITECTURE.md**: System architecture and patterns
- **API_REFERENCE.md**: Backend API documentation
- **DATABASE_SCHEMA.md**: Database schema documentation
- **phases/**: Detailed per-phase documentation

## License

Copyright © 2024 Global Outcomes Inc.
All rights reserved.

## Contact

For questions or issues, contact the Global Outcomes development team.
