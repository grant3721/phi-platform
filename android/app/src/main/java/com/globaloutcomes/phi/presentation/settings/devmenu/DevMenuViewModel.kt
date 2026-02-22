package com.globaloutcomes.phi.presentation.settings.devmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.data.local.GoDatabase
import com.globaloutcomes.phi.data.local.preferences.DemoSettingsRepository
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Sex
import com.globaloutcomes.phi.domain.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DevMenuViewModel @Inject constructor(
    private val demoSettingsRepository: DemoSettingsRepository,
    private val patientRepository: PatientRepository,
    private val database: GoDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(DevMenuState())
    val state: StateFlow<DevMenuState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            demoSettingsRepository.isDedupBypassed.collect { bypassed ->
                _state.update { it.copy(dedupBypassed = bypassed) }
            }
        }
    }

    fun onEvent(event: DevMenuEvent) {
        when (event) {
            is DevMenuEvent.ToggleDedupBypass -> {
                viewModelScope.launch {
                    demoSettingsRepository.setDedupBypass(!_state.value.dedupBypassed)
                }
            }
            is DevMenuEvent.GenerateTestPatient -> {
                generateTestPatients(1)
            }
            is DevMenuEvent.GenerateBulkPatients -> {
                generateTestPatients(event.count)
            }
            is DevMenuEvent.ResetAllData -> {
                resetAllData()
            }
            is DevMenuEvent.DismissSuccess -> {
                _state.update { it.copy(successMessage = null) }
            }
            is DevMenuEvent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun generateTestPatients(count: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true) }

            try {
                val firstNames = listOf(
                    "Juan", "Maria", "Jose", "Ana", "Pedro", "Rosa", "Miguel", "Luz",
                    "Antonio", "Carmen", "Manuel", "Elena", "Francisco", "Teresa", "Rafael"
                )
                val lastNames = listOf(
                    "Santos", "Reyes", "Cruz", "Bautista", "Ocampo", "Garcia", "Mendoza",
                    "Torres", "Gonzales", "Lopez", "Ramos", "Flores", "Rivera", "Castro"
                )

                var successCount = 0
                repeat(count) {
                    val phoneNumber = "09170%06d".format(Random.nextInt(1000000))
                    val firstName = firstNames.random()
                    val lastName = lastNames.random()
                    val age = Random.nextInt(18, 70)
                    val birthdate = System.currentTimeMillis() - (age * 365L * 24 * 60 * 60 * 1000)
                    val sex = if (Random.nextBoolean()) Sex.MALE else Sex.FEMALE
                    val isPregnant = sex == Sex.FEMALE && age in 15..49 && Random.nextDouble() < 0.15

                    val patient = Patient(
                        phoneNumber = phoneNumber,
                        firstName = firstName,
                        lastName = lastName,
                        birthdate = birthdate,
                        sex = sex,
                        barangayId = "brgy-00${Random.nextInt(1, 3)}", // Use stub barangays
                        isPregnant = isPregnant,
                        gestationalAgeWeeks = if (isPregnant) Random.nextInt(1, 40) else null
                    )

                    val result = patientRepository.insertPatient(patient)
                    result.onSuccess { patientId ->
                        successCount++
                        Timber.d("Generated test patient: ${patient.fullName} with ID $patientId")
                    }.onFailure { error ->
                        Timber.e(error, "Failed to generate patient")
                    }
                }

                _state.update {
                    it.copy(
                        isGenerating = false,
                        successMessage = "Generated $successCount test patient${if (successCount != 1) "s" else ""}"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate test patients")
                _state.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Failed to generate patients: ${e.message}"
                    )
                }
            }
        }
    }

    private fun resetAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isResetting = true) }

            try {
                // Clear all tables
                database.clearAllTables()

                // Reset demo settings
                demoSettingsRepository.setDedupBypass(false)

                _state.update {
                    it.copy(
                        isResetting = false,
                        successMessage = "All data reset successfully",
                        dedupBypassed = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset data")
                _state.update {
                    it.copy(
                        isResetting = false,
                        errorMessage = "Failed to reset data: ${e.message}"
                    )
                }
            }
        }
    }
}

data class DevMenuState(
    val dedupBypassed: Boolean = false,
    val isGenerating: Boolean = false,
    val isResetting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class DevMenuEvent {
    object ToggleDedupBypass : DevMenuEvent()
    object GenerateTestPatient : DevMenuEvent()
    data class GenerateBulkPatients(val count: Int) : DevMenuEvent()
    object ResetAllData : DevMenuEvent()
    object DismissSuccess : DevMenuEvent()
    object DismissError : DevMenuEvent()
}
