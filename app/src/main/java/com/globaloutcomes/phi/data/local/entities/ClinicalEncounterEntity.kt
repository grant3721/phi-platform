package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Clinical Encounter Entity - BHS/RHU Clinical Documentation
 * Includes manual vitals, diagnosis, treatment, and maternal-specific fields
 */
@Entity(tableName = "clinical_encounters")
data class ClinicalEncounterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val encounterType: String,              // "fpe" | "follow_up" | "maternal" | "emergency"
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

    // Clinical notes
    val chiefComplaint: String? = null,
    val historyOfPresentIllness: String? = null,
    val pastMedicalHistory: String? = null,
    val physicalExam: String? = null,       // JSON structured by body system
    val diagnosisIcd10: String? = null,     // JSON array of ICD-10 codes
    val diagnosisText: String? = null,      // JSON array of text descriptions
    val medicationsPrescribed: String? = null, // JSON: [{drug,dosage,frequency,duration}]
    val labResults: String? = null,         // JSON: [{test,value,unit,date}]

    // Maternal-specific
    val fundalHeight: Double? = null,
    val fetalHeartRate: Double? = null,
    val fetalPresentation: String? = null,
    val edemaAssessment: String? = null,
    val proteinuria: String? = null,
    val ogttResults: String? = null,        // JSON: {fasting,oneHr,twoHr}
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
