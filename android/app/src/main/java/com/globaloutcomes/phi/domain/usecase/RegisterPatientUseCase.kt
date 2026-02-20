package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Use case for registering a new patient
 * Validates data and saves to repository
 */
class RegisterPatientUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(patient: Patient): Result<String> {
        // Validate patient data
        if (patient.phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number is required"))
        }

        if (patient.firstName.isBlank() || patient.lastName.isBlank()) {
            return Result.failure(IllegalArgumentException("Name is required"))
        }

        if (patient.barangayId.isBlank()) {
            return Result.failure(IllegalArgumentException("Barangay is required"))
        }

        // Validate pregnant-specific fields
        if (patient.isPregnant) {
            if (patient.gestationalAgeWeeks == null || patient.gestationalAgeWeeks < 0 || patient.gestationalAgeWeeks > 42) {
                return Result.failure(IllegalArgumentException("Valid gestational age is required for pregnant patients"))
            }
        }

        // Save patient
        return patientRepository.insertPatient(patient)
    }
}
