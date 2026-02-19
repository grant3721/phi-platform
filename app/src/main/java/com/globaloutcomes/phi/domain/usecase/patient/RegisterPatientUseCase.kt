package com.globaloutcomes.phi.domain.usecase.patient

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.PatientRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Register Patient Use Case
 * Registers a new patient with validation
 */
class RegisterPatientUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val checkDuplicateUseCase: CheckDuplicateUseCase
) {

    suspend operator fun invoke(
        phoneNumber: String,
        firstName: String,
        lastName: String,
        dateOfBirth: Long,
        sex: String,
        barangayCode: String,
        municipalityCode: String,
        philhealthNumber: String? = null,
        philsysId: String? = null,
        pregnant: Boolean = false,
        gestationalAgeWeeks: Int? = null,
        messengerOptIn: Boolean = false,
        messengerContactMethod: String? = null,
        messengerContactValue: String? = null
    ): Result<Patient> {
        // Validate required fields
        if (phoneNumber.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            return Result.failure(IllegalArgumentException("Required fields cannot be empty"))
        }

        // Check for duplicates
        val duplicateCheck = checkDuplicateUseCase(phoneNumber)
        if (duplicateCheck.isSuccess && duplicateCheck.getOrNull() != null) {
            return Result.failure(DuplicatePatientException("Patient already registered with this phone number"))
        }

        // Create patient
        val now = System.currentTimeMillis()
        val patient = Patient(
            id = UUID.randomUUID().toString(),
            phoneNumber = phoneNumber,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            sex = sex,
            barangayCode = barangayCode,
            municipalityCode = municipalityCode,
            philhealthNumber = philhealthNumber,
            philsysId = philsysId,
            pregnant = pregnant,
            gestationalAgeWeeks = gestationalAgeWeeks,
            highRiskFlag = false,
            highRiskReasons = null,
            maternalHighRisk = false,
            lastScanDate = null,
            totalScans = 0,
            messengerOptIn = messengerOptIn,
            messengerContactMethod = messengerContactMethod,
            messengerContactValue = messengerContactValue,
            messengerPsid = null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )

        // Insert patient
        return patientRepository.insert(patient).map { patient }
    }
}

class DuplicatePatientException(message: String) : Exception(message)
