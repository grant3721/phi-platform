package com.globaloutcomes.phi.domain.usecase.patient

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Check Duplicate Patient Use Case
 * Checks if a patient with the given phone number already exists
 */
class CheckDuplicateUseCase @Inject constructor(
    private val patientRepository: PatientRepository
) {

    suspend operator fun invoke(phoneNumber: String): Result<Patient?> {
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        }

        return patientRepository.findByPhone(phoneNumber)
    }
}
