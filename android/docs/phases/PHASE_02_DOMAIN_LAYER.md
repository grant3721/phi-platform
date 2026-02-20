# Phase 2: Domain Layer & Use Cases - COMPLETE ✅

**Completed:** February 19, 2026
**Duration:** ~1.5 hours
**Status:** Domain models, repositories, and core use cases implemented

---

## Requirements Traceability

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Create Patient domain model | ✅ | `domain/model/Patient.kt` |
| Create Scan domain model | ✅ | `domain/model/Scan.kt` |
| Create Barangay domain model | ✅ | `domain/model/Barangay.kt` |
| Create PatientRepository interface | ✅ | `domain/repository/PatientRepository.kt` |
| Create ScanRepository interface | ✅ | `domain/repository/ScanRepository.kt` |
| Implement PatientRepositoryImpl | ✅ | `data/repository/PatientRepositoryImpl.kt` |
| Implement ScanRepositoryImpl | ✅ | `data/repository/ScanRepositoryImpl.kt` |
| Create RegisterPatientUseCase | ✅ | `domain/usecase/RegisterPatientUseCase.kt` |
| Create CheckDuplicateUseCase | ✅ | `domain/usecase/CheckDuplicateUseCase.kt` |
| Create CheckRescanEligibilityUseCase | ✅ | `domain/usecase/CheckRescanEligibilityUseCase.kt` |
| Create CheckDailyCapUseCase | ✅ | `domain/usecase/CheckDailyCapUseCase.kt` |
| Configure Hilt DI for repositories | ✅ | `di/RepositoryModule.kt` |

---

## Files Created

### Domain Models (3 files)

**1. Patient.kt**
- Business logic representation of patient
- Computed properties: `fullName`, `age`, `isHighRisk`
- Method: `needsSync()` for sync status
- Enums: `Sex`, `SyncStatus`

**2. Scan.kt**
- All 34 vital signs from BiosenseSignal SDK
- Computed properties: `isHighRisk`, `isValidQuality`
- Method: `needsSync()`
- Enums: `RiskLevel`, `SignalQuality`

**3. Barangay.kt**
- Location hierarchy model
- Computed property: `fullLocation`

### Repository Interfaces (2 files)

**PatientRepository.kt** - 11 methods
- `insertPatient()`, `updatePatient()`
- `getPatientById()`, `getPatientByIdFlow()`
- `getPatientByPhoneNumber()` - For deduplication
- `getAllPatientsFlow()`, `getHighRiskPatientsFlow()`, `getPregnantPatientsFlow()`
- `getPatientCount()`, `getHighRiskCount()`
- `updateRiskStatus()`, `deletePatient()`

**ScanRepository.kt** - 10 methods
- `insertScan()`, `getScanById()`, `getScanByIdFlow()`
- `getScansByPatientFlow()`, `getLatestScanForPatient()`
- `getScansSince()` - For rescan eligibility
- `getScansForDay()`, `getScanCountForDay()` - For daily cap
- `getHighRiskScansFlow()`, `deleteScan()`

### Repository Implementations (2 files)

**PatientRepositoryImpl.kt**
- Injects `PatientDao` via Hilt
- Maps `Patient` ↔ `PatientEntity`
- Handles JSON serialization for `highRiskReasons`
- Uses `Result<T>` for error handling
- Flow-based queries for reactive UI

**ScanRepositoryImpl.kt**
- Injects `ScanDao` via Hilt
- Maps `Scan` ↔ `ScanEntity`
- Handles JSON serialization for `highRiskFlags` and `biomarkersFull`
- All 34 vital signs properly mapped
- Uses `Result<T>` for error handling

### Use Cases (4 files)

**1. RegisterPatientUseCase.kt**
- Validates patient data before saving
- Checks required fields (phone, name, barangay)
- Validates pregnant-specific fields (gestational age 0-42 weeks)
- Returns `Result<String>` with patient ID

**2. CheckDuplicateUseCase.kt**
- Checks if patient exists by phone number
- Returns `DuplicateResult` with:
  - `isDuplicate: Boolean`
  - `existingPatient: Patient?`
  - `message: String`
- Prevents duplicate registrations

**3. CheckRescanEligibilityUseCase.kt** ⭐ CRITICAL BUSINESS LOGIC
- Implements exact specification rules:
  - **BASELINE**: New patient, no scans → ✅ Allowed
  - **HIGH_RISK**: Active high-risk flag → ✅ Allowed immediately
  - **MATERNAL_HIGH_RISK**: Pregnant + high-risk → ✅ Allowed immediately
  - **MATERNAL**: Pregnant, 6-month interval → ✅ Allowed
  - **ANNUAL**: 12-month interval → ✅ Allowed
  - **TOO_SOON**: Doesn't meet criteria → ❌ Rejected
- Returns `EligibilityResult` with:
  - `isEligible: Boolean`
  - `reason: EligibilityReason` (enum)
  - `message: String`
  - `daysUntilEligible: Int`
- Uses `TimeUnit` for accurate day calculations

**4. CheckDailyCapUseCase.kt** ⭐ CRITICAL BUSINESS LOGIC
- Implements daily cap rules:
  - ₱3.00 per scan
  - ₱150.00 daily maximum (50 scans)
- Calculates start of day correctly
- Returns `DailyCapResult` with:
  - `scansToday: Int`
  - `amountToday: Float`
  - `isAtCap: Boolean`
  - `remainingScans: Int`
  - `remainingAmount: Float`
  - `message: String`
- Constants: `MAX_SCANS_PER_DAY = 50`, `MAX_DAILY_AMOUNT = 150.00`, `AMOUNT_PER_SCAN = 3.00`

### Dependency Injection (1 file)

**RepositoryModule.kt**
- Binds repository implementations to interfaces
- `@Singleton` scope for efficient memory usage
- Uses `@Binds` for interface → implementation mapping

---

## Design Decisions

### 1. Clean Architecture Separation
- **Decision:** Separate domain models from entities
- **Rationale:** Business logic independent of data layer, easier testing
- **Impact:** Can change database without affecting business rules

### 2. Result<T> for Error Handling
- **Decision:** Use Kotlin `Result<T>` instead of exceptions
- **Rationale:** Explicit error handling, better type safety
- **Impact:** UI layer can handle errors gracefully

### 3. Flow for Reactive Queries
- **Decision:** Repository methods return `Flow<T>` for live data
- **Rationale:** Compose UI automatically updates when data changes
- **Impact:** Reduced boilerplate, automatic UI updates

### 4. JSON Serialization in Repositories
- **Decision:** Repository handles JSON conversion (not entities)
- **Rationale:** Domain layer doesn't know about JSON storage
- **Impact:** Clean domain models, flexible storage format

### 5. Computed Properties on Models
- **Decision:** Domain models have computed properties (`age`, `fullName`, `isHighRisk`)
- **Rationale:** Encapsulate business logic in models
- **Impact:** Consistent calculations, no logic duplication in UI

### 6. Enum for Status Values
- **Decision:** Use enums for `Sex`, `SyncStatus`, `RiskLevel`, `SignalQuality`, `EligibilityReason`
- **Rationale:** Type safety, compile-time checks, exhaustive when expressions
- **Impact:** Fewer bugs, better IDE support

---

## Business Logic Verification

### Patient Deduplication Flow
```kotlin
// In UI/ViewModel:
val duplicateResult = checkDuplicateUseCase("09171234567").getOrNull()

if (duplicateResult?.isDuplicate == true) {
    // Show existing patient, ask if user wants to continue
    showDialog("Patient already exists: ${duplicateResult.existingPatient?.fullName}")
} else {
    // Proceed with registration
    val patientId = registerPatientUseCase(newPatient).getOrThrow()
}
```

### Rescan Eligibility Flow
```kotlin
// Before allowing scan:
val eligibility = checkRescanEligibilityUseCase(patient).getOrNull()

when (eligibility?.reason) {
    EligibilityReason.BASELINE -> {
        // "New patient - baseline scan allowed"
        proceedWithScan()
    }
    EligibilityReason.HIGH_RISK -> {
        // "High-risk patient - immediate rescan allowed"
        proceedWithScan()
    }
    EligibilityReason.TOO_SOON -> {
        // "Must wait 12 months since last scan"
        // "Days until eligible: ${eligibility.daysUntilEligible}"
        showRejectionMessage(eligibility.message, eligibility.daysUntilEligible)
    }
    else -> { /* Handle other reasons */ }
}
```

### Daily Cap Enforcement Flow
```kotlin
// Before recording scan:
val capResult = checkDailyCapUseCase(bhwId).getOrNull()

if (capResult?.isAtCap == true) {
    // "Daily cap reached (₱150.00 / 50 scans)"
    showCapReachedDialog()
    return
}

// Show earnings progress
showEarningsCard(
    scansToday = capResult.scansToday,
    amountToday = capResult.amountToday,
    remainingScans = capResult.remainingScans
)

// After scan validation:
recordIncentiveUseCase(scan) // Will increment daily count
```

---

## Implementation Summary

**Total Files Created:** 12
- 3 Domain models
- 2 Repository interfaces
- 2 Repository implementations
- 4 Use cases
- 1 Hilt DI module

**Lines of Code:** ~1,500+

**Key Achievements:**
- ✅ Clean Architecture enforced (domain layer independent)
- ✅ Business logic encapsulated in use cases
- ✅ Rescan eligibility rules implemented (exact spec)
- ✅ Daily cap logic implemented (₱3/scan, ₱150 cap)
- ✅ Patient deduplication by phone number
- ✅ Type-safe error handling with Result<T>
- ✅ Reactive data with Flow
- ✅ Dependency injection configured

---

## Testing Strategy

### Unit Tests (To be written in Phase 2B)

**CheckRescanEligibilityUseCase Tests:**
```kotlin
@Test
fun `new patient is eligible for baseline scan`() { }

@Test
fun `high-risk patient can rescan immediately`() { }

@Test
fun `pregnant patient must wait 6 months`() { }

@Test
fun `normal patient must wait 12 months`() { }

@Test
fun `calculates days until eligible correctly`() { }
```

**CheckDailyCapUseCase Tests:**
```kotlin
@Test
fun `0 scans today is not at cap`() { }

@Test
fun `50 scans today is at cap`() { }

@Test
fun `calculates remaining amount correctly`() { }

@Test
fun `start of day calculation is correct`() { }
```

**CheckDuplicateUseCase Tests:**
```kotlin
@Test
fun `detects existing patient by phone`() { }

@Test
fun `allows new patient registration`() { }
```

---

## Known Issues

None. Phase 2 domain layer is complete and ready for Phase 3 (UI).

---

## Next Phase: Patient Registration UI

**Phase 3 will create:**
1. RegistrationScreen (Compose UI)
2. RegistrationViewModel (MVI pattern)
3. Navigation setup
4. Form validation
5. Integration with RegisterPatientUseCase and CheckDuplicateUseCase

**Estimated time:** 2-3 hours

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────┐
│              Presentation Layer                 │
│         (ViewModels + Compose UI)               │
│                                                 │
│  ViewModel calls Use Cases                      │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│              Domain Layer ✅                    │
│                                                 │
│  ┌───────────────┐  ┌────────────────────────┐ │
│  │  Use Cases    │  │   Domain Models        │ │
│  │               │  │                        │ │
│  │ - Register    │  │ - Patient              │ │
│  │ - CheckDup    │  │ - Scan                 │ │
│  │ - CheckRescan │  │ - Barangay             │ │
│  │ - CheckCap    │  │                        │ │
│  └───────┬───────┘  └────────────────────────┘ │
│          │                                      │
│  ┌───────▼──────────────────┐                  │
│  │ Repository Interfaces    │                  │
│  │                          │                  │
│  │ - PatientRepository      │                  │
│  │ - ScanRepository         │                  │
│  └──────────────────────────┘                  │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│              Data Layer ✅                      │
│                                                 │
│  ┌────────────────────────┐  ┌──────────────┐  │
│  │ Repository Impls       │  │  Entities    │  │
│  │                        │  │              │  │
│  │ - PatientRepoImpl      │  │ - Patient    │  │
│  │ - ScanRepoImpl         │  │ - Scan       │  │
│  │                        │  │ - ...        │  │
│  │ (Entity ↔ Domain map)  │  │              │  │
│  └───────┬────────────────┘  └──────┬───────┘  │
│          │                          │          │
│  ┌───────▼──────────────────────────▼───────┐  │
│  │              Room DAOs                   │  │
│  │                                          │  │
│  │  - PatientDao                            │  │
│  │  - ScanDao                               │  │
│  │  - ...                                   │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

**Phase 2: COMPLETE ✅**
**Ready for Phase 3: UI Layer!**
