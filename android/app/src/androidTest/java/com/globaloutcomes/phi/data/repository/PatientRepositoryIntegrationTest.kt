package com.globaloutcomes.phi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.globaloutcomes.phi.data.local.GoDatabase
import com.globaloutcomes.phi.data.local.dao.PatientDao
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Sex
import com.globaloutcomes.phi.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for PatientRepository with real Room database
 * Tests the complete data flow: Repository -> DAO -> Database
 */
@RunWith(AndroidJUnit4::class)
class PatientRepositoryIntegrationTest {

    private lateinit var database: GoDatabase
    private lateinit var patientDao: PatientDao
    private lateinit var repository: PatientRepositoryImpl

    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            GoDatabase::class.java
        )
            .allowMainThreadQueries() // Allow queries on main thread for testing
            .build()

        patientDao = database.patientDao()
        repository = PatientRepositoryImpl(patientDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    // ========== INSERT PATIENT ==========

    @Test
    fun insertPatient_savesToDatabaseAndReturnsId() = runTest {
        // Arrange
        val patient = createPatient(
            id = "patient-001",
            firstName = "Juan",
            lastName = "Dela Cruz"
        )

        // Act
        val result = repository.insertPatient(patient)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("patient-001", result.getOrNull())

        // Verify saved to database
        val savedPatient = repository.getPatientById("patient-001").getOrNull()
        assertNotNull(savedPatient)
        assertEquals("Juan", savedPatient?.firstName)
        assertEquals("Dela Cruz", savedPatient?.lastName)
    }

    @Test
    fun insertPatient_withAllFieldsPopulated_savesCorrectly() = runTest {
        // Arrange
        val patient = createPatient(
            id = "patient-002",
            firstName = "Maria",
            lastName = "Santos",
            phoneNumber = "09181234567",
            sex = Sex.FEMALE,
            philHealthNumber = "12-345678901-2",
            philSysNumber = "1234-5678-9012-3456",
            isPregnant = true,
            gestationalAgeWeeks = 20,
            messengerOptIn = true,
            highRiskFlag = true,
            highRiskReasons = listOf("Hypertension", "High Blood Glucose")
        )

        // Act
        repository.insertPatient(patient)

        // Assert
        val saved = repository.getPatientById("patient-002").getOrNull()!!
        assertEquals("Maria", saved.firstName)
        assertEquals(Sex.FEMALE, saved.sex)
        assertEquals("12-345678901-2", saved.philHealthNumber)
        assertEquals("1234-5678-9012-3456", saved.philSysNumber)
        assertTrue(saved.isPregnant)
        assertEquals(20, saved.gestationalAgeWeeks)
        assertTrue(saved.messengerOptIn)
        assertTrue(saved.highRiskFlag)
        assertEquals(2, saved.highRiskReasons.size)
        assertTrue(saved.highRiskReasons.contains("Hypertension"))
    }

    @Test
    fun insertPatient_duplicateId_replacesExisting() = runTest {
        // Arrange
        val patient1 = createPatient(id = "patient-003", firstName = "Juan")
        val patient2 = createPatient(id = "patient-003", firstName = "Pedro")

        // Act
        repository.insertPatient(patient1)
        repository.insertPatient(patient2)

        // Assert
        val saved = repository.getPatientById("patient-003").getOrNull()!!
        assertEquals("Pedro", saved.firstName) // Second insert replaced first
    }

    // ========== UPDATE PATIENT ==========

    @Test
    fun updatePatient_modifiesExistingPatient() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-004", firstName = "Juan", lastName = "Dela Cruz")
        repository.insertPatient(patient)

        // Act
        val updated = patient.copy(firstName = "Jose", lastName = "Rizal")
        val result = repository.updatePatient(updated)

        // Assert
        assertTrue(result.isSuccess)
        val saved = repository.getPatientById("patient-004").getOrNull()!!
        assertEquals("Jose", saved.firstName)
        assertEquals("Rizal", saved.lastName)
    }

    @Test
    fun updatePatient_updatesSyncStatus() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-005", syncStatus = SyncStatus.PENDING)
        repository.insertPatient(patient)

        // Act
        val updated = patient.copy(syncStatus = SyncStatus.SYNCED, syncedAt = now)
        repository.updatePatient(updated)

        // Assert
        val saved = repository.getPatientById("patient-005").getOrNull()!!
        assertEquals(SyncStatus.SYNCED, saved.syncStatus)
        assertEquals(now, saved.syncedAt)
    }

    // ========== GET PATIENT BY ID ==========

    @Test
    fun getPatientById_returnsPatient_whenExists() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-006", firstName = "Maria")
        repository.insertPatient(patient)

        // Act
        val result = repository.getPatientById("patient-006")

        // Assert
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("Maria", result.getOrNull()?.firstName)
    }

    @Test
    fun getPatientById_returnsNull_whenNotExists() = runTest {
        // Act
        val result = repository.getPatientById("nonexistent-patient")

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ========== GET PATIENT BY PHONE NUMBER ==========

    @Test
    fun getPatientByPhoneNumber_findsPatient() = runTest {
        // Arrange
        val patient = createPatient(
            id = "patient-007",
            phoneNumber = "09171112233",
            firstName = "Pedro"
        )
        repository.insertPatient(patient)

        // Act
        val result = repository.getPatientByPhoneNumber("09171112233")

        // Assert
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("Pedro", result.getOrNull()?.firstName)
    }

    @Test
    fun getPatientByPhoneNumber_returnsNull_whenNotExists() = runTest {
        // Act
        val result = repository.getPatientByPhoneNumber("09999999999")

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ========== FLOW QUERIES ==========

    @Test
    fun getPatientByIdFlow_emitsUpdates() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-008", firstName = "Juan")
        repository.insertPatient(patient)

        // Act - Get initial value
        val flow = repository.getPatientByIdFlow("patient-008")
        val initial = flow.first()

        // Assert
        assertNotNull(initial)
        assertEquals("Juan", initial?.firstName)

        // Act - Update patient
        repository.updatePatient(patient.copy(firstName = "Jose"))
        val updated = flow.first()

        // Assert
        assertEquals("Jose", updated?.firstName)
    }

    @Test
    fun getAllPatientsFlow_returnsAllPatients() = runTest {
        // Arrange
        repository.insertPatient(createPatient(id = "p1", firstName = "Patient1"))
        repository.insertPatient(createPatient(id = "p2", firstName = "Patient2"))
        repository.insertPatient(createPatient(id = "p3", firstName = "Patient3"))

        // Act
        val patients = repository.getAllPatientsFlow().first()

        // Assert
        assertEquals(3, patients.size)
    }

    @Test
    fun getHighRiskPatientsFlow_returnsOnlyHighRisk() = runTest {
        // Arrange
        repository.insertPatient(createPatient(id = "p1", highRiskFlag = true))
        repository.insertPatient(createPatient(id = "p2", highRiskFlag = false))
        repository.insertPatient(createPatient(id = "p3", highRiskFlag = true))
        repository.insertPatient(createPatient(id = "p4", highRiskFlag = false))

        // Act
        val highRiskPatients = repository.getHighRiskPatientsFlow().first()

        // Assert
        assertEquals(2, highRiskPatients.size)
        assertTrue(highRiskPatients.all { it.highRiskFlag })
    }

    @Test
    fun getPregnantPatientsFlow_returnsOnlyPregnant() = runTest {
        // Arrange
        repository.insertPatient(createPatient(id = "p1", isPregnant = true))
        repository.insertPatient(createPatient(id = "p2", isPregnant = false))
        repository.insertPatient(createPatient(id = "p3", isPregnant = true))

        // Act
        val pregnantPatients = repository.getPregnantPatientsFlow().first()

        // Assert
        assertEquals(2, pregnantPatients.size)
        assertTrue(pregnantPatients.all { it.isPregnant })
    }

    // ========== COUNT QUERIES ==========

    @Test
    fun getPatientCount_returnsCorrectCount() = runTest {
        // Arrange
        repository.insertPatient(createPatient(id = "p1"))
        repository.insertPatient(createPatient(id = "p2"))
        repository.insertPatient(createPatient(id = "p3"))

        // Act
        val count = repository.getPatientCount().getOrNull()

        // Assert
        assertEquals(3, count)
    }

    @Test
    fun getHighRiskCount_countsOnlyHighRisk() = runTest {
        // Arrange
        repository.insertPatient(createPatient(id = "p1", highRiskFlag = true))
        repository.insertPatient(createPatient(id = "p2", highRiskFlag = false))
        repository.insertPatient(createPatient(id = "p3", highRiskFlag = true))
        repository.insertPatient(createPatient(id = "p4", highRiskFlag = true))

        // Act
        val count = repository.getHighRiskCount().getOrNull()

        // Assert
        assertEquals(3, count)
    }

    // ========== UPDATE RISK STATUS ==========

    @Test
    fun updateRiskStatus_updatesPatientRiskFields() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-009", highRiskFlag = false)
        repository.insertPatient(patient)

        // Act
        val riskReasons = listOf("Hypertension", "Tachycardia", "Low Oxygen Saturation")
        repository.updateRiskStatus(
            patientId = "patient-009",
            highRiskFlag = true,
            highRiskReasons = riskReasons,
            timestamp = now
        )

        // Assert
        val updated = repository.getPatientById("patient-009").getOrNull()!!
        assertTrue(updated.highRiskFlag)
        assertEquals(3, updated.highRiskReasons.size)
        assertTrue(updated.highRiskReasons.contains("Hypertension"))
        assertEquals(now, updated.lastRiskAssessment)
    }

    // ========== SYNC STATUS ==========

    @Test
    fun getPatientsBySyncStatus_filtersByStatus() = runTest {
        // Arrange
        repository.insertPatient(createPatient(id = "p1", syncStatus = SyncStatus.PENDING))
        repository.insertPatient(createPatient(id = "p2", syncStatus = SyncStatus.SYNCED))
        repository.insertPatient(createPatient(id = "p3", syncStatus = SyncStatus.PENDING))
        repository.insertPatient(createPatient(id = "p4", syncStatus = SyncStatus.FAILED))

        // Act
        val pendingPatients = repository.getPatientsBySyncStatus(SyncStatus.PENDING).getOrNull()!!

        // Assert
        assertEquals(2, pendingPatients.size)
        assertTrue(pendingPatients.all { it.syncStatus == SyncStatus.PENDING })
    }

    @Test
    fun updateSyncStatus_changesSyncStatus() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-010", syncStatus = SyncStatus.PENDING)
        repository.insertPatient(patient)

        // Act
        val syncTime = now
        repository.updateSyncStatus("patient-010", SyncStatus.SYNCED, syncTime)

        // Assert
        val updated = repository.getPatientById("patient-010").getOrNull()!!
        assertEquals(SyncStatus.SYNCED, updated.syncStatus)
        assertEquals(syncTime, updated.syncedAt)
    }

    // ========== DELETE PATIENT ==========

    @Test
    fun deletePatient_removesFromDatabase() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-011")
        repository.insertPatient(patient)
        assertNotNull(repository.getPatientById("patient-011").getOrNull())

        // Act
        repository.deletePatient("patient-011")

        // Assert
        assertNull(repository.getPatientById("patient-011").getOrNull())
    }

    @Test
    fun deletePatient_nonexistent_doesNotThrowError() = runTest {
        // Act & Assert - Should not throw
        val result = repository.deletePatient("nonexistent-patient")
        assertTrue(result.isSuccess)
    }

    // ========== JSON SERIALIZATION ==========

    @Test
    fun highRiskReasons_serializeAndDeserializeCorrectly() = runTest {
        // Arrange
        val reasons = listOf(
            "Hypertension",
            "High Blood Glucose",
            "Low Oxygen Saturation",
            "Tachycardia"
        )
        val patient = createPatient(id = "patient-012", highRiskReasons = reasons)

        // Act
        repository.insertPatient(patient)
        val saved = repository.getPatientById("patient-012").getOrNull()!!

        // Assert
        assertEquals(4, saved.highRiskReasons.size)
        assertEquals(reasons, saved.highRiskReasons)
    }

    @Test
    fun emptyHighRiskReasons_handledCorrectly() = runTest {
        // Arrange
        val patient = createPatient(id = "patient-013", highRiskReasons = emptyList())

        // Act
        repository.insertPatient(patient)
        val saved = repository.getPatientById("patient-013").getOrNull()!!

        // Assert
        assertTrue(saved.highRiskReasons.isEmpty())
    }

    // ========== MULTIPLE OPERATIONS ==========

    @Test
    fun multipleInserts_allSavedCorrectly() = runTest {
        // Arrange
        val patients = (1..10).map { i ->
            createPatient(
                id = "patient-$i",
                firstName = "Patient$i",
                phoneNumber = "0917000000$i"
            )
        }

        // Act
        patients.forEach { repository.insertPatient(it) }

        // Assert
        val count = repository.getPatientCount().getOrNull()
        assertEquals(10, count)

        val allPatients = repository.getAllPatientsFlow().first()
        assertEquals(10, allPatients.size)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createPatient(
        id: String,
        phoneNumber: String = "09171234567",
        firstName: String = "Juan",
        lastName: String = "Dela Cruz",
        birthdate: Long = now - (30L * 365 * 24 * 60 * 60 * 1000),
        sex: Sex = Sex.MALE,
        barangayId: String = "brgy-001",
        philHealthNumber: String? = null,
        philSysNumber: String? = null,
        isPregnant: Boolean = false,
        gestationalAgeWeeks: Int? = null,
        lastMenstrualPeriod: Long? = null,
        messengerOptIn: Boolean = false,
        messengerUserId: String? = null,
        messengerName: String? = null,
        highRiskFlag: Boolean = false,
        highRiskReasons: List<String> = emptyList(),
        maternalHighRisk: Boolean = false,
        lastRiskAssessment: Long? = null,
        createdAt: Long = now,
        updatedAt: Long = now,
        syncStatus: SyncStatus = SyncStatus.PENDING,
        syncedAt: Long? = null,
        serverPatientId: String? = null
    ) = Patient(
        id = id,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        birthdate = birthdate,
        sex = sex,
        barangayId = barangayId,
        philHealthNumber = philHealthNumber,
        philSysNumber = philSysNumber,
        isPregnant = isPregnant,
        gestationalAgeWeeks = gestationalAgeWeeks,
        lastMenstrualPeriod = lastMenstrualPeriod,
        messengerOptIn = messengerOptIn,
        messengerUserId = messengerUserId,
        messengerName = messengerName,
        highRiskFlag = highRiskFlag,
        highRiskReasons = highRiskReasons,
        maternalHighRisk = maternalHighRisk,
        lastRiskAssessment = lastRiskAssessment,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        syncedAt = syncedAt,
        serverPatientId = serverPatientId
    )
}
