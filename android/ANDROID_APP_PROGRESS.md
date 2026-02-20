# PHI Platform Android App - Build Progress

**Started:** February 19, 2026
**Target:** Kotlin + Jetpack Compose + Clean Architecture

---

## ✅ Phase 0: Project Scaffolding - COMPLETE

### Project Structure Created
```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/globaloutcomes/phi/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/ (entities, dao, converters)
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── remote/
│   │   │   │   ├── domain/ (model, repository, usecase)
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── patient/
│   │   │   │   │   ├── scan/
│   │   │   │   │   ├── survey/
│   │   │   │   │   ├── referral/
│   │   │   │   │   ├── clinical/
│   │   │   │   │   ├── settings/
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── theme/
│   │   │   │   │   └── navigation/
│   │   │   │   └── di/
│   │   │   ├── res/ (resources)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
├── docs/
├── settings.gradle.kts
└── build.gradle.kts
```

### ✅ Files Created

**Build Configuration:**
- `settings.gradle.kts` - Project settings
- `build.gradle.kts` (root) - Plugin configuration
- `app/build.gradle.kts` - **All dependencies configured**

**Dependencies Configured:**
- ✅ Jetpack Compose (UI framework)
- ✅ Material 3 (design system)
- ✅ Room Database (local storage)
- ✅ Hilt (dependency injection)
- ✅ Ktor Client (HTTP networking)
- ✅ Navigation Compose
- ✅ CameraX (for scans)
- ✅ ML Kit Face Detection
- ✅ WorkManager (background sync)
- ✅ DataStore (settings)
- ✅ Lottie (animations)
- ✅ Testing frameworks

**Android Configuration:**
- `AndroidManifest.xml` - Permissions, activities, providers
- `strings.xml` - String resources (English)
- `colors.xml` - GO brand colors + Material 3 theme
- `themes.xml` - Material theme
- `backup_rules.xml` - Backup configuration
- `data_extraction_rules.xml` - Data extraction rules

**Application Code:**
- `PHIApplication.kt` - Application class with Hilt

**API Configuration:**
- Base URL: `https://phi-platform-api.azurewebsites.net/api/`
- BuildConfig field configured for debug & release

---

## 📋 Next Steps (Phase 1: Data Layer)

### Priority 1: Create Room Entities (9 entities)

**File:** `data/local/entities/PatientEntity.kt`
```kotlin
@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val birthdate: Long, // timestamp
    val sex: String, // MALE, FEMALE, OTHER
    val barangayId: String,
    val philHealthNumber: String?,
    val philSysNumber: String?,
    val isPregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val messengerOptIn: Boolean = false,
    val messengerUserId: String? = null,
    val highRiskFlag: Boolean = false,
    val highRiskReasons: String? = null, // JSON array
    val maternalHighRisk: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING" // PENDING, SYNCED, FAILED
)
```

### Other Entities to Create:
1. **ScanEntity** - 34 vital sign fields from BiosenseSignal
2. **SurveyEntity** - Structured survey responses (JSON)
3. **ReferralEntity** - Three-tier referral system
4. **ClinicalEncounterEntity** - Clinical documentation
5. **YakapClaimEntity** - Claims data
6. **BhwIncentiveEntity** - Earnings tracking
7. **ConfigThresholdEntity** - Clinical thresholds from server
8. **BarangayEntity** - Location data

### Priority 2: Create DAOs
- `PatientDao.kt`, `ScanDao.kt`, `SurveyDao.kt`, etc.
- CRUD operations + custom queries

### Priority 3: Create GoDatabase
- Database class with all entities
- Type converters (JSON, timestamps)
- Migration strategy

### Priority 4: Write Unit Tests
- DAO tests with in-memory database
- Target: 20+ tests covering CRUD + business logic

---

## 📋 Week 2 Tasks (After Data Layer)

### Domain Layer
- Domain models (Patient, Scan, etc.)
- Repository interfaces
- Use cases:
  - RegisterPatientUseCase
  - CheckDuplicateUseCase
  - PerformScanUseCase
  - CheckRescanEligibilityUseCase

### Navigation
- NavGraph.kt with all routes
- BottomNavBar composable

### First Screen: Patient Registration
- RegistrationScreen.kt (full form)
- RegistrationViewModel
- Integration with backend

---

## 🎯 Milestone 1 Target (Week 3)

**Complete working flow:**
- Home → New Scan → Register Patient → Scan (stub) → View Results → Home
- Patient saved to Room database
- Deduplication working
- Smooth navigation

---

## 🛠️ Build & Run Commands

### Sync Gradle
```bash
cd /c/Users/Grant/PHI_V1/android
./gradlew --refresh-dependencies
```

### Build APK
```bash
./gradlew assembleDebug
```

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Install on Device
```bash
./gradlew installDebug
```

---

## 📱 Target Devices

- **Minimum SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

---

## 🔑 Key Architecture Decisions

1. **Clean Architecture** - Separation of concerns (data/domain/presentation)
2. **Offline-First** - Room database as single source of truth
3. **Jetpack Compose** - Modern declarative UI
4. **MVI Pattern** - Unidirectional data flow in ViewModels
5. **Hilt DI** - Dependency injection throughout
6. **Work Manager** - Reliable background sync

---

## 📚 Documentation Structure

Following the plan, documentation will include:
- `docs/phases/PHASE_00_SCAFFOLDING.md`
- `docs/phases/PHASE_01_DATA_LAYER.md`
- `docs/ARCHITECTURE.md`
- `docs/SDLC.md`

---

## Next Command to Run

To continue building, we need to create the 9 Room entities. Would you like me to:

**Option A:** Create all 9 Room entities + DAOs + Database class (complete Phase 1)
**Option B:** Create just the first vertical slice (Patient + Scan entities) to get something working quickly
**Option C:** Generate the entire patient registration screen to see UI working
**Option D:** Create a detailed implementation plan document for you to follow

The backend is deployed and ready. The Android scaffolding is complete. Ready to build the app!
