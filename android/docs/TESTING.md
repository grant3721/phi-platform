# Testing Guide

> **Comprehensive testing strategies for PHI Platform Android App**

This guide covers unit testing, integration testing, UI testing, and manual testing strategies for ensuring app quality and reliability.

---

## 📋 Table of Contents

- [Testing Philosophy](#testing-philosophy)
- [Test Structure](#test-structure)
- [Unit Testing](#unit-testing)
- [Integration Testing](#integration-testing)
- [UI Testing](#ui-testing)
- [Manual Testing](#manual-testing)
- [Running Tests](#running-tests)
- [Test Coverage](#test-coverage)
- [Best Practices](#best-practices)

---

## 🎯 Testing Philosophy

### Goals

1. **Confidence** - Tests ensure features work as expected
2. **Regression Prevention** - Catch bugs before they reach users
3. **Documentation** - Tests document expected behavior
4. **Refactoring Safety** - Change code confidently with test safety net

### Testing Pyramid

```
        ┌─────────────┐
        │   UI Tests  │  10% - Slow, brittle, expensive
        ├─────────────┤
        │ Integration │  20% - Moderate speed/cost
        │    Tests    │
        ├─────────────┤
        │    Unit     │  70% - Fast, isolated, cheap
        │    Tests    │
        └─────────────┘
```

**Target Distribution:**
- **70% Unit Tests** - Business logic, ViewModels, Use Cases
- **20% Integration Tests** - Repository + Database, API + DTO mapping
- **10% UI Tests** - Critical user flows only

---

## 📁 Test Structure

### Directory Layout

```
src/
├── test/                              # Unit tests (JVM)
│   └── java/com/globaloutcomes/phi/
│       ├── domain/
│       │   ├── sync/
│       │   │   ├── SyncManagerTest.kt
│       │   │   └── NetworkConnectivityObserverTest.kt
│       │   └── usecase/
│       │       ├── RescanEligibilityUseCaseTest.kt
│       │       └── IncentiveTrackingUseCaseTest.kt
│       ├── data/
│       │   └── repository/
│       │       └── PatientRepositoryImplTest.kt
│       └── presentation/
│           ├── sync/
│           │   └── SyncViewModelTest.kt
│           └── patient/
│               └── PatientDetailViewModelTest.kt
│
└── androidTest/                       # Instrumented tests (Device)
    └── java/com/globaloutcomes/phi/
        ├── data/
        │   └── local/
        │       └── dao/
        │           ├── PatientDaoTest.kt
        │           └── ScanDaoTest.kt
        └── ui/
            ├── HomeScreenTest.kt
            └── ScanFlowTest.kt
```

---

## 🧪 Unit Testing

### What to Test

✅ **Always test:**
- Business logic (Use Cases)
- ViewModels (state transitions)
- Data transformations
- Validation logic
- Calculations (risk scores, incentives)

❌ **Don't test:**
- Android framework code
- Third-party libraries
- Simple getters/setters
- Data classes without logic

### Example: Testing a Use Case

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class RescanEligibilityUseCaseTest {

    private lateinit var useCase: RescanEligibilityUseCase
    private lateinit var mockScanRepository: ScanRepository
    private lateinit var mockPatientRepository: PatientRepository

    @Before
    fun setup() {
        mockScanRepository = mockk()
        mockPatientRepository = mockk()
        useCase = RescanEligibilityUseCase(mockScanRepository, mockPatientRepository)
    }

    @Test
    fun `new patient is eligible for baseline scan`() = runTest {
        // Arrange
        val patientId = "patient-123"
        coEvery { mockScanRepository.getScansForPatient(patientId) } returns Result.success(emptyList())

        // Act
        val result = useCase(patientId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(EligibilityStatus.ALLOWED_BASELINE, result.getOrNull()?.status)
    }

    @Test
    fun `patient with recent scan is not eligible`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val recentScan = createScan(scannedAt = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)) // 1 day ago
        coEvery { mockScanRepository.getScansForPatient(patientId) } returns Result.success(listOf(recentScan))
        coEvery { mockPatientRepository.getPatientById(patientId) } returns Result.success(createPatient(highRiskFlag = false))

        // Act
        val result = useCase(patientId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(EligibilityStatus.REJECTED, result.getOrNull()?.status)
    }
}
```

### Testing ViewModels

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `syncNow calls performSync on SyncManager`() = runTest {
        // Arrange
        val mockSyncManager = mockk<SyncManager>()
        coEvery { mockSyncManager.performSync() } returns Result.success(mockSyncResult)
        val viewModel = SyncViewModel(mockContext, mockSyncManager, mockNetworkObserver)

        // Act
        viewModel.syncNow()
        advanceUntilIdle()

        // Assert
        coVerify { mockSyncManager.performSync() }
    }
}
```

---

## 🔗 Integration Testing

### What to Test

✅ **Integration test scenarios:**
- Repository + Database (DAO)
- API Client + DTO mapping
- WorkManager + SyncManager
- Multiple components working together

### Example: Testing Repository with Room

```kotlin
@RunWith(AndroidJUnit4::class)
class PatientRepositoryIntegrationTest {

    private lateinit var database: GoDatabase
    private lateinit var patientDao: PatientDao
    private lateinit var repository: PatientRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        patientDao = database.patientDao()
        repository = PatientRepositoryImpl(patientDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertPatient_savesToDatabaseAndReturnsId() = runTest {
        // Arrange
        val patient = Patient(
            id = "test-patient-1",
            phoneNumber = "09171234567",
            firstName = "Juan",
            lastName = "Dela Cruz",
            birthdate = System.currentTimeMillis(),
            sex = Sex.MALE,
            barangayId = "brgy-001"
        )

        // Act
        val result = repository.insertPatient(patient)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("test-patient-1", result.getOrNull())

        // Verify saved to database
        val savedPatient = repository.getPatientById("test-patient-1").getOrNull()
        assertNotNull(savedPatient)
        assertEquals("Juan", savedPatient?.firstName)
    }
}
```

---

## 🖥️ UI Testing

### What to Test

✅ **UI test scenarios:**
- Critical user flows (registration → scan → survey → referral)
- Navigation between screens
- Form validation
- Button states (enabled/disabled)
- Error message display

❌ **Don't UI test:**
- Every screen variation
- Styling/layout (use screenshot tests instead)
- Complex business logic (use unit tests)

### Example: Testing a Screen Flow

```kotlin
@RunWith(AndroidJUnit4::class)
class ScanFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun completeScanFlow_displaysResults() {
        // Navigate to scan screen
        composeTestRule.onNodeWithText("Start New Scan").performClick()

        // Fill patient info (assuming scan requires patient)
        composeTestRule.onNodeWithText("Phone Number").performTextInput("09171234567")
        composeTestRule.onNodeWithText("First Name").performTextInput("Juan")
        composeTestRule.onNodeWithText("Last Name").performTextInput("Dela Cruz")
        composeTestRule.onNodeWithText("Register").performClick()

        // Start scan
        composeTestRule.onNodeWithText("Start Scan").performClick()

        // Wait for scan completion (stub SDK completes after delay)
        composeTestRule.waitUntil(timeoutMillis = 35000) {
            composeTestRule.onAllNodesWithText("Scan Complete").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify results displayed
        composeTestRule.onNodeWithText("Risk Level").assertExists()
        composeTestRule.onNodeWithText("Continue to Survey").assertExists()
    }
}
```

---

## 🧑‍💻 Manual Testing

### Critical Manual Test Scenarios

#### 1. Offline Functionality
- [ ] Register patient while offline
- [ ] Perform scan while offline
- [ ] Complete survey while offline
- [ ] Data persists after app restart
- [ ] Sync uploads all pending items when online

#### 2. Sync Scenarios
- [ ] Manual sync via sync button works
- [ ] Periodic sync triggers after 15 minutes
- [ ] Offline indicator appears when network lost
- [ ] Sync continues in background
- [ ] Multiple pending items sync correctly

#### 3. Rescan Eligibility
- [ ] New patient can scan immediately
- [ ] High-risk patient can re-scan immediately
- [ ] Normal patient blocked for 12 months
- [ ] Pregnant patient can scan every 6 months
- [ ] Error message shows days until eligible

#### 4. Incentive Tracking
- [ ] Earnings update after each scan
- [ ] Daily cap blocks scans at 50
- [ ] Earnings reset at midnight
- [ ] Progress ring updates correctly

#### 5. Referral System
- [ ] High-risk scan generates referral automatically
- [ ] Referral appears in referral list
- [ ] BHW can confirm referral
- [ ] Confirmed referral escalates to RHU
- [ ] Overdue referrals show red badge

#### 6. Patient History
- [ ] All scans visible in Scans tab
- [ ] Surveys visible in Surveys tab
- [ ] Referrals visible in Referrals tab
- [ ] Overview tab shows recent items
- [ ] Tab counts update correctly

#### 7. Scan Details
- [ ] All 34 biomarkers display
- [ ] Category filtering works
- [ ] Risk assessment shows correct color
- [ ] Signal quality displayed
- [ ] Patient context header correct

---

## 🏃 Running Tests

### Command Line

```bash
# Run all unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run specific test class
./gradlew test --tests SyncManagerTest

# Run specific test method
./gradlew test --tests SyncManagerTest.performSync_with_no_pending_items_returns_success

# Run instrumented tests (requires device)
./gradlew connectedAndroidTest

# Run specific instrumented test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=PatientDaoTest
```

### Android Studio

1. **Run all tests in a file**
   - Right-click on test file → Run 'TestFileName'

2. **Run single test**
   - Click green arrow next to test function
   - Or right-click test function → Run

3. **Run with coverage**
   - Right-click test file → Run 'TestFileName' with Coverage
   - View coverage report in Coverage panel

4. **Debug tests**
   - Set breakpoints in test or production code
   - Right-click test → Debug
   - Step through execution

---

## 📊 Test Coverage

### Coverage Goals

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| **Domain (Use Cases)** | 80%+ | ⭐⭐⭐ Critical |
| **ViewModels** | 70%+ | ⭐⭐⭐ Critical |
| **Repository Impl** | 60%+ | ⭐⭐ Important |
| **Sync Infrastructure** | 80%+ | ⭐⭐⭐ Critical |
| **UI Composables** | 20%+ | ⭐ Nice to have |

### Viewing Coverage

```bash
# Generate coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Open report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Current Coverage Status

✅ **Completed:**
- SyncManager - 12 tests (upload, download, errors)
- NetworkConnectivityObserver - 10 tests (connectivity detection)
- SyncViewModel - 8 tests (state management)

📋 **TODO:**
- RescanEligibilityUseCase - 0 tests
- IncentiveTrackingUseCase - 0 tests
- PatientDetailViewModel - 0 tests
- ScanDetailViewModel - 0 tests
- Repository implementations - 0 tests
- DAO integration tests - 0 tests

---

## ✅ Best Practices

### 1. Test Naming

```kotlin
// ❌ Bad
@Test
fun test1() { }

// ✅ Good
@Test
fun `performSync with no pending items returns success with zero counts`() { }
```

**Pattern:** `methodName_scenario_expectedResult`

### 2. Arrange-Act-Assert (AAA)

```kotlin
@Test
fun `example test`() = runTest {
    // Arrange - Set up test data and mocks
    val input = "test-input"
    coEvery { mockRepository.getData() } returns Result.success(mockData)

    // Act - Execute the code under test
    val result = useCase(input)

    // Assert - Verify the outcome
    assertTrue(result.isSuccess)
    assertEquals(expectedValue, result.getOrNull())
}
```

### 3. Mock Dependencies, Not System Under Test

```kotlin
// ✅ Good - Mock dependencies
val mockRepository = mockk<PatientRepository>()
val useCase = RegisterPatientUseCase(mockRepository) // Real use case

// ❌ Bad - Mocking system under test
val mockUseCase = mockk<RegisterPatientUseCase>() // Don't mock what you're testing!
```

### 4. Use Test Fixtures

```kotlin
// Create reusable test data
object TestFixtures {
    fun createPatient(
        id: String = "test-patient-1",
        firstName: String = "Juan",
        phoneNumber: String = "09171234567",
        syncStatus: SyncStatus = SyncStatus.PENDING
    ) = Patient(
        id = id,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = "Dela Cruz",
        birthdate = System.currentTimeMillis(),
        sex = Sex.MALE,
        barangayId = "brgy-001",
        syncStatus = syncStatus
    )
}

// Use in tests
@Test
fun `test something`() {
    val patient = TestFixtures.createPatient(syncStatus = SyncStatus.SYNCED)
    // ...
}
```

### 5. Test Edge Cases

```kotlin
@Test
fun `handles empty list`() { }

@Test
fun `handles null values`() { }

@Test
fun `handles network timeout`() { }

@Test
fun `handles database error`() { }

@Test
fun `handles concurrent access`() { }
```

### 6. Avoid Test Interdependence

```kotlin
// ❌ Bad - Tests depend on execution order
@Test
fun test1_createUser() { /* creates user in DB */ }

@Test
fun test2_updateUser() { /* assumes user exists from test1 */ }

// ✅ Good - Each test is independent
@Test
fun `createUser saves to database`() {
    // Arrange: Create fresh test data
    val user = TestFixtures.createUser()
    // Act & Assert
}

@Test
fun `updateUser modifies existing user`() {
    // Arrange: Create user specifically for this test
    val user = TestFixtures.createUser()
    repository.insert(user)
    // Act & Assert
}
```

### 7. Test Behavior, Not Implementation

```kotlin
// ❌ Bad - Testing implementation details
@Test
fun `syncManager calls uploadData exactly once`() {
    verify(exactly = 1) { mockApi.uploadData(any()) }
}

// ✅ Good - Testing observable behavior
@Test
fun `performSync uploads pending items and returns success`() {
    val result = syncManager.performSync()
    assertTrue(result.isSuccess)
    assertEquals(5, result.getOrNull()?.uploadedPatients)
}
```

---

## 🐛 Debugging Failed Tests

### Common Issues

1. **Test timeout** - Increase timeout or use `advanceUntilIdle()` in coroutine tests
2. **Flaky tests** - Remove time dependencies, use deterministic data
3. **Mock not configured** - Ensure all mock calls are set up with `coEvery`
4. **Wrong dispatcher** - Use `StandardTestDispatcher` and set Main dispatcher
5. **Flow not collected** - Use `.first()` or `.toList()` to collect Flow values

### Debug Tips

```kotlin
// Print values during test
@Test
fun `debug test`() = runTest {
    val result = useCase()
    println("Result: $result") // Appears in test output
    assertTrue(result.isSuccess)
}

// Verify mock was called
@Test
fun `verify mock`() = runTest {
    useCase()
    coVerify { mockRepository.getData() } // Fails if not called
}

// Capture arguments
@Test
fun `capture arguments`() = runTest {
    val slot = slot<Patient>()
    coEvery { mockRepository.insert(capture(slot)) } returns Result.success(Unit)

    useCase(patient)

    assertEquals("Juan", slot.captured.firstName)
}
```

---

## 📚 Additional Resources

### Libraries Documentation
- [MockK](https://mockk.io/) - Mocking library
- [Turbine](https://github.com/cashapp/turbine) - Flow testing
- [Compose Testing](https://developer.android.com/jetpack/compose/testing) - UI testing

### Testing Guides
- [Android Testing Guide](https://developer.android.com/training/testing)
- [Testing Coroutines](https://developer.android.com/kotlin/coroutines/test)
- [ViewModel Testing](https://developer.android.com/codelabs/android-testing)

---

**Happy Testing! 🎉**
