package com.globaloutcomes.phi.domain.model

/**
 * Survey Domain Model
 * Represents completed health survey with structured responses
 */
data class Survey(
    val id: String,
    val scanId: String,
    val patientId: String,
    val surveyType: SurveyType,
    val responses: Map<String, Any>, // Structured JSON responses
    val completedAt: Long,
    val syncStatus: SyncStatus
)

/**
 * Survey Types
 */
enum class SurveyType {
    NCD,           // Non-Communicable Diseases
    MATERNAL,      // Maternal Health
    INFECTIOUS,    // Infectious Diseases
    MENTAL_HEALTH  // Mental Health (PHQ-9)
}

/**
 * NCD Survey Response Model
 * Structured data for Non-Communicable Disease survey
 */
data class NcdSurveyResponse(
    // Diabetes Risk
    val hasDiabetes: Boolean? = null,
    val diabetesFamilyHistory: Boolean? = null,
    val frequentUrination: Boolean? = null,
    val excessiveThirst: Boolean? = null,
    val unexplainedWeightLoss: Boolean? = null,

    // Hypertension Risk
    val hasHypertension: Boolean? = null,
    val hypertensionFamilyHistory: Boolean? = null,
    val frequentHeadaches: Boolean? = null,
    val dizziness: Boolean? = null,

    // Cardiovascular Risk
    val heartDiseaseFamilyHistory: Boolean? = null,
    val chestPain: Boolean? = null,
    val shortnessOfBreath: Boolean? = null,

    // Lifestyle Factors
    val smokingStatus: SmokingStatus? = null,
    val cigarettesPerDay: Int? = null,
    val alcoholConsumption: AlcoholConsumption? = null,
    val physicalActivityLevel: Int? = null, // 1-5 scale
    val dietQuality: Int? = null, // 1-5 scale

    // Current Medications
    val currentMedications: List<String> = emptyList(),

    // Other Conditions
    val otherConditions: List<String> = emptyList()
)

enum class SmokingStatus {
    NEVER,
    FORMER,
    CURRENT
}

enum class AlcoholConsumption {
    NONE,
    OCCASIONAL,
    MODERATE,
    HEAVY
}

/**
 * Maternal Survey Response Model
 * Structured data for Maternal Health survey
 */
data class MaternalSurveyResponse(
    // Pregnancy Info
    val lmpDate: Long? = null, // Last Menstrual Period
    val gestationalAgeWeeks: Int? = null,
    val gravidity: Int? = null, // Total pregnancies
    val parity: Int? = null, // Live births
    val abortions: Int? = null,

    // Previous Pregnancy Complications
    val previousComplications: List<MaternalComplication> = emptyList(),

    // Current Symptoms
    val currentSymptoms: List<MaternalSymptom> = emptyList(),

    // Prenatal Care
    val prenatalVisits: Int? = null,
    val lastPrenatalVisit: Long? = null,

    // Supplementation
    val takingIronSupplements: Boolean? = null,
    val takingFolicAcid: Boolean? = null,
    val takingCalcium: Boolean? = null,

    // Immunization
    val tetanusToxoidDoses: Int? = null,

    // Risk Factors
    val maternalAge: Int? = null,
    val multiplePregnancy: Boolean? = null,
    val gestationalDiabetes: Boolean? = null,
    val preeclampsia: Boolean? = null
)

enum class MaternalComplication {
    PREECLAMPSIA,
    GESTATIONAL_DIABETES,
    PRETERM_LABOR,
    POSTPARTUM_HEMORRHAGE,
    CESAREAN_SECTION,
    STILLBIRTH,
    MISCARRIAGE
}

enum class MaternalSymptom {
    SEVERE_HEADACHE,
    BLURRED_VISION,
    ABDOMINAL_PAIN,
    VAGINAL_BLEEDING,
    DECREASED_FETAL_MOVEMENT,
    SEVERE_SWELLING,
    PERSISTENT_VOMITING,
    FEVER,
    DIFFICULTY_BREATHING
}

/**
 * Infectious Disease Survey Response Model
 */
data class InfectiousSurveyResponse(
    // TB Screening
    val persistentCough: Boolean? = null,
    val coughDuration: Int? = null, // weeks
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
    val travelDestinations: List<String> = emptyList()
)

/**
 * Mental Health Survey Response Model (PHQ-9)
 */
data class MentalHealthSurveyResponse(
    // PHQ-9 Questions (0-3 scale: Not at all, Several days, More than half, Nearly every day)
    val littleInterest: Int? = null,
    val feelingDown: Int? = null,
    val troubleSleeping: Int? = null,
    val feelingTired: Int? = null,
    val poorAppetite: Int? = null,
    val feelingBad: Int? = null,
    val troubleConcentrating: Int? = null,
    val movingSlow: Int? = null,
    val thoughtsHurting: Int? = null,

    // Total PHQ-9 Score (calculated)
    val totalScore: Int? = null,

    // Severity Level (calculated)
    val severityLevel: PhqSeverity? = null
)

enum class PhqSeverity {
    MINIMAL,        // 0-4
    MILD,           // 5-9
    MODERATE,       // 10-14
    MODERATELY_SEVERE, // 15-19
    SEVERE          // 20-27
}
