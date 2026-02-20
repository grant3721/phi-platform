package com.globaloutcomes.phi.domain.model

import java.util.UUID

/**
 * Referral Domain Model
 * Three-tier referral system: BHW → BHS → RHU
 */
data class Referral(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val scanId: String,
    val tier: ReferralTier,
    val status: ReferralStatus,
    val riskLevel: RiskLevel,
    val riskFlags: List<String>,
    val referredBy: String = "", // BHW/BHS user ID
    val referredAt: Long = System.currentTimeMillis(),
    val dueBy: Long, // 48 hours from referredAt
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolutionNotes: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Referral Tier
 * Three-tier system for escalation
 */
enum class ReferralTier {
    BHW_TO_BHS,     // BHW refers patient to Barangay Health Station
    BHS_TO_RHU,     // BHS escalates to Rural Health Unit
    RHU_TO_HOSPITAL // RHU escalates to hospital
}

/**
 * Referral Status
 */
enum class ReferralStatus {
    PENDING,        // Awaiting action
    CONFIRMED,      // Acknowledged by receiving facility
    IN_PROGRESS,    // Patient being seen
    RESOLVED,       // Completed at this level
    ESCALATED,      // Escalated to next tier
    OVERDUE,        // Past due date without action
    CANCELLED       // Cancelled (duplicate, error, etc.)
}
