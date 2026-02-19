package com.globaloutcomes.phi.presentation.survey.selection

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Survey Selection ViewModel
 * Determines which surveys should be completed based on patient and scan data
 */
@HiltViewModel
class SurveySelectionViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val patientRepository: PatientRepository,
    private val surveyRepository: SurveyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SurveySelectionState())
    val state: StateFlow<SurveySelectionState> = _state.asStateFlow()

    fun loadScanAndPatient(scanId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Load scan
            val scanResult = scanRepository.getById(scanId)
            if (scanResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = scanResult.exceptionOrNull()?.message ?: "Failed to load scan"
                )
                return@launch
            }

            val scan = scanResult.getOrNull()
            if (scan == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Scan not found"
                )
                return@launch
            }

            // Load patient
            val patientResult = patientRepository.getById(scan.patientId)
            if (patientResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = patientResult.exceptionOrNull()?.message ?: "Failed to load patient"
                )
                return@launch
            }

            val patient = patientResult.getOrNull()
            if (patient == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Patient not found"
                )
                return@launch
            }

            // Load existing surveys for this scan
            val surveysResult = surveyRepository.getByScanId(scanId)
            val existingSurveys = surveysResult.getOrNull() ?: emptyList()
            val completedTypes = existingSurveys.map { it.surveyType }.toSet()

            // Determine which surveys are required and optional
            val (required, optional) = determineSurveys(patient, scan, completedTypes)

            val totalSurveys = required.size + optional.size
            val completedSurveys = required.count { it.completed } + optional.count { it.completed }
            val canSkip = required.all { it.completed } && optional.any { !it.completed }
            val allCompleted = required.all { it.completed } && optional.all { it.completed }

            _state.value = SurveySelectionState(
                requiredSurveys = required,
                optionalSurveys = optional,
                totalSurveys = totalSurveys,
                completedSurveys = completedSurveys,
                canSkipRemaining = canSkip,
                allSurveysCompleted = allCompleted,
                isLoading = false,
                error = null
            )
        }
    }

    private fun determineSurveys(
        patient: Patient,
        scan: Scan,
        completedTypes: Set<SurveyType>
    ): Pair<List<SurveyInfo>, List<SurveyInfo>> {
        val required = mutableListOf<SurveyInfo>()
        val optional = mutableListOf<SurveyInfo>()

        // NCD Survey - REQUIRED for all patients
        required.add(
            SurveyInfo(
                type = SurveyType.NCD,
                title = "Non-Communicable Diseases",
                description = "Diabetes, hypertension, cardiovascular risk assessment",
                estimatedMinutes = 5,
                icon = Icons.Filled.Favorite,
                required = true,
                completed = SurveyType.NCD in completedTypes
            )
        )

        // Maternal Survey - REQUIRED if pregnant
        if (patient.isPregnant) {
            required.add(
                SurveyInfo(
                    type = SurveyType.MATERNAL,
                    title = "Maternal Health",
                    description = "Pregnancy status, prenatal care, complications screening",
                    estimatedMinutes = 4,
                    icon = Icons.Filled.FavoriteBorder,
                    required = true,
                    completed = SurveyType.MATERNAL in completedTypes
                )
            )
        }

        // Infectious Disease Survey - OPTIONAL (can be required based on symptoms)
        val infectiousRequired = hasInfectiousSymptoms(scan)
        if (infectiousRequired) {
            required.add(
                SurveyInfo(
                    type = SurveyType.INFECTIOUS,
                    title = "Infectious Disease Screening",
                    description = "TB, Dengue, COVID-19, travel history",
                    estimatedMinutes = 3,
                    icon = Icons.Filled.Warning,
                    required = true,
                    completed = SurveyType.INFECTIOUS in completedTypes
                )
            )
        } else {
            optional.add(
                SurveyInfo(
                    type = SurveyType.INFECTIOUS,
                    title = "Infectious Disease Screening",
                    description = "TB, Dengue, COVID-19, travel history",
                    estimatedMinutes = 3,
                    icon = Icons.Filled.Warning,
                    required = false,
                    completed = SurveyType.INFECTIOUS in completedTypes
                )
            )
        }

        // Mental Health Survey (PHQ-9) - OPTIONAL (can be required based on symptoms/risk)
        val mentalHealthRequired = hasMentalHealthRisk(scan)
        if (mentalHealthRequired) {
            required.add(
                SurveyInfo(
                    type = SurveyType.MENTAL_HEALTH,
                    title = "Mental Health Screening (PHQ-9)",
                    description = "Depression screening questionnaire",
                    estimatedMinutes = 3,
                    icon = Icons.Filled.Person,
                    required = true,
                    completed = SurveyType.MENTAL_HEALTH in completedTypes
                )
            )
        } else {
            optional.add(
                SurveyInfo(
                    type = SurveyType.MENTAL_HEALTH,
                    title = "Mental Health Screening (PHQ-9)",
                    description = "Depression screening questionnaire",
                    estimatedMinutes = 3,
                    icon = Icons.Filled.Person,
                    required = false,
                    completed = SurveyType.MENTAL_HEALTH in completedTypes
                )
            )
        }

        return Pair(required, optional)
    }

    /**
     * Check if scan indicates infectious disease symptoms
     * Based on: fever indicators, respiratory rate, SpO2
     */
    private fun hasInfectiousSymptoms(scan: Scan): Boolean {
        // High respiratory rate (>20/min)
        val highRR = (scan.respiratoryRate ?: 0.0) > 20.0

        // Low SpO2 (<95%)
        val lowSpO2 = (scan.spo2 ?: 100.0) < 95.0

        // Any risk flags mentioning respiratory or fever
        val respiratoryRisk = scan.riskFlags.any { flag ->
            flag.contains("respiratory", ignoreCase = true) ||
            flag.contains("fever", ignoreCase = true) ||
            flag.contains("oxygen", ignoreCase = true) ||
            flag.contains("SpO2", ignoreCase = true)
        }

        return highRR || lowSpO2 || respiratoryRisk
    }

    /**
     * Check if patient might benefit from mental health screening
     * Based on: stress indicators from HRV/ANS balance
     */
    private fun hasMentalHealthRisk(scan: Scan): Boolean {
        // High stress index
        val highStress = (scan.stressIndex ?: 0.0) > 7.0

        // Low HRV (indicates poor stress adaptation)
        val lowHRV = (scan.sdnn ?: 100.0) < 50.0

        // Any mental health risk flags
        val mentalHealthRisk = scan.riskFlags.any { flag ->
            flag.contains("stress", ignoreCase = true) ||
            flag.contains("mental", ignoreCase = true) ||
            flag.contains("anxiety", ignoreCase = true)
        }

        return highStress || lowHRV || mentalHealthRisk
    }
}

/**
 * Survey Selection State
 */
data class SurveySelectionState(
    val requiredSurveys: List<SurveyInfo> = emptyList(),
    val optionalSurveys: List<SurveyInfo> = emptyList(),
    val totalSurveys: Int = 0,
    val completedSurveys: Int = 0,
    val canSkipRemaining: Boolean = false,
    val allSurveysCompleted: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
