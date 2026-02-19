# CLAUDE CODE BUILD INSTRUCTIONS
# Global Outcomes — Population Health Intelligence Platform
# Android Application + Azure Backend

## META
- **Project Name**: GlobalOutcomes
- **Package**: com.globaloutcomes.phi
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose only — ZERO XML layouts
- **Architecture**: Clean Architecture (data / domain / presentation) + MVVM + MVI state
- **Build System**: Gradle Kotlin DSL (build.gradle.kts)
- **Backend**: Azure Functions (Python 3.11) + Azure PostgreSQL Flexible Server

---

## PHASE 0: PROJECT SCAFFOLDING

### Step 0.1: Create Android Project

Create a new Android project with the following structure:

```
GlobalOutcomes/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/globaloutcomes/phi/
│   │   │   │   ├── GlobalOutcomesApp.kt              # Application class + Hilt
│   │   │   │   ├── MainActivity.kt                    # Single Activity (Compose)
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── NavGraph.kt                    # Top-level navigation
│   │   │   │   │   ├── Routes.kt                      # Type-safe route definitions
│   │   │   │   │   └── BottomNavBar.kt                # Bottom navigation component
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── GoDatabase.kt              # Room database definition
│   │   │   │   │   │   ├── entities/
│   │   │   │   │   │   │   ├── PatientEntity.kt
│   │   │   │   │   │   │   ├── ScanEntity.kt
│   │   │   │   │   │   │   ├── SurveyEntity.kt
│   │   │   │   │   │   │   ├── ReferralEntity.kt
│   │   │   │   │   │   │   ├── ClinicalEncounterEntity.kt
│   │   │   │   │   │   │   ├── YakapClaimEntity.kt
│   │   │   │   │   │   │   ├── BhwIncentiveEntity.kt
│   │   │   │   │   │   │   ├── ConfigThresholdEntity.kt
│   │   │   │   │   │   │   └── BarangayEntity.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── PatientDao.kt
│   │   │   │   │   │   │   ├── ScanDao.kt
│   │   │   │   │   │   │   ├── SurveyDao.kt
│   │   │   │   │   │   │   ├── ReferralDao.kt
│   │   │   │   │   │   │   ├── ClinicalEncounterDao.kt
│   │   │   │   │   │   │   ├── YakapClaimDao.kt
│   │   │   │   │   │   │   ├── BhwIncentiveDao.kt
│   │   │   │   │   │   │   └── ConfigDao.kt
│   │   │   │   │   │   └── converters/
│   │   │   │   │   │       └── Converters.kt          # Room type converters
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   ├── GoApiService.kt        # Ktor API client
│   │   │   │   │   │   │   ├── AuthApi.kt
│   │   │   │   │   │   │   ├── SyncApi.kt
│   │   │   │   │   │   │   ├── PatientApi.kt
│   │   │   │   │   │   │   ├── ReferralApi.kt
│   │   │   │   │   │   │   ├── ClaimsApi.kt
│   │   │   │   │   │   │   ├── OutbreakApi.kt
│   │   │   │   │   │   │   └── AnalyticsApi.kt
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   ├── SyncUploadDto.kt
│   │   │   │   │   │   │   ├── SyncDownloadDto.kt
│   │   │   │   │   │   │   ├── AuthRequestDto.kt
│   │   │   │   │   │   │   ├── AuthResponseDto.kt
│   │   │   │   │   │   │   └── ...Dto.kt files
│   │   │   │   │   │   └── interceptors/
│   │   │   │   │   │       └── AuthInterceptor.kt     # JWT token injection
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── PatientRepositoryImpl.kt
│   │   │   │   │   │   ├── ScanRepositoryImpl.kt
│   │   │   │   │   │   ├── SurveyRepositoryImpl.kt
│   │   │   │   │   │   ├── ReferralRepositoryImpl.kt
│   │   │   │   │   │   ├── ClinicalRepositoryImpl.kt
│   │   │   │   │   │   ├── ClaimsRepositoryImpl.kt
│   │   │   │   │   │   ├── IncentiveRepositoryImpl.kt
│   │   │   │   │   │   ├── OutbreakRepositoryImpl.kt
│   │   │   │   │   │   ├── AuthRepositoryImpl.kt
│   │   │   │   │   │   └── SyncRepositoryImpl.kt
│   │   │   │   │   └── sync/
│   │   │   │   │       ├── SyncWorker.kt              # WorkManager periodic sync
│   │   │   │   │       ├── SyncManager.kt             # Sync orchestration logic
│   │   │   │   │       └── ConflictResolver.kt        # Server-wins config, client-wins scans
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Patient.kt
│   │   │   │   │   │   ├── Scan.kt
│   │   │   │   │   │   ├── Biomarkers.kt
│   │   │   │   │   │   ├── Survey.kt
│   │   │   │   │   │   ├── NcdSurvey.kt
│   │   │   │   │   │   ├── InfectiousDiseaseSurvey.kt
│   │   │   │   │   │   ├── MaternalSurvey.kt
│   │   │   │   │   │   ├── MentalHealthSurvey.kt
│   │   │   │   │   │   ├── Referral.kt
│   │   │   │   │   │   ├── ClinicalEncounter.kt
│   │   │   │   │   │   ├── YakapClaim.kt
│   │   │   │   │   │   ├── BhwIncentive.kt
│   │   │   │   │   │   ├── OutbreakSignal.kt
│   │   │   │   │   │   ├── RiskFlag.kt
│   │   │   │   │   │   ├── UserRole.kt               # BHW, BHS, RHU, PHO
│   │   │   │   │   │   └── SyncStatus.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── PatientRepository.kt       # Interface
│   │   │   │   │   │   ├── ScanRepository.kt
│   │   │   │   │   │   ├── SurveyRepository.kt
│   │   │   │   │   │   ├── ReferralRepository.kt
│   │   │   │   │   │   ├── ClinicalRepository.kt
│   │   │   │   │   │   ├── ClaimsRepository.kt
│   │   │   │   │   │   ├── IncentiveRepository.kt
│   │   │   │   │   │   ├── OutbreakRepository.kt
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   └── SyncRepository.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── patient/
│   │   │   │   │       │   ├── RegisterPatientUseCase.kt
│   │   │   │   │       │   ├── CheckDuplicateUseCase.kt
│   │   │   │   │       │   └── GetPatientHistoryUseCase.kt
│   │   │   │   │       ├── scan/
│   │   │   │   │       │   ├── PerformScanUseCase.kt
│   │   │   │   │       │   ├── ValidateScanUseCase.kt
│   │   │   │   │       │   └── CheckRescanEligibilityUseCase.kt
│   │   │   │   │       ├── survey/
│   │   │   │   │       │   ├── SubmitNcdSurveyUseCase.kt
│   │   │   │   │       │   ├── SubmitInfectiousSurveyUseCase.kt
│   │   │   │   │       │   ├── SubmitMaternalSurveyUseCase.kt
│   │   │   │   │       │   └── SubmitMentalHealthSurveyUseCase.kt
│   │   │   │   │       ├── referral/
│   │   │   │   │       │   ├── CreateReferralUseCase.kt
│   │   │   │   │       │   ├── UpdateReferralUseCase.kt
│   │   │   │   │       │   └── GetPendingReferralsUseCase.kt
│   │   │   │   │       ├── clinical/
│   │   │   │   │       │   ├── SaveClinicalEncounterUseCase.kt
│   │   │   │   │       │   ├── SaveLabResultsUseCase.kt
│   │   │   │   │       │   └── SaveMaternalClinicalUseCase.kt
│   │   │   │   │       ├── claims/
│   │   │   │   │       │   ├── GenerateYakapClaimUseCase.kt
│   │   │   │   │       │   └── GetClaimStatusUseCase.kt
│   │   │   │   │       ├── incentive/
│   │   │   │   │       │   ├── RecordIncentiveUseCase.kt
│   │   │   │   │       │   ├── GetDailyEarningsUseCase.kt
│   │   │   │   │       │   └── CheckDailyCapUseCase.kt
│   │   │   │   │       └── auth/
│   │   │   │   │           ├── RequestOtpUseCase.kt
│   │   │   │   │           ├── VerifyOtpUseCase.kt
│   │   │   │   │           └── OfflinePinAuthUseCase.kt
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Theme.kt                   # Material 3 dynamic color theme
│   │   │   │   │   │   ├── Color.kt                   # GO brand colors
│   │   │   │   │   │   ├── Type.kt                    # Typography scale
│   │   │   │   │   │   └── Shape.kt                   # Component shapes
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── GoButton.kt                # Primary/Secondary/Danger buttons
│   │   │   │   │   │   ├── GoCard.kt                  # Standard card with elevation
│   │   │   │   │   │   ├── GoChipGroup.kt             # Multi-select symptom chips
│   │   │   │   │   │   ├── GoSlider.kt                # Labeled slider with haptic snap
│   │   │   │   │   │   ├── GoSegmentedButton.kt       # Yes/No/Unknown selector
│   │   │   │   │   │   ├── GoNumberStepper.kt         # [-] value [+] large buttons
│   │   │   │   │   │   ├── GoDateWheel.kt             # Bottom sheet scroll date picker
│   │   │   │   │   │   ├── GoDropdown.kt              # Searchable dropdown
│   │   │   │   │   │   ├── GoVoiceInput.kt            # Mic FAB + waveform + transcription
│   │   │   │   │   │   ├── GoOfflineIndicator.kt      # Sync status chip
│   │   │   │   │   │   ├── GoRiskBadge.kt             # High-risk / Elevated / Normal badge
│   │   │   │   │   │   ├── GoScanAnimation.kt         # Lottie face outline pulse
│   │   │   │   │   │   ├── GoSuccessAnimation.kt      # Lottie confetti / checkmark
│   │   │   │   │   │   ├── GoAlertAnimation.kt        # Lottie warning pulse
│   │   │   │   │   │   ├── GoEarningsCard.kt          # BHW daily earnings display
│   │   │   │   │   │   ├── GoProgressRing.kt          # Circular progress (scans/cap)
│   │   │   │   │   │   ├── GoSearchBar.kt             # Patient search
│   │   │   │   │   │   └── GoEmptyState.kt            # Empty list illustrations
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── AuthScreen.kt              # Phone + OTP entry
│   │   │   │   │   │   ├── PinScreen.kt               # Offline PIN setup/verify
│   │   │   │   │   │   └── AuthViewModel.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt              # BHW home dashboard
│   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   ├── patient/
│   │   │   │   │   │   ├── registration/
│   │   │   │   │   │   │   ├── RegistrationScreen.kt
│   │   │   │   │   │   │   └── RegistrationViewModel.kt
│   │   │   │   │   │   ├── list/
│   │   │   │   │   │   │   ├── PatientListScreen.kt
│   │   │   │   │   │   │   └── PatientListViewModel.kt
│   │   │   │   │   │   └── detail/
│   │   │   │   │   │       ├── PatientDetailScreen.kt
│   │   │   │   │   │       └── PatientDetailViewModel.kt
│   │   │   │   │   ├── scan/
│   │   │   │   │   │   ├── ScanScreen.kt              # Camera + 30-sec scan
│   │   │   │   │   │   ├── ScanResultScreen.kt        # Biomarker results + risk flags
│   │   │   │   │   │   ├── ScanViewModel.kt
│   │   │   │   │   │   └── ScanCooldownTracker.kt     # 3-min cooldown timer between scans
│   │   │   │   │   ├── survey/
│   │   │   │   │   │   ├── NcdSurveyScreen.kt
│   │   │   │   │   │   ├── InfectiousSurveyScreen.kt
│   │   │   │   │   │   ├── MaternalSurveyScreen.kt
│   │   │   │   │   │   ├── MentalHealthSurveyScreen.kt
│   │   │   │   │   │   ├── SurveySelectionScreen.kt   # Which surveys to complete
│   │   │   │   │   │   └── SurveyViewModel.kt
│   │   │   │   │   ├── referral/
│   │   │   │   │   │   ├── ReferralListScreen.kt
│   │   │   │   │   │   ├── ReferralDetailScreen.kt
│   │   │   │   │   │   └── ReferralViewModel.kt
│   │   │   │   │   ├── clinical/
│   │   │   │   │   │   ├── ClinicalEncounterScreen.kt # FPE / follow-up form
│   │   │   │   │   │   ├── LabResultsScreen.kt        # Lab entry
│   │   │   │   │   │   ├── MaternalClinicalScreen.kt  # Maternal clinical entry
│   │   │   │   │   │   └── ClinicalViewModel.kt
│   │   │   │   │   ├── claims/
│   │   │   │   │   │   ├── ClaimsListScreen.kt
│   │   │   │   │   │   ├── ClaimDetailScreen.kt
│   │   │   │   │   │   └── ClaimsViewModel.kt
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   ├── BhsDashboardScreen.kt
│   │   │   │   │   │   ├── RhuDashboardScreen.kt
│   │   │   │   │   │   └── DashboardViewModel.kt
│   │   │   │   │   └── devmenu/
│   │   │   │   │       ├── DevMenuScreen.kt           # Demo/test mode toggles
│   │   │   │   │       └── DevMenuViewModel.kt
│   │   │   │   ├── voice/
│   │   │   │   │   ├── VoiceRecognitionManager.kt     # Abstracts SpeechRecognizer + Vosk
│   │   │   │   │   ├── VoiceCommandProcessor.kt       # "next field", "delete that", etc.
│   │   │   │   │   └── VoiceState.kt                  # Listening, transcribing, idle
│   │   │   │   └── di/
│   │   │   │       ├── AppModule.kt                   # Hilt: database, prefs, voice
│   │   │   │       ├── NetworkModule.kt               # Hilt: Ktor client, API services
│   │   │   │       └── RepositoryModule.kt            # Hilt: binds interfaces to impls
│   │   │   └── res/
│   │   │       ├── raw/
│   │   │       │   ├── scan_progress.json             # Lottie: pulsing face outline
│   │   │       │   ├── scan_success.json              # Lottie: green checkmark confetti
│   │   │       │   ├── scan_rejected.json             # Lottie: red X shake
│   │   │       │   ├── high_risk_alert.json           # Lottie: warning pulse
│   │   │       │   └── syncing.json                   # Lottie: upload animation
│   │   │       └── values/
│   │   │           └── strings.xml                    # All user-facing strings
│   │   └── test/                                      # Unit tests
│   │   └── androidTest/                               # Instrumented tests
├── backend/
│   ├── requirements.txt
│   ├── host.json
│   ├── local.settings.json
│   ├── shared/
│   │   ├── db.py                                      # PostgreSQL connection pool
│   │   ├── auth.py                                    # JWT verification
│   │   ├── models.py                                  # Pydantic models
│   │   └── config.py                                  # Environment config
│   ├── auth_otp_request/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── auth_otp_verify/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── sync_upload/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── sync_download/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── patients_dedup/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── patients_history/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── scans_validate/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── referrals_create/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── referrals_update/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── claims_generate/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── claims_status/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── outbreak_signals/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── outbreak_analyze/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── analytics_province/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── analytics_municipality/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── analytics_query/
│   │   ├── function.json
│   │   └── __init__.py
│   ├── config_thresholds_get/
│   │   ├── function.json
│   │   └── __init__.py
│   └── config_thresholds_update/
│       ├── function.json
│       └── __init__.py
├── database/
│   ├── migrations/
│   │   ├── 001_create_bronze_schema.sql
│   │   ├── 002_create_silver_schema.sql
│   │   ├── 003_create_gold_schema.sql
│   │   ├── 004_create_indexes.sql
│   │   └── 005_seed_barangays.sql
│   └── seed/
│       └── quezon_barangays.csv                       # 1,209 barangays + municipality mapping
├── build.gradle.kts                                   # Root build file
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

### Step 0.2: Root build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("com.google.dagger.hilt.android") version "2.53.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
}
```

### Step 0.3: App build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.globaloutcomes.phi"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.globaloutcomes.phi"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-demo"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        debug {
            buildConfigField("Boolean", "DEMO_MODE_AVAILABLE", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://go-phi-api-dev.azurewebsites.net/api\"")
        }
        release {
            buildConfigField("Boolean", "DEMO_MODE_AVAILABLE", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://go-phi-api.azurewebsites.net/api\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlin { jvmToolchain(17) }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.9.3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.53.1")
    ksp("com.google.dagger:hilt-compiler:2.53.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Ktor (Networking)
    val ktorVersion = "3.0.3"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // CameraX
    val cameraVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // ML Kit Face Detection (scan framing guide)
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.2")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.1")

    // Coil (Image Loading)
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Vosk (Offline Voice Recognition)
    implementation("com.alphacephei:vosk-android:0.3.47")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:6.2.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## PHASE 1: CORE DATA LAYER

### Step 1.1: Room Entities

Every entity must include these fields: `id` (UUID string, primary key), `createdAt` (Long, epoch millis), `updatedAt` (Long, epoch millis), `syncStatus` (enum: PENDING, SYNCED, FAILED).

#### PatientEntity.kt
```kotlin
@Entity(tableName = "patients", indices = [Index(value = ["phoneNumber"], unique = true)])
data class PatientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long,               // epoch millis
    val sex: String,                      // "male" | "female"
    val barangayCode: String,
    val municipalityCode: String,
    val philhealthNumber: String? = null,  // optional in demo
    val philsysId: String? = null,         // optional in demo
    val pregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val highRiskFlag: Boolean = false,
    val highRiskReasons: String? = null,   // JSON array stored as string
    val maternalHighRisk: Boolean = false,
    val lastScanDate: Long? = null,
    val totalScans: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"     // PENDING | SYNCED | FAILED
)
```

#### ScanEntity.kt

**CRITICAL: Every field in this entity maps 1:1 to a BiosenseSignal SDK v5.11 output. No invented fields. No estimates. If the SDK doesn't output it, we don't store it.**

```kotlin
@Entity(tableName = "scans",
    foreignKeys = [ForeignKey(entity = PatientEntity::class, parentColumns = ["id"], childColumns = ["patientId"])])
data class ScanEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val bhwId: String,
    val scanType: String,                  // "baseline" | "maternal_rescan" | "high_risk_rescan"
    val validated: Boolean,
    val rejectionReason: String? = null,

    // ============================================================
    // BIOSENSESIGNAL SDK v5.11 — ALL 34 VITAL SIGNS
    // Source: https://developer.biosensesignal.com/android/5.11/
    // Every field below is a direct SDK output. Nothing invented.
    // ============================================================

    // --- CARDIOVASCULAR (7) ---
    val pulseRate: Double? = null,              // bpm — SDK: Pulse Rate
    val bpSystolic: Double? = null,             // mmHg — SDK: Blood Pressure (systolic)
    val bpDiastolic: Double? = null,            // mmHg — SDK: Blood Pressure (diastolic)
    val meanArterialPressure: Double? = null,   // mmHg — SDK: Mean Arterial Pressure (NEW in v5.11)
    val pulsePressure: Double? = null,          // mmHg — SDK: Pulse Pressure (NEW in v5.11)
    val cardiacWorkload: Double? = null,        // index — SDK: Cardiac Workload (NEW in v5.11)
    val heartAge: Double? = null,               // years — SDK: Heart Age

    // --- RESPIRATORY (2) ---
    val respirationRate: Double? = null,        // breaths/min — SDK: Respiration Rate
    val oxygenSaturation: Double? = null,       // % — SDK: Oxygen Saturation (SpO2)

    // --- BLOODLESS BLOOD TESTS (2) ---
    val hemoglobin: Double? = null,             // g/dL — SDK: Hemoglobin
    val hemoglobinA1c: Double? = null,          // % — SDK: Hemoglobin A1c

    // --- RISK INDICATORS (5) ---
    val ascvdRisk: Double? = null,              // score — SDK: ASCVD Risk
    val ascvdRiskLevel: String? = null,         // category — SDK: ASCVD Risk Level (NEW in v5.11)
    val highBloodPressureRisk: Double? = null,  // score — SDK: High Blood Pressure Risk
    val highFastingGlucoseRisk: Double? = null, // score — SDK: High Fasting Glucose Risk
    val highHemoglobinA1cRisk: Double? = null,  // score — SDK: High Hemoglobin A1c Risk
    val highTotalCholesterolRisk: Double? = null,// score — SDK: High Total Cholesterol Risk
    val lowHemoglobinRisk: Double? = null,      // score — SDK: Low Hemoglobin Risk

    // --- HEART RATE VARIABILITY (10) ---
    val meanRri: Double? = null,                // ms — SDK: Mean RRi
    val rri: Double? = null,                    // ms — SDK: RRi (individual interval)
    val sdnn: Double? = null,                   // ms — SDK: SDNN
    val rmssd: Double? = null,                  // ms — SDK: RMSSD
    val sd1: Double? = null,                    // ms — SDK: SD1
    val sd2: Double? = null,                    // ms — SDK: SD2
    val prq: Double? = null,                    // ratio — SDK: PRQ
    val lfhf: Double? = null,                   // ratio — SDK: LF/HF

    // --- AUTONOMIC NERVOUS SYSTEM (4) ---
    val pnsIndex: Double? = null,               // index — SDK: PNS Index
    val pnsZone: String? = null,                // category — SDK: PNS Zone
    val snsIndex: Double? = null,               // index — SDK: SNS Index
    val snsZone: String? = null,                // category — SDK: SNS Zone

    // --- STRESS (3) ---
    val stressLevel: String? = null,            // category — SDK: Stress Level
    val stressIndex: Double? = null,            // index — SDK: Stress Index
    val normalizedStressIndex: Double? = null,  // index — SDK: Normalized Stress Index

    // --- WELLNESS (2) ---
    val wellnessIndex: Double? = null,          // 0-100 — SDK: Wellness Index
    val wellnessLevel: String? = null,          // category — SDK: Wellness Level

    // ============================================================
    // END SDK VITAL SIGNS — Total: 34 distinct outputs
    //
    // Numeric vitals: 26 (flaggable, chartable, trend-trackable)
    // Categorical: 5 (ASCVD Risk Level, PNS Zone, SNS Zone,
    //                  Stress Level, Wellness Level)
    // Risk scores: 5 (high BP, glucose, A1c, cholesterol, low Hgb)
    //   — these feed directly into riskFlags
    // ============================================================

    // --- SCAN METADATA (not from SDK) ---
    val riskFlags: String? = null,          // JSON array: ["hypertension_crisis","hypoxemia"]
    val overallRiskScore: Double? = null,   // Computed by our app from SDK outputs
    val scanDurationMs: Int? = null,
    val signalQuality: Double? = null,      // SDK reports this
    val gpsLat: Double? = null,
    val gpsLon: Double? = null,
    val deviceId: String? = null,
    val appVersion: String? = null,
    val scannedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
```

**What was REMOVED from previous spec (these are NOT BiosenseSignal outputs):**
- ~~hrv~~ → replaced by meanRri, sdnn, rmssd (SDK gives components, not a single HRV number)
- ~~pulseWaveVelocity~~ → not an SDK output
- ~~bloodVolumePulse~~ → not an SDK output
- ~~respiratoryVariability~~ → not an SDK output
- ~~bmiEstimated~~ → not an SDK output (must be manually calculated from height/weight in clinical encounter)
- ~~bodyFatPct~~ → not an SDK output
- ~~visceralFat~~ → not an SDK output
- ~~waistCircEstimated~~ → not an SDK output
- ~~wellnessScore~~ → replaced by wellnessIndex (SDK's actual field name)
- ~~recoveryCapacity~~ → not an SDK output
- ~~vascularAge~~ → replaced by heartAge (SDK's actual field name)
- ~~cardiacOutput~~ → not an SDK output
- ~~strokeVolume~~ → not an SDK output
- ~~bpClassification~~ → replaced by ascvdRiskLevel (SDK's actual output)
- ~~snsBalance / pnsBalance~~ → replaced by snsIndex/snsZone, pnsIndex/pnsZone (SDK's actual outputs)

#### SurveyEntity.kt
```kotlin
@Entity(tableName = "surveys",
    foreignKeys = [ForeignKey(entity = ScanEntity::class, parentColumns = ["id"], childColumns = ["scanId"])])
data class SurveyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val scanId: String,
    val patientId: String,
    val surveyType: String,               // "ncd" | "infectious" | "maternal" | "mental_health"
    val responses: String,                 // Full JSON blob of all responses
    val completedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
```

#### ReferralEntity.kt
```kotlin
@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val sourceScanId: String,
    val tier: String,                      // "bhw_to_bhs" | "bhs_to_rhu"
    val status: String,                    // "pending" | "confirmed" | "resolved" | "escalated" | "overdue"
    val riskFlags: String? = null,         // JSON array
    val referredAt: Long = System.currentTimeMillis(),
    val dueBy: Long,                       // referredAt + 48 hours
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolutionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
```

#### ClinicalEncounterEntity.kt
```kotlin
@Entity(tableName = "clinical_encounters")
data class ClinicalEncounterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val encounterType: String,            // "fpe" | "follow_up" | "maternal" | "emergency"
    val facilityCode: String,
    val providerId: String,
    // Manual vitals (confirmatory)
    val bpManualSystolic: Double? = null,
    val bpManualDiastolic: Double? = null,
    val hrManual: Double? = null,
    val temperature: Double? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val bmi: Double? = null,
    // Clinical
    val chiefComplaint: String? = null,
    val historyOfPresentIllness: String? = null,
    val pastMedicalHistory: String? = null,
    val physicalExam: String? = null,      // JSON structured by body system
    val diagnosisIcd10: String? = null,    // JSON array of ICD-10 codes
    val diagnosisText: String? = null,     // JSON array of text descriptions
    val medicationsPrescribed: String? = null, // JSON: [{drug,dosage,frequency,duration}]
    val labResults: String? = null,        // JSON: [{test,value,unit,date}]
    // Maternal-specific
    val fundalHeight: Double? = null,
    val fetalHeartRate: Double? = null,
    val fetalPresentation: String? = null,
    val edemaAssessment: String? = null,
    val proteinuria: String? = null,
    val ogttResults: String? = null,       // JSON: {fasting,oneHr,twoHr}
    val riskClassification: String? = null,
    val birthPlan: String? = null,
    // Follow-up
    val referralTo: String? = null,
    val followUpDate: Long? = null,
    val encounterDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
```

#### YakapClaimEntity.kt
```kotlin
@Entity(tableName = "yakap_claims")
data class YakapClaimEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val encounterId: String,
    val claimType: String,                // "fpe" | "follow_up" | "lab" | "medicine" | "screening"
    val philhealthNumber: String? = null,
    val providerId: String,
    val facilityCode: String,
    val diagnosisIcd10: String? = null,
    val claimAmount: Double? = null,
    val status: String = "draft",          // "draft" | "ready" | "submitted" | "accepted" | "denied"
    val denialReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val submittedAt: Long? = null,
    val syncStatus: String = "PENDING"
)
```

#### BhwIncentiveEntity.kt
```kotlin
@Entity(tableName = "bhw_incentives")
data class BhwIncentiveEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bhwId: String,
    val scanId: String,
    val date: Long,                        // epoch millis, date only (start of day)
    val amount: Double,                    // 3.0 or 0.0
    val status: String,                    // "earned" | "rejected" | "paid"
    val rejectionReason: String? = null,
    val dailyRunningTotal: Double,
    val createdAt: Long = System.currentTimeMillis()
)
```

#### ConfigThresholdEntity.kt
```kotlin
@Entity(tableName = "config_thresholds")
data class ConfigThresholdEntity(
    @PrimaryKey val key: String,           // e.g. "bp_systolic_crisis", "spo2_low"
    val value: Double,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### BarangayEntity.kt
```kotlin
@Entity(tableName = "barangays")
data class BarangayEntity(
    @PrimaryKey val code: String,
    val name: String,
    val municipalityCode: String,
    val municipalityName: String,
    val population: Int? = null
)
```

### Step 1.2: Room DAOs

Each DAO must support:
- Insert (OnConflictStrategy.REPLACE)
- Query by ID
- Query all with syncStatus filter
- Update syncStatus
- Relevant business queries (see below)

#### Critical DAO Queries

**PatientDao.kt:**
```kotlin
@Query("SELECT * FROM patients WHERE phoneNumber = :phone LIMIT 1")
suspend fun findByPhone(phone: String): PatientEntity?

@Query("SELECT * FROM patients WHERE highRiskFlag = 1")
fun getHighRiskPatients(): Flow<List<PatientEntity>>

@Query("SELECT * FROM patients WHERE pregnant = 1")
fun getPregnantPatients(): Flow<List<PatientEntity>>

@Query("SELECT COUNT(*) FROM patients")
fun getTotalPatientCount(): Flow<Int>

@Query("SELECT COUNT(DISTINCT patientId) FROM scans WHERE validated = 1")
fun getUniqueScannedCount(): Flow<Int>
```

**ScanDao.kt:**
```kotlin
@Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC")
fun getScansForPatient(patientId: String): Flow<List<ScanEntity>>

@Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC LIMIT 1")
suspend fun getLatestScan(patientId: String): ScanEntity?

@Query("SELECT COUNT(*) FROM scans WHERE bhwId = :bhwId AND validated = 1 AND date(scannedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
fun getTodayValidatedCount(bhwId: String): Flow<Int>

@Query("SELECT COUNT(*) FROM scans WHERE validated = 1")
fun getTotalValidatedScans(): Flow<Int>
```

**BhwIncentiveDao.kt:**
```kotlin
@Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND date = :todayEpoch")
suspend fun getDailyTotal(bhwId: String, todayEpoch: Long): Double?

@Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND date >= :monthStartEpoch")
suspend fun getMonthlyTotal(bhwId: String, monthStartEpoch: Long): Double?
```

**ReferralDao.kt:**
```kotlin
@Query("SELECT * FROM referrals WHERE status = 'pending' AND tier = :tier ORDER BY referredAt ASC")
fun getPendingByTier(tier: String): Flow<List<ReferralEntity>>

@Query("SELECT * FROM referrals WHERE status = 'pending' AND dueBy < :now")
fun getOverdueReferrals(now: Long): Flow<List<ReferralEntity>>
```

### Step 1.3: Room Database

```kotlin
@Database(
    entities = [
        PatientEntity::class,
        ScanEntity::class,
        SurveyEntity::class,
        ReferralEntity::class,
        ClinicalEncounterEntity::class,
        YakapClaimEntity::class,
        BhwIncentiveEntity::class,
        ConfigThresholdEntity::class,
        BarangayEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GoDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun scanDao(): ScanDao
    abstract fun surveyDao(): SurveyDao
    abstract fun referralDao(): ReferralDao
    abstract fun clinicalEncounterDao(): ClinicalEncounterDao
    abstract fun yakapClaimDao(): YakapClaimDao
    abstract fun bhwIncentiveDao(): BhwIncentiveDao
    abstract fun configDao(): ConfigDao
}
```

---

## PHASE 2: THEME & DESIGN SYSTEM

### Step 2.1: Brand Colors

```kotlin
// GO Brand Colors
val GoGreen = Color(0xFF0D8A72)         // Primary — teal/green
val GoGreenLight = Color(0xFFECFDF5)    // Containers, backgrounds
val GoGreenDark = Color(0xFF065F4E)     // Dark variant
val GoDark = Color(0xFF1B3A5C)          // Navy — headings, dark surfaces
val GoRed = Color(0xFFDC2626)           // High-risk, errors, alerts
val GoRedLight = Color(0xFFFEF2F2)      // High-risk backgrounds
val GoAmber = Color(0xFFEA580C)         // Elevated risk, warnings
val GoAmberLight = Color(0xFFFFF7ED)    // Warning backgrounds
val GoBlue = Color(0xFF2E75B6)          // Info, links
val GoGray = Color(0xFF6B7280)          // Secondary text
val GoGrayLight = Color(0xFFF3F4F6)     // Backgrounds, dividers
```

### Step 2.2: Typography

Use Google Sans or fallback to system default. Scale:
- Display: 32sp bold — screen titles
- Headline: 24sp bold — section headers
- Title: 20sp medium — card titles
- Body: 16sp regular — primary content
- Label: 14sp medium — button text, labels
- Caption: 12sp regular — timestamps, metadata

### Step 2.3: Component Specifications

Every custom component in `presentation/components/` must:
- Use Material 3 tokens (no hardcoded dp/sp values where M3 provides tokens)
- Support dark mode automatically via theme
- Have minimum 48dp touch targets
- Include haptic feedback where applicable (sliders, buttons, toggles)
- Include content descriptions for accessibility

---

## PHASE 3: SCAN MODULE (Real BiosenseSignal SDK)

### Step 3.1: Scan Engine — ALL SCANS USE REAL BIOSENSESIGNAL SDK

**There is no simulated scan mode in this build.** Every scan — whether testing, demo, or production — runs through the live BiosenseSignal SDK and produces real biomarker data from the device camera. This means:

- A valid BiosenseSignal license key must be activated on the device before any scan
- The device must have completed one-time license validation (requires internet once)
- The 3-minute cooldown between scans is accepted as part of the workflow
- The BHW spends the cooldown period completing registration, surveys, and moving to the next resident

**ScanSimulator.kt is NOT created.** The `ScanEngine` interface has only one implementation: `BiosenseScanEngine.kt` which wraps the real SDK.

The test patient generator (Phase 11) still creates fake patient demographics for testing, but the SCAN for each test patient is a real SDK scan of a real face.

**Normal Ranges for High-Risk Flagging (reference only — actual values come from SDK):**
| SDK Vital Sign | Field Name | Normal Min | Normal Max | Unit |
|---------------|-----------|-----------|-----------|------|
| Pulse Rate | pulseRate | 60 | 100 | bpm |
| Blood Pressure (systolic) | bpSystolic | 100 | 130 | mmHg |
| Blood Pressure (diastolic) | bpDiastolic | 60 | 85 | mmHg |
| Respiration Rate | respirationRate | 12 | 20 | breaths/min |
| Oxygen Saturation | oxygenSaturation | 95 | 100 | % |
| Hemoglobin | hemoglobin | 12.0 | 17.5 | g/dL |
| Hemoglobin A1c | hemoglobinA1c | 4.0 | 5.6 | % |
| Stress Index | stressIndex | — | — | index (SDK scale) |
| Wellness Index | wellnessIndex | 70 | 100 | score |
| Mean Arterial Pressure | meanArterialPressure | 70 | 100 | mmHg |
| Pulse Pressure | pulsePressure | 30 | 50 | mmHg |

### Step 3.2: ScanScreen.kt

**UI Flow (all scans use real BiosenseSignal SDK):**
1. CameraX preview fills screen with semi-transparent oval face guide overlay
2. ML Kit face detection confirms face is in frame → green outline
3. BiosenseSignal SDK `ImageValidity` provides real-time guidance:
   - Face not detected → "Position your face in the oval"
   - Too much movement → "Hold still"
   - Poor lighting → "Move to better lighting"
4. "Start Scan" button (large, bottom center, 56dp height)
5. 50-second countdown timer (circular progress ring around camera preview)
6. Real-time vital sign updates shown as they compute (some arrive before others)
7. Lottie pulsing animation on face outline during scan
8. On complete: haptic buzz + Lottie success/alert animation
9. Transition to ScanResultScreen with shared element animation
10. After scan: 3-minute cooldown timer shown — "Next scan available in X:XX"
    - BHW uses this time for surveys, registration, walking to next house
    - Cooldown timer visible on home screen so BHW knows when device is ready

### Step 3.3: ScanResultScreen.kt

**Layout:**
- Top: Patient name + risk badge (Normal / Elevated / High-Risk)
- Body: Biomarker cards grouped by SDK category:

  **Cardiovascular** (7): Pulse Rate, BP Systolic, BP Diastolic, Mean Arterial Pressure, Pulse Pressure, Cardiac Workload, Heart Age

  **Respiratory** (2): Respiration Rate, Oxygen Saturation (SpO2)

  **Bloodless Blood Tests** (2): Hemoglobin, Hemoglobin A1c

  **Risk Indicators** (6): ASCVD Risk + Level, High BP Risk, High Fasting Glucose Risk, High A1c Risk, High Total Cholesterol Risk, Low Hemoglobin Risk

  **Heart Rate Variability** (8): Mean RRi, RRi, SDNN, RMSSD, SD1, SD2, PRQ, LF/HF

  **Autonomic Nervous System** (4): PNS Index + Zone, SNS Index + Zone

  **Stress** (3): Stress Level, Stress Index, Normalized Stress Index

  **Wellness** (2): Wellness Index, Wellness Level

  - Each card shows metric name, value, unit, and color indicator (green/amber/red)
  - Abnormal values highlighted with red text and GoRiskBadge
  - Categorical values (Risk Level, Zones, Levels) shown as colored chips
  - Risk Indicators shown as a dedicated section with clear pass/flag visual
- If HIGH-RISK flagged: Full-width red alert banner with Lottie warning animation
  - "HIGH-RISK: [reason]. Referral generated. Advise resident to visit BHS within 48 hours."
- Bottom: Action buttons
  - "Continue to Survey" (primary)
  - "View Details" (secondary — expands full 34-biomarker list)
  - If high-risk: "View Referral" button added

---

## PHASE 4: SURVEY SCREENS

### Design Rules for ALL Survey Screens

1. **NO free text fields** except where explicitly marked. Every question uses structured input.
2. **One question per visible section.** Surveys scroll vertically. Each question is a card.
3. **Progress bar** at top showing completion percentage.
4. **"Next" button** at bottom of each card auto-scrolls to next question.
5. **Required fields** marked with subtle asterisk. Cannot proceed to next section until answered.
6. **All inputs use custom GO components**: GoSegmentedButton, GoChipGroup, GoSlider, GoNumberStepper, GoDateWheel, GoDropdown.

### Survey 4A: NCD Risk Assessment — Input Mapping

| Question | Component | Options |
|----------|-----------|---------|
| Smoking status | GoSegmentedButton | Never / Former / Current |
| Alcohol use | GoSegmentedButton | Never / Occasional / Regular / Heavy |
| Physical activity | GoSlider (4 stops) | Sedentary / Light / Moderate / Active |
| Diet quality | GoSlider (3 stops) | Poor / Fair / Good |
| Family history: hypertension | GoSegmentedButton | Yes / No |
| Family history: diabetes | GoSegmentedButton | Yes / No |
| Family history: heart disease | GoSegmentedButton | Yes / No |
| Family history: stroke | GoSegmentedButton | Yes / No |
| Family history: cancer | GoSegmentedButton | Yes / No |
| Known existing conditions | GoChipGroup (multi) | Hypertension / Diabetes / Asthma / Heart Disease / Kidney Disease / Thyroid / None |
| Current medications | GoDropdown (searchable, multi) | GAMOT formulary autocomplete + free text fallback |
| Medication adherence | GoSlider (4 stops) | Always / Usually / Sometimes / Rarely |

### Survey 4B: Infectious Disease — Input Mapping

| Question | Component | Options |
|----------|-----------|---------|
| Fever | GoSegmentedButton | Yes / No |
| → If yes: duration | GoNumberStepper | 1-30 days |
| Cough | GoSegmentedButton | Yes / No |
| → If yes: duration | GoNumberStepper | 1-30 days |
| → Productive | GoSegmentedButton | Yes / No |
| Difficulty breathing | GoSegmentedButton | Yes / No |
| → Severity | GoSlider (3 stops) | Mild / Moderate / Severe |
| Diarrhea | GoSegmentedButton | Yes / No |
| → Duration | GoNumberStepper | 1-30 days |
| → Bloody | GoSegmentedButton | Yes / No |
| Vomiting | GoSegmentedButton | Yes / No |
| Rash | GoSegmentedButton | Yes / No |
| Headache | GoSegmentedButton | Yes / No |
| → Severity | GoSlider (3 stops) | Mild / Moderate / Severe |
| Joint/muscle pain | GoSegmentedButton | Yes / No |
| Unusual bleeding | GoSegmentedButton | Yes / No |
| Symptom onset date | GoDateWheel | Date picker (max: today) |
| Travel last 14 days | GoSegmentedButton | Yes / No |
| → Destination | GoDropdown | Province list + "International" |
| Contact with sick person | GoSegmentedButton | Yes / No |
| Standing water near home | GoSegmentedButton | Yes / No |
| Animal contact | GoSegmentedButton | Yes / No |
| → Type | GoChipGroup | Livestock / Poultry / Stray dogs / Stray cats / Rodents |
| Crowded living | GoSegmentedButton | Yes / No |
| Dengue vaccine | GoSegmentedButton | Complete / Partial / None / Unknown |
| TB (BCG) | GoSegmentedButton | Yes / No / Unknown |
| Measles/MMR | GoSegmentedButton | Complete / Partial / None / Unknown |
| COVID-19 | GoSegmentedButton | Complete / Partial / None / Unknown |
| Influenza (current year) | GoSegmentedButton | Yes / No / Unknown |

**Conditional logic**: Symptom follow-up fields (duration, severity, bloody) only appear if parent symptom = Yes. Use AnimatedVisibility for smooth expand/collapse.

### Survey 4C: Maternal Health — Input Mapping

| Question | Component | Options |
|----------|-----------|---------|
| Last menstrual period | GoDateWheel | Date picker (max: today, min: 10 months ago) |
| Gestational age | Display only | Auto-calculated from LMP. Show as "X weeks, Y days" |
| Gravidity | GoNumberStepper | 1-15 |
| Parity | GoNumberStepper | 0-15 |
| Previous complications | GoChipGroup (multi) | Preeclampsia / GDM / Preterm / Stillbirth / C-section / Hemorrhage / None |
| Current symptoms | GoChipGroup (multi) | Headache / Vision changes / Epigastric pain / Swelling / Bleeding / Contractions / Reduced fetal movement / None |
| Prenatal visits this pregnancy | GoNumberStepper | 0-20 |
| Iron/folate supplementation | GoSegmentedButton | Yes / No |
| Tetanus toxoid | GoSegmentedButton | Complete / Partial / None |

### Survey 4D: Mental Health (PHQ-2) — Input Mapping

| Question | Component | Options |
|----------|-----------|---------|
| Little interest or pleasure (last 2 weeks) | GoSlider (4 stops) | Not at all (0) / Several days (1) / More than half the days (2) / Nearly every day (3) |
| Feeling down/depressed/hopeless (last 2 weeks) | GoSlider (4 stops) | Same scale |
| PHQ-2 Score | Display only | Auto-sum, shown in colored badge (0-2 green, 3-4 amber, 5-6 red) |
| If score >= 3: expand PHQ-9 | Conditional | 7 additional questions, same slider format |

---

## PHASE 5: RESCAN ELIGIBILITY ENGINE

### Step 5.1: CheckRescanEligibilityUseCase.kt

This is a critical business logic class. It must implement exactly this decision tree:

```
INPUT: phoneNumber (String)
OUTPUT: RescanResult (ALLOWED_BASELINE | ALLOWED_MATERNAL | ALLOWED_MATERNAL_HIGH_RISK | ALLOWED_HIGH_RISK | ALLOWED_ANNUAL | REJECTED)

LOGIC:
  patient = patientDao.findByPhone(phoneNumber)

  IF patient IS NULL:
    RETURN ALLOWED_BASELINE
    (First-ever scan for this resident)

  lastScan = scanDao.getLatestScan(patient.id)

  IF patient.highRiskFlag == true:
    RETURN ALLOWED_HIGH_RISK
    (Active high-risk flag — no time restriction)

  IF patient.pregnant == true AND patient.maternalHighRisk == true:
    RETURN ALLOWED_MATERNAL_HIGH_RISK
    (Pregnant + high-risk — no time restriction)

  IF patient.pregnant == true AND lastScan.scannedAt < (now - 180 days):
    RETURN ALLOWED_MATERNAL
    (Standard maternal rescan — 6 month interval)

  IF lastScan.scannedAt < (now - 365 days):
    RETURN ALLOWED_ANNUAL
    (Annual rescan — 12 month interval)

  RETURN REJECTED
    (Not eligible. Include: lastScanDate, reason, daysUntilEligible)
```

### Step 5.2: High-Risk Flag Logic

After each scan, evaluate biomarkers against configurable thresholds:

```
IF bpSystolic > threshold("bp_systolic_crisis") OR bpDiastolic > threshold("bp_diastolic_crisis"):
  SET patient.highRiskFlag = true
  ADD "hypertension_crisis" to patient.highRiskReasons
  IF patient.pregnant: SET patient.maternalHighRisk = true

IF oxygenSaturation < threshold("spo2_low"):
  SET patient.highRiskFlag = true
  ADD "hypoxemia" to patient.highRiskReasons

IF pulseRate > threshold("hr_high") OR pulseRate < threshold("hr_low"):
  SET patient.highRiskFlag = true
  ADD "cardiac_arrhythmia" to patient.highRiskReasons

IF respirationRate > threshold("rr_high") OR respirationRate < threshold("rr_low"):
  SET patient.highRiskFlag = true
  ADD "respiratory_distress" to patient.highRiskReasons

IF highBloodPressureRisk > threshold("high_bp_risk_threshold"):
  ADD "high_bp_risk" to patient.highRiskReasons

IF highFastingGlucoseRisk > threshold("high_glucose_risk_threshold"):
  ADD "high_glucose_risk" to patient.highRiskReasons

IF highHemoglobinA1cRisk > threshold("high_a1c_risk_threshold"):
  ADD "high_a1c_risk" to patient.highRiskReasons

IF lowHemoglobinRisk > threshold("low_hemoglobin_risk_threshold"):
  ADD "low_hemoglobin_risk" to patient.highRiskReasons

IF highTotalCholesterolRisk > threshold("high_cholesterol_risk_threshold"):
  ADD "high_cholesterol_risk" to patient.highRiskReasons
```

Default thresholds (stored in ConfigThresholdEntity, synced from server):
| Key | Default Value | SDK Source Field |
|-----|---------------|-----------------|
| bp_systolic_crisis | 160.0 | bpSystolic |
| bp_diastolic_crisis | 100.0 | bpDiastolic |
| spo2_low | 94.0 | oxygenSaturation |
| hr_high | 120.0 | pulseRate |
| hr_low | 50.0 | pulseRate |
| rr_high | 25.0 | respirationRate |
| rr_low | 10.0 | respirationRate |
| stress_high | 4.0 | stressIndex |
| high_bp_risk_threshold | 0.7 | highBloodPressureRisk |
| high_glucose_risk_threshold | 0.7 | highFastingGlucoseRisk |
| high_a1c_risk_threshold | 0.7 | highHemoglobinA1cRisk |
| high_cholesterol_risk_threshold | 0.7 | highTotalCholesterolRisk |
| low_hemoglobin_risk_threshold | 0.7 | lowHemoglobinRisk |

**Note:** Risk indicator thresholds (0.7 default) will need calibration once we see real SDK output ranges. These are SDK-computed risk scores — consult BiosenseSignal documentation for their exact scale and recommended clinical cutoffs.

---

## PHASE 6: BHW INCENTIVE TRACKER

### Step 6.1: Business Rules

```
CONSTANTS:
  INCENTIVE_PER_SCAN = 3.00        // PHP
  DAILY_CAP = 150.00               // PHP
  MAX_SCANS_PER_DAY = 50           // = DAILY_CAP / INCENTIVE_PER_SCAN

AFTER EACH VALIDATED SCAN:
  todayTotal = incentiveDao.getDailyTotal(bhwId, todayEpoch)

  IF todayTotal >= DAILY_CAP:
    RETURN: Daily cap reached. No additional incentive.
    (App blocks new scans until next calendar day)

  INSERT incentive record:
    amount = INCENTIVE_PER_SCAN
    status = "earned"
    dailyRunningTotal = todayTotal + INCENTIVE_PER_SCAN

AFTER EACH REJECTED SCAN:
  INSERT incentive record:
    amount = 0.0
    status = "rejected"
    rejectionReason = [reason from dedup/validation]
```

### Step 6.2: HomeScreen.kt — BHW Dashboard

**Layout (top to bottom):**

1. **Greeting bar**: "Good morning, [name]" + date + GoOfflineIndicator chip (right-aligned)

2. **Earnings card** (GoEarningsCard — prominent, full width):
   - Left: GoProgressRing showing scans today / 50 max
   - Center: "₱[amount] / ₱150" in large text (GoGreen if under cap, GoAmber at 80%, GoRed at cap)
   - Right: "Today: [N] scans" count
   - Animated fill on the progress ring as scans accumulate

3. **Quick actions row** (3 cards, equal width):
   - "New Scan" (GoGreen, scan icon, primary action)
   - "My Patients" (GoBlue, people icon)
   - "Sync Now" (GoGray, cloud icon + pending count badge)

4. **Monthly summary card**:
   - Vico line chart: daily earnings for current month (x: dates, y: ₱ earned)
   - Total this month: ₱[amount]
   - Total scans this month: [N]

5. **Active referrals card** (only if BHW has pending referrals):
   - List of patients referred, status, days since referral
   - Tap to view referral detail

6. **Rejected scans today** (only if > 0):
   - Count + breakdown by reason (duplicate, quality, cap reached)

---

## PHASE 7: CLINICAL ENCOUNTER + VOICE INPUT

### Step 7.1: Voice Input Architecture

**VoiceRecognitionManager.kt** must:
1. Check device capability on init:
   - If Google on-device speech available → use SpeechRecognizer with EXTRA_PREFER_OFFLINE = true
   - Else → initialize Vosk with pre-downloaded Filipino + English models
2. Expose a StateFlow<VoiceState>:
   ```kotlin
   sealed class VoiceState {
       object Idle : VoiceState()
       object Listening : VoiceState()
       data class Transcribing(val partialText: String) : VoiceState()
       data class Result(val finalText: String) : VoiceState()
       data class Error(val message: String) : VoiceState()
   }
   ```
3. Support language toggle: Filipino / English (stored in DataStore preference)
4. Handle audio focus, microphone permissions

**VoiceCommandProcessor.kt** must recognize:
| Spoken Command | Action |
|---------------|--------|
| "next field" / "susunod" | Move focus to next input field |
| "go back" / "bumalik" | Move focus to previous input field |
| "delete that" / "burahin" | Clear last transcribed sentence |
| "save" / "i-save" | Trigger form save |
| "read it back" / "basahin" | TTS reads current field content |

**GoVoiceInput.kt** (Composable):
```
@Composable
fun GoVoiceInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 3,
    maxLines: Int = 8
)
```
- Text field with mic FAB floating at bottom-right of field
- Tap mic → pulsing Lottie animation + waveform visualization
- Real-time partial transcription appears in field as user speaks
- Tap mic again → finalize text
- Long-press mic → show language selector (Filipino / English)

### Step 7.2: ClinicalEncounterScreen.kt

This screen is for BHS/RHU clinical staff only. It is accessed from a referral or a patient record.

**Layout:**
- **Header**: Patient name, age, sex, barangay + risk badge
- **Scan comparison panel**: Side-by-side latest scan biomarkers vs. manual vitals entered below (shows concordance/discordance)
- **Sections** (each collapsible card):

**Section A: Manual Vitals (always expanded first)**
| Field | Component |
|-------|-----------|
| BP (cuff) | Two GoNumberSteppers side by side: Systolic / Diastolic |
| Heart rate | GoNumberStepper |
| Temperature °C | GoSlider (35.0 - 42.0, 0.1 step) |
| Weight kg | GoNumberStepper (0.1 step) |
| Height cm | GoNumberStepper |
| BMI | Display only (auto-calc from weight/height) |

**Section B: Clinical Notes (voice-enabled)**
| Field | Component |
|-------|-----------|
| Chief complaint | GoVoiceInput (3-line text field + mic) |
| History of present illness | GoVoiceInput (5-line) |
| Past medical history | GoVoiceInput (3-line) + GoChipGroup for common conditions |
| Physical exam | GoVoiceInput per body system: HEENT, Chest, Abdomen, Extremities, Neuro (collapsible sub-sections) |

**Section C: Diagnosis**
| Field | Component |
|-------|-----------|
| ICD-10 code | GoDropdown (searchable, pulls from pre-loaded ICD-10 database). Multi-select. |
| Diagnosis text | Auto-populated from ICD-10 selection, editable |

**Section D: Treatment**
| Field | Component |
|-------|-----------|
| Medications | GoDropdown (GAMOT formulary, searchable) + dosage/frequency/duration per med. "Add medication" button for multiple. |
| Referral to higher facility | GoSegmentedButton (Yes/No) + GoDropdown (facility list) + GoVoiceInput (reason) |
| Follow-up date | GoDateWheel |

**Section E: YAKAP Claim Preview**
- Auto-generated from above fields
- Shows: Claim type, required fields status (✓ complete / ✗ missing), estimated claim value
- "Generate Claim" button (creates YakapClaimEntity in READY status)

---

## PHASE 8: REFERRAL SYSTEM

### Step 8.1: Auto-Referral Generation

When a scan flags HIGH-RISK:
1. Create ReferralEntity with:
   - tier = "bhw_to_bhs"
   - status = "pending"
   - dueBy = now + 48 hours
   - riskFlags = copy from scan.riskFlags
2. Update patient.highRiskFlag = true
3. Show in-app alert to BHW:
   - Full-width red banner at top of ScanResultScreen
   - Lottie warning animation
   - "HIGH-RISK: [reason]. Referral generated to [nearest BHS]."
   - "Advise resident to visit within 48 hours."
4. Add to BHW's active referral list on HomeScreen

### Step 8.2: BHS Confirmatory Flow

BHS staff sees incoming referrals on their dashboard:
1. Tap referral → see original scan data + risk flags
2. Perform confirmatory rescan (same ScanScreen, but tagged as rescan)
3. After rescan:
   - If HIGH-RISK confirmed: Tap "Confirm & Refer to RHU" → creates new ReferralEntity tier = "bhs_to_rhu"
   - If NOT confirmed: Tap "Resolved at BHS" → updates status = "resolved" + GoVoiceInput for notes

### Step 8.3: Overdue Referral Escalation

A background job (runs every hour via WorkManager) checks:
```
SELECT * FROM referrals WHERE status = 'pending' AND dueBy < NOW()
```
For each overdue referral:
1. Update status = "overdue"
2. Push notification to BHW: "Referral for [patient] is overdue. Please follow up."
3. Flag on BHS/RHU dashboard in red

---

## PHASE 9: SYNC & OFFLINE

### Step 9.1: SyncWorker.kt

- **Periodic**: Every 15 minutes when connectivity detected (WorkManager PeriodicWorkRequest)
- **One-shot**: Triggered by user tapping "Sync Now"
- **Constraints**: NetworkType.CONNECTED

**Upload sequence** (order matters):
1. Patients with syncStatus = PENDING
2. Scans with syncStatus = PENDING
3. Surveys with syncStatus = PENDING
4. Referrals with syncStatus = PENDING
5. Clinical encounters with syncStatus = PENDING
6. YAKAP claims with syncStatus = PENDING

**Download sequence:**
1. Config thresholds (always pull latest)
2. Dedup results (server may have rejected scans found duplicate province-wide)
3. Referral status updates (BHS/RHU may have updated from their device)
4. Patient updates (high-risk flags set by clinical staff)

**Conflict resolution:**
- Config/thresholds: SERVER WINS (always overwrite local)
- New scans/surveys/registrations: CLIENT WINS (append-only, server dedup runs post-sync)
- Referral status: LATEST TIMESTAMP WINS
- Patient flags: MERGE (union of risk flags from all sources)

### Step 9.2: SyncManager.kt

Exposes:
```kotlin
val syncState: StateFlow<SyncState>

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val progress: Float, val currentStep: String) : SyncState()
    data class Success(val uploaded: Int, val downloaded: Int, val timestamp: Long) : SyncState()
    data class Failed(val error: String, val retryAt: Long) : SyncState()
}

val pendingCount: StateFlow<Int>  // Total records awaiting sync
```

---

## PHASE 10: AZURE BACKEND

### Step 10.1: PostgreSQL Schema

Create the database with three schemas: `bronze`, `silver`, `gold`.

Execute migrations in order:
1. `001_create_bronze_schema.sql` — raw tables (scan_raw, survey_raw, patient_registration_raw)
2. `002_create_silver_schema.sql` — validated tables (patients, scans, surveys, clinical_encounters, referrals)
3. `003_create_gold_schema.sql` — analytics tables (population_cohorts, outbreak_signals, yakap_claims, bhw_incentives, ai_queries)
4. `004_create_indexes.sql` — indexes on phone_number, barangay_code, scan dates, risk flags
5. `005_seed_barangays.sql` — insert 1,209 Quezon barangays with municipality mapping

Full SQL for each migration is defined in the build spec document (GO_Android_Build_Spec.md). Use those exact schemas.

### Step 10.2: Azure Functions (Python)

Each function follows this pattern:
```python
import azure.functions as func
import json
from shared.db import get_connection
from shared.auth import verify_jwt
from shared.models import [relevant pydantic models]

def main(req: func.HttpRequest) -> func.HttpResponse:
    # 1. Verify JWT (except auth endpoints)
    # 2. Parse request body
    # 3. Execute business logic
    # 4. Return JSON response
```

**Critical endpoint: sync_upload**
This is the highest-traffic endpoint. It receives a batch of all pending records from the mobile device:
```json
{
  "device_id": "...",
  "bhw_id": "...",
  "patients": [...],
  "scans": [...],
  "surveys": [...],
  "referrals": [...],
  "clinical_encounters": [...],
  "claims": [...]
}
```
Processing:
1. Insert all records into Bronze tables (raw, immutable)
2. Run dedup on patients (phone number match against silver.patients)
3. Validate scans (quality, dedup, rescan eligibility at province level)
4. Promote validated records to Silver tables
5. Update Gold aggregates (population cohorts, incentive ledger)
6. Return: sync receipt with accepted/rejected counts + reasons

**Critical endpoint: outbreak_analyze**
Triggered periodically (timer trigger, every 6 hours) or on-demand:
1. Query Silver scans from last 7 days, grouped by barangay
2. Calculate rolling averages: RR, SpO2, fever symptom rate
3. Compare to 30-day baseline
4. If deviation exceeds threshold → create/update Gold outbreak_signal
5. Call Anthropic Claude API with signal data:
   ```
   System: You are a Philippine public health epidemiologist. Analyze the following population vital signs data and syndromic surveillance signals for Quezon Province.
   User: [structured signal data + context]
   ```
6. Store Claude response as recommended_actions in outbreak_signal
7. Push alert to PHO dashboard

### Step 10.3: Backend requirements.txt
```
azure-functions==1.21.3
psycopg2-binary==2.9.10
pydantic==2.10.4
PyJWT==2.10.1
anthropic==0.42.0
python-dateutil==2.9.0
```

---

## PHASE 11: DEMO / TEST MODE

### Step 11.1: DevMenuScreen.kt

**Access**: Long-press on app version text in Settings screen (5 taps if version text not visible). Only available when BuildConfig.DEMO_MODE_AVAILABLE == true.

**Layout:**

**Toggle 1: Dedup Bypass**
- Material 3 Switch
- When ON: Yellow banner "DEMO MODE — Dedup Disabled" shown at top of every screen
- Stored in DataStore

**Toggle 2: Test Patient Generator**
- "Generate Test Patient" button
- Creates patient with:
  - Phone: 0917000XXXX (sequential counter stored in DataStore)
  - Name: Random from Filipino name lists (100 first names × 100 last names pre-loaded)
  - Barangay: Random from barangays table
  - Age: Random 18-75
  - Sex: Random 50/50
  - Pregnant: If female and 15-49, 15% probability
- "Generate 10 Patients" button for bulk testing
- "Generate 50 Patients" button for volume testing

**Toggle 3: Reset Demo Data**
- "Reset All Data" button
- Confirmation dialog: "This will delete all patients, scans, surveys, referrals, claims, and incentive records. This cannot be undone."
- On confirm: Drop and recreate all Room tables, reset DataStore counters

**Note on scanning during testing:** Every scan uses the real BiosenseSignal SDK. There is no simulated scan mode. The 3-minute cooldown between scans applies during testing. For the test patient generator, generated patients still require a real SDK scan of a real face — the generator creates demographics only. Plan testing sessions accordingly.

---

## BUILD SEQUENCE (Execute in this order)

### Sprint 1 (Weeks 1-2): Foundation
1. Project scaffolding (Step 0.1 - 0.3)
2. Theme + design system (Phase 2)
3. Room database with all entities and DAOs (Phase 1)
4. Hilt dependency injection modules
5. Navigation graph with placeholder screens

### Sprint 2 (Weeks 2-3): Core Flow
6. Patient registration screen (structured inputs, barangay dropdown)
7. Dedup engine (CheckDuplicateUseCase)
8. Scan screen with CameraX + BiosenseSignal SDK
9. Scan result screen with risk flagging
10. BHW home screen dashboard

### Sprint 3 (Weeks 3-5): Surveys + Incentives
11. All four survey screens (NCD, Infectious, Maternal, Mental Health)
12. Rescan eligibility engine (Phase 5)
13. BHW incentive tracker + daily cap (Phase 6)
14. Demo/test mode developer menu (Phase 11)

### Sprint 4 (Weeks 5-7): Clinical + Referrals
15. Three-tier referral system (Phase 8)
16. Voice input components (GoVoiceInput, VoiceRecognitionManager)
17. Clinical encounter screen with voice (Phase 7)
18. Lab results entry screen
19. Maternal clinical entry screen
20. YAKAP claim generation

### Sprint 5 (Weeks 7-9): Backend + Sync
21. PostgreSQL migrations (Phase 10.1)
22. Azure Functions API endpoints (Phase 10.2)
23. Ktor client + API service layer
24. SyncWorker + SyncManager (Phase 9)
25. Conflict resolution

### Sprint 6 (Weeks 9-11): Intelligence + Polish
26. Outbreak detection engine (server-side)
27. Claude AI integration (outbreak analysis + natural language query)
28. BHS dashboard screen
29. RHU dashboard screen
30. End-to-end testing with real SDK scans

---

## CRITICAL BUSINESS RULES (Must be enforced in code)

1. **BHW daily cap**: 50 validated scans = ₱150.00 max. App BLOCKS new scans after cap.
2. **Dedup**: Phone number is primary key in demo. Duplicate scan within non-eligible window = ₱0.00.
3. **Rescan eligibility**: Exact decision tree in Phase 5. No exceptions.
4. **High-risk thresholds**: Loaded from ConfigThresholdEntity, NOT hardcoded. Server can update.
5. **Referral 48-hour window**: If referral not resolved in 48 hours, status = "overdue", escalation triggered.
6. **Scan minimum duration**: 30 seconds enforced. Scans under 30 seconds are auto-rejected.
7. **Signal quality**: Scans below quality threshold are auto-rejected. BHW prompted to rescan.
8. **Offline-first**: EVERY feature must work without internet. Sync is additive, not required.
9. **Data sovereignty**: All patient data stored locally on device AND synced to Azure PostgreSQL. Province owns the data.
10. **YAKAP claims**: Generated in READY status only. Not submitted until PhilHealth API integration (Phase 3 of the real project, not this build).

---

## TESTING REQUIREMENTS

1. **Unit tests** for every UseCase (domain layer)
2. **Unit tests** for dedup logic, rescan eligibility, incentive cap, high-risk flagging
3. **Compose UI tests** for survey screens (verify all inputs produce correct data)
4. **Integration tests** for Room database (insert → query → verify)
5. **Flow tests** with Turbine for all reactive data streams

---

## PHASE 12: BIOSENSESIGNAL SDK INTEGRATION (Edge Processing)

**Reference**: https://developer.biosensesignal.com/android/5.11/

The BiosenseSignal SDK v5.11 is the sole scan engine in this build. It processes PPG video from the front camera entirely on-device — no internet required during the scan. This is critical for offline-first field deployment.

### Step 12.1: SDK Setup

**Integration** (per BiosenseSignal docs):
- Obtain SDK AAR and license key from BiosenseSignal support
- Add AAR to `app/libs/` directory
- Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(files("libs/biosensesignal-sdk-5.11.aar"))
}
```

**License activation**:
- SDK requires ONE internet connection on first launch per device to validate the license key
- After first activation, SDK works fully offline
- License server: `https://licensing-api.biosensesignal.com`
- **CRITICAL for Philippines**: BiosenseSignal notes Cloudflare routing issues in some regions. If license server is unreachable, contact their support for regional workaround. Test this EARLY in deployment planning.
- Store license activation state in DataStore so app knows if device is pre-activated

**Permissions** (add to AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />  <!-- license activation only -->
```

### Step 12.2: SDK Architecture — Edge Processing

```
┌──────────────────────────────────────────────────────────┐
│                    ON-DEVICE (Edge)                        │
│                                                           │
│  Camera (CameraX) → BiosenseSignal SDK → Vital Signs     │
│       ↓                    ↓                    ↓         │
│  Face framing guide   PPG extraction      34 biomarkers   │
│  (ML Kit overlay)     100% on-device      stored in Room  │
│                       NO cloud calls                      │
│                                                           │
│  Survey data → Room DB → WorkManager                      │
│                            ↓                              │
│                    Queued for sync                         │
│                    (hours or days)                         │
└──────────────────────────────────────────────────────────┘
                         ↓ (when WiFi available)
┌──────────────────────────────────────────────────────────┐
│                    AZURE BACKEND                          │
│                                                           │
│  Sync Upload → Bronze Layer → Silver (dedup/validate)     │
│                                  → Gold (analytics)       │
│                                  → Outbreak detection     │
│                                  → Claude AI              │
└──────────────────────────────────────────────────────────┘
```

**Key principle**: The BHW's phone is the edge compute node. Everything that matters — the scan, the biomarker extraction, the survey, the dedup check, the incentive tracking, the referral generation — happens ON DEVICE with zero connectivity. The Azure backend only receives data when the device eventually reaches WiFi — could be hours, could be 2 days. This is by design.

### Step 12.3: BiosenseSignal Face Session Integration

**Create**: `data/sdk/BiosenseScanEngine.kt`

This class wraps the BiosenseSignal SDK. It is the sole implementation of the `ScanEngine` interface — there is no simulated scan mode.

```kotlin
interface ScanEngine {
    suspend fun startSession(config: ScanConfig): Flow<ScanSessionState>
    fun stopSession()
}

sealed class ScanSessionState {
    object Initializing : ScanSessionState()
    data class Measuring(val progress: Float, val timeRemaining: Int) : ScanSessionState()
    data class ImageValidity(val isValid: Boolean, val guidance: String?) : ScanSessionState()
    data class VitalSignUpdate(val vitalSign: String, val value: Double, val confidence: Double) : ScanSessionState()
    data class Complete(val biomarkers: Biomarkers) : ScanSessionState()
    data class Error(val code: String, val message: String) : ScanSessionState()
}

data class ScanConfig(
    val measurementDuration: Int = 50,    // seconds (SDK recommends 50s for full Wellness Index)
    val enabledVitalSigns: List<String>,  // which vitals to measure
    val userInfo: UserInfo?               // age, sex, weight, height — improves accuracy
)
```

**BiosenseSignal SDK vital signs → ScanEntity field mapping (1:1, no interpretation):**

| # | BiosenseSignal Vital Sign | ScanEntity Field | Type | Unit |
|---|--------------------------|-----------------|------|------|
| 1 | Pulse Rate | pulseRate | Double | bpm |
| 2 | Blood Pressure (systolic) | bpSystolic | Double | mmHg |
| 3 | Blood Pressure (diastolic) | bpDiastolic | Double | mmHg |
| 4 | Mean Arterial Pressure | meanArterialPressure | Double | mmHg |
| 5 | Pulse Pressure | pulsePressure | Double | mmHg |
| 6 | Cardiac Workload | cardiacWorkload | Double | index |
| 7 | Heart Age | heartAge | Double | years |
| 8 | Respiration Rate | respirationRate | Double | breaths/min |
| 9 | Oxygen Saturation | oxygenSaturation | Double | % |
| 10 | Hemoglobin | hemoglobin | Double | g/dL |
| 11 | Hemoglobin A1c | hemoglobinA1c | Double | % |
| 12 | ASCVD Risk | ascvdRisk | Double | score |
| 13 | ASCVD Risk Level | ascvdRiskLevel | String | category |
| 14 | High Blood Pressure Risk | highBloodPressureRisk | Double | score |
| 15 | High Fasting Glucose Risk | highFastingGlucoseRisk | Double | score |
| 16 | High Hemoglobin A1c Risk | highHemoglobinA1cRisk | Double | score |
| 17 | High Total Cholesterol Risk | highTotalCholesterolRisk | Double | score |
| 18 | Low Hemoglobin Risk | lowHemoglobinRisk | Double | score |
| 19 | Mean RRi | meanRri | Double | ms |
| 20 | RRi | rri | Double | ms |
| 21 | SDNN | sdnn | Double | ms |
| 22 | RMSSD | rmssd | Double | ms |
| 23 | SD1 | sd1 | Double | ms |
| 24 | SD2 | sd2 | Double | ms |
| 25 | PRQ | prq | Double | ratio |
| 26 | LF/HF | lfhf | Double | ratio |
| 27 | PNS Index | pnsIndex | Double | index |
| 28 | PNS Zone | pnsZone | String | category |
| 29 | SNS Index | snsIndex | Double | index |
| 30 | SNS Zone | snsZone | String | category |
| 31 | Stress Level | stressLevel | String | category |
| 32 | Stress Index | stressIndex | Double | index |
| 33 | Normalized Stress Index | normalizedStressIndex | Double | index |
| 34 | Wellness Index | wellnessIndex | Double | 0-100 |
| 35 | Wellness Level | wellnessLevel | String | category |

**Total: 35 fields (Blood Pressure splits into systolic + diastolic = 34 SDK vital signs, 35 stored fields)**
**All 35 fields are reported, stored, synced, and displayed. Nothing is discarded.**

### Step 12.4: Scan Screen Updates for SDK

`ScanScreen.kt` uses the real BiosenseSignal SDK for every scan:

- 50-second measurement (SDK recommended for full Wellness Index)
- CameraX preview feeds frames to BiosenseSignal SDK
- SDK provides real-time `ImageValidity` feedback:
  - Face not detected → "Position your face in the oval"
  - Too much movement → "Hold still"
  - Poor lighting → "Move to better lighting"
  - Signal quality low → "Hold steady, measuring..."
- Real-time vital sign updates shown as they compute (some arrive before others)
- 3-minute cooldown enforced between scans (SDK device overheating protection):
  - After scan complete, "Next scan available in X:XX" countdown
  - Does NOT count against BHW daily cap — this is a device protection, not a business rule
  - At 50 scans/day with 3-min cooldowns = ~2.5 hours of active scanning per day. Feasible.

**Hilt injection — single implementation:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ScanModule {
    @Provides
    @Singleton
    fun provideScanEngine(
        @ApplicationContext context: Context
    ): ScanEngine {
        return BiosenseScanEngine(context)  // Always real SDK. No simulator.
    }
}
```

### Step 12.5: Offline Sync Queue

The sync system from Phase 9 already handles delayed upload. Additional considerations for multi-day offline:

**Local storage capacity planning:**
- Each scan record: ~4-6 KB
- 50 scans/day × 2 days offline = 100 scans = ~600 KB
- With surveys: ~1.5 MB for 2 days of work
- Room database on a 2GB RAM phone can easily handle weeks of offline data

**Sync priority when connectivity returns:**
1. CRITICAL: High-risk referrals (may need immediate BHS/RHU visibility)
2. HIGH: Validated scans + surveys (feed outbreak detection)
3. MEDIUM: Patient registrations
4. LOW: Incentive records, claims

**Add to SyncWorker.kt:**
```kotlin
// When connectivity detected after extended offline period
// Sort upload queue by priority, not just creation order
val uploadQueue = buildUploadQueue(
    referrals = referralDao.getPendingSync().sortedBy { if (it.riskFlags.isNotEmpty()) 0 else 1 },
    scans = scanDao.getPendingSync().sortedBy { it.scannedAt },
    surveys = surveyDao.getPendingSync(),
    patients = patientDao.getPendingSync(),
    incentives = incentiveDao.getPendingSync()
)
```

**Stale data handling:**
- On sync, server returns updated patient records (other BHWs may have scanned the same person)
- Merge strategy: union of risk flags, latest scan date wins, latest pregnant status wins
- If server-side dedup rejects a scan the BHW already got paid for locally, flag for reconciliation (not auto-deducted — handled in RN/MedTech operations)

---

## PHASE 13: FACEBOOK MESSENGER PATIENT ENGAGEMENT

### Purpose

After a BHW scans a resident, that person wants to know: What do my results mean? What should I do? The app sends a structured summary to the patient's Facebook Messenger. This is the right channel because Messenger penetration in the Philippines is near-universal — far higher than email, SMS apps, or any other messaging platform.

### Architecture

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  GO Android App   │────▶│  Azure Backend    │────▶│  Facebook        │
│                   │     │                   │     │  Messenger API   │
│  Scan completes   │     │  Generate health  │     │                  │
│  Patient opts in  │     │  summary message  │     │  Deliver to      │
│  Send trigger     │     │  via Claude AI    │     │  patient's       │
│                   │     │  Personalized     │     │  Messenger       │
│                   │     │  + actionable     │     │                  │
└──────────────────┘     └──────────────────┘     └──────────────────┘
```

**This is NOT a chatbot.** It is a one-way (initially) structured health summary delivered via Messenger. Future phases can add interactive Q&A via Claude AI.

### Step 13.1: Patient Consent & Messenger Opt-In

**Add to Patient Registration (RegistrationScreen.kt):**

| Field | Component | Required |
|-------|-----------|----------|
| Facebook Messenger opt-in | GoSegmentedButton | Yes — "Would you like to receive your health results via Messenger?" Yes / No |
| Messenger contact method | GoSegmentedButton | If opted in: "Phone Number" / "Facebook Name" |
| Messenger phone or name | Text field | The phone number linked to their Messenger account OR their Facebook display name |

**Add to PatientEntity.kt:**
```kotlin
val messengerOptIn: Boolean = false,
val messengerContactMethod: String? = null,   // "phone" | "facebook_name"
val messengerContactValue: String? = null,     // phone number or FB name
val messengerPsid: String? = null,             // Facebook Page-Scoped ID (resolved on first message)
```

**Privacy & consent:**
- Consent must be explicit and recorded with timestamp
- Patient can opt out at any time (BHW updates record)
- Message content never includes full name + all vitals together — uses first name only
- Compliant with RA 10173 (Data Privacy Act) — documented consent for data sharing via third-party platform

### Step 13.2: Facebook Page & Messenger Platform Setup

**Prerequisites (one-time setup by GO):**
1. Create a Facebook Page for "Global Outcomes Health" (or province-specific page)
2. Register as Facebook Developer at developers.facebook.com
3. Create a Facebook App with Messenger product enabled
4. Generate Page Access Token (long-lived)
5. Subscribe to messaging webhooks
6. Submit for Messenger Platform review (required for sending messages to users who haven't messaged the page first — request `pages_messaging` permission)

**Alternative for MVP/Demo: Customer Matching API**
- Facebook's Customer Matching lets you send messages to users by phone number if they have that number linked to their Messenger account
- This avoids requiring patients to message the page first
- Requires `pages_messaging` approval from Facebook

### Step 13.3: Azure Backend — Message Generation

**Create Azure Function: `messenger_send_results/`**

**Trigger:** Called by `sync_upload` function after scan is validated and promoted to Silver layer, if patient.messengerOptIn = true.

**Message generation flow:**
1. Retrieve patient scan results from Silver layer
2. Call Anthropic Claude API to generate personalized health summary:

```python
prompt = f"""
You are a friendly community health advisor in the Philippines.
Write a short Messenger message for {patient.first_name} about their health screening results.

Language: Mix of simple English and Filipino (Taglish) based on what's natural.
Tone: Warm, encouraging, clear. Like a caring neighbor who happens to be a nurse.
Length: Maximum 300 words. Short paragraphs. Use emoji sparingly but naturally.

Results:
- Blood Pressure: {scan.bp_systolic}/{scan.bp_diastolic} mmHg
- Heart Rate: {scan.pulse_rate} bpm
- Oxygen Level: {scan.oxygen_saturation}%
- Stress Level: {scan.stress_level}
- Wellness Index: {scan.wellness_index}
- Overall Risk: {risk_classification}
- Risk Indicators: BP Risk: {scan.high_blood_pressure_risk}, Glucose Risk: {scan.high_fasting_glucose_risk}, A1c Risk: {scan.high_hemoglobin_a1c_risk}, Cholesterol Risk: {scan.high_total_cholesterol_risk}, Hemoglobin Risk: {scan.low_hemoglobin_risk}
{f"- HIGH RISK FLAGS: {scan.risk_flags}" if scan.risk_flags else ""}
{f"- Pregnancy Status: {patient.gestational_age_weeks} weeks" if patient.pregnant else ""}

Structure:
1. Greeting with first name
2. What their results mean in plain language (not medical jargon)
3. What's good (always find something positive first)
4. What needs attention (if anything) — be direct but kind
5. One specific action they should take this week
6. If HIGH-RISK: Emphasize urgency of visiting the health station within 48 hours
7. Reminder that this is screening, not diagnosis — see a healthcare provider for concerns

Do NOT include: last name, phone number, full address, or any identifying information beyond first name.
"""
```

3. Format Claude's response for Messenger API
4. Send via Facebook Send API

### Step 13.4: Messenger Message Templates

**Normal results message structure:**
```json
{
  "recipient": { "id": "<PSID>" },
  "messaging_type": "MESSAGE_TAG",
  "tag": "CONFIRMED_EVENT_UPDATE",
  "message": {
    "text": "[Claude-generated personalized summary]"
  }
}
```

**High-risk results — add quick reply buttons:**
```json
{
  "recipient": { "id": "<PSID>" },
  "message": {
    "text": "[Claude-generated HIGH-RISK summary with urgency]",
    "quick_replies": [
      {
        "content_type": "text",
        "title": "📍 Find nearest BHS",
        "payload": "FIND_BHS"
      },
      {
        "content_type": "text",
        "title": "📞 Call my RHU",
        "payload": "CALL_RHU"
      },
      {
        "content_type": "text",
        "title": "ℹ️ What does high-risk mean?",
        "payload": "EXPLAIN_HIGH_RISK"
      }
    ]
  }
}
```

**Follow-up educational messages (sent 24-48 hours after scan):**
Based on scan results, send ONE targeted health education message:
- If elevated BP: "Understanding Blood Pressure — What You Can Do Today"
- If low SpO2: "When to Seek Help for Breathing Issues"
- If high stress: "Simple Stress Management for Your Health"
- If pregnant + any risk: "Keeping You and Your Baby Safe — Your Next Steps"
- If NCD risk factors: "Small Changes That Protect Your Health"

These are pre-written templates (not Claude-generated) reviewed by clinical advisors. Claude personalizes the intro, but the core health education content is validated.

### Step 13.5: Webhook Handler for Responses

**Create Azure Function: `messenger_webhook/`**

When a patient replies to a message or taps a quick reply:

| Payload | Response |
|---------|----------|
| FIND_BHS | Send Google Maps link to nearest BHS based on patient's barangay |
| CALL_RHU | Send phone number of the RHU serving their municipality |
| EXPLAIN_HIGH_RISK | Send pre-written explanation of what high-risk means + what to expect at the health station |
| Free text question | Route to Claude AI for health education response (NOT diagnosis). Claude has patient's scan context. Response always ends with "Visit your health station for a full evaluation." |

**Safety guardrails for Claude AI responses via Messenger:**
- NEVER provide diagnosis
- NEVER recommend specific medications
- NEVER contradict a healthcare provider's advice
- ALWAYS recommend visiting a healthcare provider for concerns
- ALWAYS use simple, accessible language
- Responses capped at 300 words
- If question is outside health scope: "I can only help with health-related questions from your screening. For other questions, please visit your health station."

### Step 13.6: Backend Requirements Update

Add to `backend/requirements.txt`:
```
requests==2.32.3        # Facebook Graph API calls
```

Add to Azure Functions environment config:
```
FACEBOOK_PAGE_ACCESS_TOKEN=<long-lived token>
FACEBOOK_APP_SECRET=<app secret for webhook verification>
FACEBOOK_VERIFY_TOKEN=<custom verify token for webhook setup>
```

**New API endpoints:**
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/messenger/send-results` | POST | Generate and send scan results to patient via Messenger |
| `/api/messenger/webhook` | GET | Facebook webhook verification |
| `/api/messenger/webhook` | POST | Receive and process patient replies |
| `/api/messenger/opt-out` | POST | Process patient opt-out request |

### Step 13.7: Android App Updates

**PatientEntity:** Add messenger fields (Step 13.1)

**RegistrationScreen:** Add Messenger opt-in section after contact info, before pregnancy status.

**ScanResultScreen:** After scan completes, if patient.messengerOptIn = true:
- Show small confirmation: "Results will be sent to [patient first name]'s Messenger ✓"
- This message appears below the scan results, non-intrusive
- The actual send happens server-side after sync — not from the device

**Patient Detail Screen:** Show Messenger status:
- "Messenger: Opted in ✓" or "Messenger: Not opted in"
- "Last message sent: [date]" if applicable
- "Opt out" button for BHW to update on patient's behalf

---

## UPDATED BUILD SEQUENCE

### Sprint 1 (Weeks 1-2): Foundation
1. Project scaffolding (Phase 0)
2. Theme + design system (Phase 2)
3. Room database with all entities and DAOs (Phase 1) — **including messenger fields**
4. Hilt dependency injection modules
5. Navigation graph with placeholder screens

### Sprint 2 (Weeks 2-3): Core Flow
6. Patient registration screen — **including Messenger opt-in**
7. Dedup engine
8. Scan screen with CameraX + BiosenseSignal SDK
9. Scan result screen with risk flagging
10. BHW home screen dashboard

### Sprint 3 (Weeks 3-5): Surveys + Incentives
11. All four survey screens
12. Rescan eligibility engine (Phase 5)
13. BHW incentive tracker + daily cap (Phase 6)
14. Demo/test mode developer menu (Phase 11)

### Sprint 4 (Weeks 5-7): Clinical + Referrals
15. Three-tier referral system (Phase 8)
16. Voice input components (Phase 7)
17. Clinical encounter screen with voice
18. Lab results + maternal clinical entry
19. YAKAP claim generation

### Sprint 5 (Weeks 7-9): Backend + Sync + Messenger
20. PostgreSQL migrations (Phase 10)
21. Azure Functions API endpoints (Phase 10)
22. Ktor client + API service layer
23. SyncWorker + SyncManager (Phase 9) — **including priority queue for offline sync**
24. Conflict resolution
25. Facebook Messenger integration (Phase 13) — **send results + webhook handler**

### Sprint 6 (Weeks 9-12): SDK Integration + Intelligence + Polish
26. BiosenseSignal SDK optimization + edge case handling (Phase 12)
27. Outbreak detection engine (server-side)
28. Claude AI integration (outbreak analysis + Messenger response generation)
29. BHS + RHU dashboard screens
30. End-to-end testing with real SDK scans

---

## NOTES FOR CLAUDE CODE

### GitHub Repository Setup

Before writing any code, initialize the repository:

```bash
git init
git remote add origin https://github.com/GlobalOutcomesInc/phi-platform.git
git checkout -b main
```

**Branch strategy:**
- `main` — production-ready, only merged from develop after full sprint completion
- `develop` — integration branch, all feature branches merge here
- `feature/phase-XX-description` — one branch per phase (e.g., `feature/phase-01-data-layer`)

**Commit convention:**
```
[PHASE-XX] STEP X.X: Brief description

- What was implemented
- What was tested
- Any known issues or TODOs
```

**After completing each Phase:** merge feature branch → develop, tag the merge commit:
```bash
git tag -a vX.X.0-phaseXX -m "Phase XX complete: [description]"
git push origin develop --tags
```

**After completing each Sprint:** merge develop → main, tag as sprint release:
```bash
git tag -a vX.X.0-sprintX -m "Sprint X complete: [phases included]"
git push origin main --tags
```

---

### SDLC Documentation Requirements

Claude Code must maintain a living SDLC document at the root of the repository: `docs/SDLC.md`. This document is updated after EVERY phase completion. It is the project's official record of what has been built, tested, and delivered.

Additionally, each phase gets its own detailed document in `docs/phases/`.

**Directory structure:**
```
docs/
├── SDLC.md                              # Master SDLC tracker (updated every phase)
├── ARCHITECTURE.md                       # System architecture overview
├── API_REFERENCE.md                      # Azure Functions API documentation
├── DATABASE_SCHEMA.md                    # PostgreSQL schema documentation
├── phases/
│   ├── PHASE_00_SCAFFOLDING.md
│   ├── PHASE_01_DATA_LAYER.md
│   ├── PHASE_02_DESIGN_SYSTEM.md
│   ├── PHASE_03_SCAN_MODULE.md
│   ├── PHASE_04_SURVEYS.md
│   ├── PHASE_05_RESCAN_ENGINE.md
│   ├── PHASE_06_INCENTIVE_TRACKER.md
│   ├── PHASE_07_CLINICAL_VOICE.md
│   ├── PHASE_08_REFERRAL_SYSTEM.md
│   ├── PHASE_09_SYNC_OFFLINE.md
│   ├── PHASE_10_AZURE_BACKEND.md
│   └── PHASE_11_DEMO_MODE.md
└── testing/
    ├── TEST_PLAN.md                      # Master test plan
    ├── TEST_RESULTS_SPRINT_01.md
    ├── TEST_RESULTS_SPRINT_02.md
    ├── TEST_RESULTS_SPRINT_03.md
    ├── TEST_RESULTS_SPRINT_04.md
    ├── TEST_RESULTS_SPRINT_05.md
    └── TEST_RESULTS_SPRINT_06.md
```

---

### Master SDLC Document (docs/SDLC.md)

Claude Code must create and maintain this file with the following structure. After each phase is completed, update the relevant row and add the completion entry.

```markdown
# Global Outcomes — Population Health Intelligence Platform
# Software Development Lifecycle (SDLC) Tracker

## Project Overview
- **Project**: GO PHI Platform — Android App + Azure Backend
- **Repository**: https://github.com/GlobalOutcomesInc/phi-platform
- **Start Date**: [date of first commit]
- **Current Sprint**: [Sprint N]
- **Current Phase**: [Phase XX]
- **Last Updated**: [timestamp]

## Executive Summary
[2-3 sentences on current project status, what's working, what's next]

---

## Phase Completion Matrix

| Phase | Description | Status | Branch | Tag | Started | Completed | Tests Pass | Docs |
|-------|-------------|--------|--------|-----|---------|-----------|------------|------|
| 0 | Project Scaffolding | ✅ Complete | feature/phase-00 | v0.1.0-phase00 | YYYY-MM-DD | YYYY-MM-DD | N/A | ✅ |
| 1 | Core Data Layer | ✅ Complete | feature/phase-01 | v0.2.0-phase01 | YYYY-MM-DD | YYYY-MM-DD | 24/24 | ✅ |
| 2 | Theme & Design System | 🔄 In Progress | feature/phase-02 | — | YYYY-MM-DD | — | — | — |
| 3 | Scan Module | ⬚ Not Started | — | — | — | — | — | — |
| ... | ... | ... | ... | ... | ... | ... | ... | ... |

## Sprint Tracker

| Sprint | Phases | Status | Started | Completed | Git Tag |
|--------|--------|--------|---------|-----------|---------|
| 1 | 0, 1, 2 | 🔄 In Progress | YYYY-MM-DD | — | — |
| 2 | 3, 4 | ⬚ Not Started | — | — | — |
| 3 | 5, 6, 11 | ⬚ Not Started | — | — | — |
| 4 | 7, 8 | ⬚ Not Started | — | — | — |
| 5 | 9, 10 | ⬚ Not Started | — | — | — |
| 6 | Polish + E2E | ⬚ Not Started | — | — | — |

---

## Change Log
[Reverse chronological — newest first]

### [YYYY-MM-DD] Phase XX Complete
- **What was built**: [summary]
- **Files created/modified**: [count]
- **Tests written**: [count passing / count total]
- **Known issues**: [list or "None"]
- **Git tag**: [tag]
- **Next**: [what's coming in next phase]
```

---

### Per-Phase Documentation (docs/phases/PHASE_XX_*.md)

After completing each phase, Claude Code must create a detailed phase document covering all SDLC stages. Use this exact template:

```markdown
# Phase [XX]: [Phase Name]

## 1. Requirements Traceability

| Requirement ID | Description | Source | Implemented In | Verified By |
|---------------|-------------|--------|----------------|-------------|
| REQ-XX-001 | [requirement] | BUILD_INSTRUCTIONS Phase XX, Step X.X | [file path] | [test name or manual] |
| REQ-XX-002 | ... | ... | ... | ... |

Every requirement from the BUILD_INSTRUCTIONS for this phase must appear in this table.

## 2. Design Decisions

| Decision | Options Considered | Choice Made | Rationale |
|----------|-------------------|-------------|-----------|
| [decision point] | [options] | [chosen option] | [why] |

Document any architectural or implementation decisions made during this phase that deviated from or extended the build instructions.

## 3. Implementation Summary

### Files Created
| File | Purpose | Lines of Code |
|------|---------|---------------|
| [path] | [what it does] | [LOC] |

### Files Modified
| File | Change Description |
|------|-------------------|
| [path] | [what changed and why] |

### Dependencies Added
| Dependency | Version | Purpose |
|-----------|---------|---------|
| [library] | [version] | [why needed] |

## 4. Business Logic Verification

For phases with business rules (Phases 5, 6, 7, 8 especially), document verification:

| Business Rule | Rule Description | Implementation | Test Coverage |
|--------------|-----------------|----------------|---------------|
| BR-XX-001 | [rule from BUILD_INSTRUCTIONS] | [how it's enforced in code] | [test class::method] |

## 5. Testing

### Unit Tests
| Test Class | Tests | Pass | Fail | Coverage |
|-----------|-------|------|------|----------|
| [class] | [N] | [N] | [N] | [%] |

### UI Tests (if applicable)
| Test Class | Tests | Pass | Fail |
|-----------|-------|------|------|
| [class] | [N] | [N] | [N] |

### Manual Test Cases (if applicable)
| Test Case | Steps | Expected Result | Actual Result | Status |
|-----------|-------|-----------------|---------------|--------|
| [TC-XX-001] | [steps] | [expected] | [actual] | ✅/❌ |

## 6. Known Issues & Technical Debt

| Issue | Severity | Description | Planned Resolution |
|-------|----------|-------------|-------------------|
| [ID] | Low/Med/High | [description] | [when/how] |

## 7. Screenshots / Evidence

[For UI phases: include descriptions of screens built. Claude Code should note where screenshot placeholders should go — actual screenshots taken during manual QA.]

## 8. Deployment Notes

- **Branch**: feature/phase-XX-[name]
- **Merged to**: develop
- **Tag**: vX.X.0-phaseXX
- **Build verified**: Yes/No
- **APK size impact**: +X.X MB
```

---

### Git Workflow Per Phase

Claude Code must follow this exact workflow for every phase:

```bash
# 1. Start phase
git checkout develop
git pull origin develop
git checkout -b feature/phase-XX-description

# 2. Work on phase (commit frequently)
git add .
git commit -m "[PHASE-XX] STEP X.X: Description"

# 3. After all steps in phase complete, run tests
./gradlew test
./gradlew connectedAndroidTest  # if UI tests exist

# 4. Write phase documentation
# Create/update docs/phases/PHASE_XX_*.md
# Update docs/SDLC.md

# 5. Commit documentation
git add docs/
git commit -m "[PHASE-XX] SDLC documentation complete"

# 6. Merge to develop
git checkout develop
git merge feature/phase-XX-description
git tag -a vX.X.0-phaseXX -m "Phase XX complete: [summary]"
git push origin develop --tags

# 7. After sprint complete (all phases in sprint done)
git checkout main
git merge develop
git tag -a vX.X.0-sprintX -m "Sprint X: [phases included]"
git push origin main --tags
```

### Continuous Documentation Rule

**CRITICAL**: Claude Code must NEVER complete a phase without:
1. All tests passing
2. Phase document written (docs/phases/PHASE_XX_*.md)
3. Master SDLC.md updated
4. Changes committed and pushed to GitHub
5. Feature branch merged to develop with tag

If tests fail, document the failures in the phase document under "Known Issues" and note them in SDLC.md. Do not merge to develop until critical tests pass. Non-critical test failures can be documented and deferred.

---

- When writing Compose UI, ALWAYS use Material 3 components (import from `androidx.compose.material3`). NEVER use Material 2.
- When writing Kotlin, use coroutines and Flows for ALL async work. No callbacks, no RxJava.
- Every screen must have a ViewModel. Every ViewModel must expose state as StateFlow.
- Every database operation must go through a Repository. ViewModels never access DAOs directly.
- Use sealed classes for UI state: Loading, Success, Error pattern.
- All monetary values stored as Double with 2 decimal precision. Display formatted as "₱X,XXX.XX".
- All dates stored as Long (epoch milliseconds). Display formatted using kotlinx-datetime.
- All JSON stored in Room as String. Use Kotlinx Serialization for parsing.
- Lottie files in res/raw/ — create placeholder JSON files with TODO comments indicating the animation needed. The actual Lottie animations will be sourced separately.
- For the 1,209 barangays: create a seed file (quezon_barangays.csv) with at minimum 50 sample barangays across 10 municipalities for demo. Full list will be loaded in production.
