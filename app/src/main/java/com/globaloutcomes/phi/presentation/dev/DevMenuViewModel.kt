package com.globaloutcomes.phi.presentation.dev

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.BarangayRepository
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * Dev Menu ViewModel
 * Manages developer utilities and demo mode settings
 */
@HiltViewModel
class DevMenuViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository,
    private val barangayRepository: BarangayRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val DEDUP_BYPASS_KEY = booleanPreferencesKey("dedup_bypass_enabled")

        // Filipino name lists for realistic test data
        private val FIRST_NAMES_MALE = listOf(
            "Juan", "Jose", "Ramon", "Carlos", "Miguel", "Pedro", "Antonio", "Manuel",
            "Ricardo", "Fernando", "Eduardo", "Roberto", "Daniel", "Rafael", "Luis"
        )
        private val FIRST_NAMES_FEMALE = listOf(
            "Maria", "Ana", "Rosa", "Carmen", "Elena", "Isabel", "Sofia", "Teresa",
            "Patricia", "Gloria", "Luz", "Cristina", "Angela", "Rita", "Lourdes"
        )
        private val LAST_NAMES = listOf(
            "Santos", "Reyes", "Cruz", "Bautista", "Ocampo", "Garcia", "Mendoza",
            "Torres", "Gonzales", "Lopez", "Ramos", "Flores", "Castillo", "Rivera",
            "Villanueva", "Aquino", "Fernandez", "Morales", "Valdez", "Santiago"
        )
    }

    private val _state = MutableStateFlow(DevMenuState())
    val state: StateFlow<DevMenuState> = _state.asStateFlow()

    init {
        loadDedupBypassSetting()
        loadDatabaseStats()
    }

    private fun loadDedupBypassSetting() {
        viewModelScope.launch {
            dataStore.data
                .map { prefs -> prefs[DEDUP_BYPASS_KEY] ?: false }
                .collect { enabled ->
                    _state.update { it.copy(dedupBypassEnabled = enabled) }
                }
        }
    }

    private fun loadDatabaseStats() {
        viewModelScope.launch {
            combine(
                patientRepository.getAll(),
                scanRepository.getAll()
            ) { patientsResult, scansResult ->
                val patients = patientsResult.getOrNull()?.size ?: 0
                val scans = scansResult.getOrNull()?.size ?: 0
                _state.update {
                    it.copy(
                        totalPatients = patients,
                        totalScans = scans
                    )
                }
            }.collect()
        }
    }

    fun toggleDedupBypass() {
        viewModelScope.launch {
            val newValue = !_state.value.dedupBypassEnabled
            dataStore.edit { prefs ->
                prefs[DEDUP_BYPASS_KEY] = newValue
            }
            _state.update {
                it.copy(
                    dedupBypassEnabled = newValue,
                    statusMessage = if (newValue) "Dedup bypass enabled" else "Dedup bypass disabled",
                    isError = false
                )
            }
        }
    }

    fun generateTestPatients(count: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, statusMessage = null) }

            try {
                // Load barangays for random selection
                val barangaysResult = barangayRepository.getAll()
                if (barangaysResult.isFailure) {
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            statusMessage = "Failed to load barangays",
                            isError = true
                        )
                    }
                    return@launch
                }

                val barangays = barangaysResult.getOrThrow()
                if (barangays.isEmpty()) {
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            statusMessage = "No barangays available. Run database seeder.",
                            isError = true
                        )
                    }
                    return@launch
                }

                var successCount = 0
                repeat(count) { index ->
                    val patient = generateTestPatient(barangays, index)
                    patientRepository.insert(patient)
                        .onSuccess { successCount++ }
                }

                _state.update {
                    it.copy(
                        isGenerating = false,
                        generatedCount = successCount,
                        statusMessage = "Successfully generated $successCount test patients",
                        isError = false
                    )
                }

                loadDatabaseStats()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        statusMessage = "Error: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }

    private fun generateTestPatient(
        barangays: List<com.globaloutcomes.phi.domain.model.Barangay>,
        index: Int
    ): Patient {
        val sex = if (Random.nextBoolean()) "M" else "F"
        val firstName = if (sex == "M") {
            FIRST_NAMES_MALE.random()
        } else {
            FIRST_NAMES_FEMALE.random()
        }
        val lastName = LAST_NAMES.random()

        val barangay = barangays.random()

        // Random age between 18 and 80
        val ageYears = Random.nextInt(18, 81)
        val dateOfBirth = System.currentTimeMillis() - (ageYears * 365.25 * 24 * 60 * 60 * 1000).toLong()

        // 20% chance of being pregnant (females only, age 18-45)
        val isPregnant = sex == "F" && ageYears in 18..45 && Random.nextFloat() < 0.2f
        val gestationalAge = if (isPregnant) Random.nextInt(4, 40) else null

        val now = System.currentTimeMillis()

        return Patient(
            id = UUID.randomUUID().toString(),
            phoneNumber = "09170000${String.format("%03d", index)}",  // Sequential for testing
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            sex = sex,
            barangayCode = barangay.code,
            municipalityCode = barangay.municipalityCode,
            philhealthNumber = if (Random.nextBoolean()) "01-${Random.nextInt(100000000, 999999999)}-1" else null,
            philsysId = null,
            pregnant = isPregnant,
            gestationalAgeWeeks = gestationalAge,
            highRiskFlag = false,
            highRiskReasons = emptyList(),
            maternalHighRisk = false,
            lastScanDate = null,
            totalScans = 0,
            messengerOptIn = false,
            messengerContactMethod = null,
            messengerContactValue = null,
            messengerPsid = null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
    }

    fun showResetConfirmation() {
        _state.update { it.copy(showResetDialog = true) }
    }

    fun hideResetConfirmation() {
        _state.update { it.copy(showResetDialog = false) }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            try {
                // Delete all patients (cascades to related data via Room)
                patientRepository.getAll()
                    .onSuccess { patients ->
                        patients.forEach { patient ->
                            patientRepository.delete(patient.id)
                        }
                    }

                // Delete all scans
                scanRepository.getAll()
                    .onSuccess { scans ->
                        scans.forEach { scan ->
                            scanRepository.delete(scan.id)
                        }
                    }

                _state.update {
                    it.copy(
                        totalPatients = 0,
                        totalScans = 0,
                        generatedCount = 0,
                        statusMessage = "Database cleared successfully",
                        isError = false
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        statusMessage = "Failed to clear database: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
}

/**
 * Dev Menu State
 */
data class DevMenuState(
    val dedupBypassEnabled: Boolean = false,
    val isGenerating: Boolean = false,
    val generatedCount: Int = 0,
    val totalPatients: Int = 0,
    val totalScans: Int = 0,
    val showResetDialog: Boolean = false,
    val statusMessage: String? = null,
    val isError: Boolean = false
)
