package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.data.local.preferences.DemoSettingsRepository
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.PatientRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for checking if patient already exists (deduplication)
 * Uses phone number as unique identifier
 *
 * Can be bypassed in demo mode for testing
 */
class CheckDuplicateUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val demoSettingsRepository: DemoSettingsRepository
) {
    suspend operator fun invoke(phoneNumber: String): Result<DuplicateResult> {
        return try {
            // Check if dedup bypass is enabled (demo mode)
            val dedupBypassed = demoSettingsRepository.isDedupBypassed.first()

            if (dedupBypassed) {
                // Demo mode: always allow registration
                return Result.success(
                    DuplicateResult(
                        isDuplicate = false,
                        existingPatient = null,
                        message = "Demo mode: Dedup check bypassed"
                    )
                )
            }

            // Normal mode: check for duplicate
            val existingPatient = patientRepository.getPatientByPhoneNumber(phoneNumber).getOrNull()

            if (existingPatient != null) {
                Result.success(
                    DuplicateResult(
                        isDuplicate = true,
                        existingPatient = existingPatient,
                        message = "Patient with phone number $phoneNumber already exists"
                    )
                )
            } else {
                Result.success(
                    DuplicateResult(
                        isDuplicate = false,
                        existingPatient = null,
                        message = "No existing patient found"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class DuplicateResult(
    val isDuplicate: Boolean,
    val existingPatient: Patient?,
    val message: String
)
