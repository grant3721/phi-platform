package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Clinical Encounter entity for documentation by BHS/RHU providers
 * Captures confirmatory vitals, diagnosis, treatment, and YAKAP claim data
 */
@Entity(
    tableName = "clinical_encounters",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["patientId"]),
        Index(value = ["scanId"]),
        Index(value = ["encounterDate"]),
        Index(value = ["facilityType"]),
        Index(value = ["syncStatus"])
    ]
)
data class ClinicalEncounterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val patientId: String,
    val scanId: String? = null, // May be standalone encounter

    // Encounter Metadata
    val encounterDate: Long = System.currentTimeMillis(),
    val encounterType: String, // CONSULTATION, FOLLOW_UP, EMERGENCY, MATERNAL
    val facilityType: String, // BHS, RHU, HOSPITAL
    val providerId: String,
    val providerName: String,

    // Manual Vitals (Confirmatory)
    val systolicBp: Float? = null,
    val diastolicBp: Float? = null,
    val heartRate: Float? = null,
    val temperature: Float? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val bmi: Float? = null,

    // Clinical Notes (Voice or Text)
    val chiefComplaint: String? = null,
    val historyPresentIllness: String? = null,
    val pastMedicalHistory: String? = null,
    val physicalExamFindings: String, // JSON object by system

    // Diagnosis
    val diagnosisIcd10: String, // ICD-10 code
    val diagnosisText: String,
    val diagnosisCertainty: String, // CONFIRMED, PROVISIONAL, SUSPECTED

    // Treatment
    val medications: String, // JSON array of {drug, dose, frequency, duration}
    val referralToFacility: String? = null,
    val followUpDate: Long? = null,
    val followUpInstructions: String? = null,

    // Lab Results (if any)
    val labResults: String? = null, // JSON object

    // Maternal-Specific (if applicable)
    val fundalHeight: Float? = null,
    val fetalHeartRate: Float? = null,
    val fetalPresentation: String? = null,
    val edemaAssessment: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING",
    val syncedAt: Long? = null,
    val serverEncounterId: String? = null
)
