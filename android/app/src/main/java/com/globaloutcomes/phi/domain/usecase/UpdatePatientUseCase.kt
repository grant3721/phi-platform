package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Use case to update an existing patient's information
 */
class UpdatePatientUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(patient: Patient): Result<Unit> {
        return try {
            // Validate patient data
            if (patient.phoneNumber.isBlank()) {
                return Result.failure(IllegalArgumentException("Phone number is required"))
            }

            if (patient.firstName.isBlank()) {
                return Result.failure(IllegalArgumentException("First name is required"))
            }

            if (patient.lastName.isBlank()) {
                return Result.failure(IllegalArgumentException("Last name is required"))
            }

            // Validate pregnancy data
            if (patient.isPregnant && patient.gestationalAgeWeeks == null) {
                return Result.failure(IllegalArgumentException("Gestational age is required for pregnant patients"))
            }

            if (patient.gestationalAgeWeeks != null && (patient.gestationalAgeWeeks!! < 0 || patient.gestationalAgeWeeks!! > 42)) {
                return Result.failure(IllegalArgumentException("Gestational age must be between 0 and 42 weeks"))
            }

            // Update patient in repository
            patientRepository.updatePatient(patient)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
