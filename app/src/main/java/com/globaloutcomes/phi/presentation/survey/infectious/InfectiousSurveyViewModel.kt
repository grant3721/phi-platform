package com.globaloutcomes.phi.presentation.survey.infectious

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.InfectiousSurveyResponse
import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Infectious Disease Survey ViewModel
 * Manages TB, Dengue, COVID-19 screening data
 */
@HiltViewModel
class InfectiousSurveyViewModel @Inject constructor(
    private val surveyRepository: SurveyRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InfectiousSurveyState())
    val state: StateFlow<InfectiousSurveyState> = _state.asStateFlow()

    fun initSurvey(scanId: String) {
        _state.update { it.copy(scanId = scanId) }
        loadScan(scanId)
    }

    private fun loadScan(scanId: String) {
        viewModelScope.launch {
            scanRepository.getById(scanId)
                .onSuccess { scan ->
                    _state.update {
                        it.copy(
                            patientId = scan?.patientId ?: "",
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load scan"
                        )
                    }
                }
        }
    }

    // TB Screening
    fun updatePersistentCough(value: Boolean?) {
        _state.update { it.copy(persistentCough = value) }
        updateProgress()
    }

    fun updateCoughDuration(weeks: Int?) {
        _state.update { it.copy(coughDuration = weeks) }
    }

    fun updateBloodInSputum(value: Boolean?) {
        _state.update { it.copy(bloodInSputum = value) }
        updateProgress()
    }

    fun updateNightSweats(value: Boolean?) {
        _state.update { it.copy(nightSweats = value) }
        updateProgress()
    }

    fun updateUnexplainedWeightLoss(value: Boolean?) {
        _state.update { it.copy(unexplainedWeightLoss = value) }
        updateProgress()
    }

    fun updateFever(value: Boolean?) {
        _state.update { it.copy(fever = value) }
        updateProgress()
    }

    fun updateTbContact(value: Boolean?) {
        _state.update { it.copy(tbContact = value) }
        updateProgress()
    }

    // Dengue Screening
    fun updateSuddenFever(value: Boolean?) {
        _state.update { it.copy(suddenFever = value) }
        updateProgress()
    }

    fun updateSevereHeadache(value: Boolean?) {
        _state.update { it.copy(severeHeadache = value) }
        updateProgress()
    }

    fun updatePainBehindEyes(value: Boolean?) {
        _state.update { it.copy(painBehindEyes = value) }
        updateProgress()
    }

    fun updateJointPain(value: Boolean?) {
        _state.update { it.copy(jointPain = value) }
        updateProgress()
    }

    fun updateRash(value: Boolean?) {
        _state.update { it.copy(rash = value) }
        updateProgress()
    }

    fun updateBleeding(value: Boolean?) {
        _state.update { it.copy(bleeding = value) }
        updateProgress()
    }

    // COVID-19 Screening
    fun updateCovidSymptoms(value: Boolean?) {
        _state.update { it.copy(covidSymptoms = value) }
        updateProgress()
    }

    fun updateCovidContact(value: Boolean?) {
        _state.update { it.copy(covidContact = value) }
        updateProgress()
    }

    fun updateCovidVaccinated(value: Boolean?) {
        _state.update { it.copy(covidVaccinated = value) }
        updateProgress()
    }

    fun updateCovidVaccineDoses(doses: Int?) {
        _state.update { it.copy(covidVaccineDoses = doses) }
    }

    // Travel History
    fun updateRecentTravel(value: Boolean?) {
        _state.update { it.copy(recentTravel = value) }
        updateProgress()
    }

    fun addTravelDestination(destination: String) {
        _state.update {
            it.copy(travelDestinations = it.travelDestinations + destination)
        }
    }

    fun removeTravelDestination(destination: String) {
        _state.update {
            it.copy(travelDestinations = it.travelDestinations - destination)
        }
    }

    private fun updateProgress() {
        val current = _state.value
        val totalRequired = 16 // All required questions
        var answered = 0

        // TB questions (7)
        if (current.persistentCough != null) answered++
        if (current.bloodInSputum != null) answered++
        if (current.nightSweats != null) answered++
        if (current.unexplainedWeightLoss != null) answered++
        if (current.fever != null) answered++
        if (current.tbContact != null) answered++

        // Dengue questions (6)
        if (current.suddenFever != null) answered++
        if (current.severeHeadache != null) answered++
        if (current.painBehindEyes != null) answered++
        if (current.jointPain != null) answered++
        if (current.rash != null) answered++
        if (current.bleeding != null) answered++

        // COVID questions (3)
        if (current.covidSymptoms != null) answered++
        if (current.covidContact != null) answered++
        if (current.covidVaccinated != null) answered++

        val progress = answered.toFloat() / totalRequired
        val canSubmit = answered == totalRequired

        _state.update {
            it.copy(
                progress = progress,
                canSubmit = canSubmit
            )
        }
    }

    fun submitSurvey() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val current = _state.value

            if (!current.canSubmit) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = "Please answer all required questions"
                    )
                }
                return@launch
            }

            val response = InfectiousSurveyResponse(
                // TB
                persistentCough = current.persistentCough,
                coughDuration = current.coughDuration,
                bloodInSputum = current.bloodInSputum,
                nightSweats = current.nightSweats,
                unexplainedWeightLoss = current.unexplainedWeightLoss,
                fever = current.fever,
                tbContact = current.tbContact,
                // Dengue
                suddenFever = current.suddenFever,
                severeHeadache = current.severeHeadache,
                painBehindEyes = current.painBehindEyes,
                jointPain = current.jointPain,
                rash = current.rash,
                bleeding = current.bleeding,
                // COVID
                covidSymptoms = current.covidSymptoms,
                covidContact = current.covidContact,
                covidVaccinated = current.covidVaccinated,
                covidVaccineDoses = current.covidVaccineDoses,
                // Travel
                recentTravel = current.recentTravel,
                travelDestinations = current.travelDestinations
            )

            val survey = Survey(
                id = java.util.UUID.randomUUID().toString(),
                scanId = current.scanId,
                patientId = current.patientId,
                surveyType = SurveyType.INFECTIOUS,
                responses = mapOf("infectious" to response), // Will be serialized to JSON
                completedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )

            surveyRepository.insert(survey)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to save survey"
                        )
                    }
                }
        }
    }
}

/**
 * Infectious Survey State
 */
data class InfectiousSurveyState(
    val scanId: String = "",
    val patientId: String = "",
    val isLoading: Boolean = true,

    // TB Screening
    val persistentCough: Boolean? = null,
    val coughDuration: Int? = null,
    val bloodInSputum: Boolean? = null,
    val nightSweats: Boolean? = null,
    val unexplainedWeightLoss: Boolean? = null,
    val fever: Boolean? = null,
    val tbContact: Boolean? = null,

    // Dengue Screening
    val suddenFever: Boolean? = null,
    val severeHeadache: Boolean? = null,
    val painBehindEyes: Boolean? = null,
    val jointPain: Boolean? = null,
    val rash: Boolean? = null,
    val bleeding: Boolean? = null,

    // COVID-19 Screening
    val covidSymptoms: Boolean? = null,
    val covidContact: Boolean? = null,
    val covidVaccinated: Boolean? = null,
    val covidVaccineDoses: Int? = null,

    // Travel History
    val recentTravel: Boolean? = null,
    val travelDestinations: List<String> = emptyList(),

    // UI State
    val progress: Float = 0f,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)
