package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import javax.inject.Inject

/**
 * Use case to delete a patient and all associated data
 *
 * NOTE: This is a destructive operation. In production, consider:
 * - Soft delete (mark as deleted, keep data)
 * - Archive instead of delete
 * - Sync with backend before deletion
 */
class DeletePatientUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository
) {
    suspend operator fun invoke(patientId: String): Result<Unit> {
        return try {
            // Check if patient exists
            val patient = patientRepository.getPatientById(patientId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Patient not found"))

            // Delete all scans for this patient
            // Note: Room cascade delete would handle this, but we're explicit for clarity
            scanRepository.getScansForPatient(patientId).getOrNull()?.forEach { scan ->
                scanRepository.deleteScan(scan.id)
            }

            // Delete the patient
            patientRepository.deletePatient(patientId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
