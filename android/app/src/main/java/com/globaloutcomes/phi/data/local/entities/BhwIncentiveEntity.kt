package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * BHW Incentive entity for tracking earnings
 * ₱3.00 per validated scan, ₱150.00 daily cap (50 scans max)
 */
@Entity(
    tableName = "bhw_incentives",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bhwId"]),
        Index(value = ["scanId"]),
        Index(value = ["earnedDate"]),
        Index(value = ["status"]),
        Index(value = ["syncStatus"])
    ]
)
data class BhwIncentiveEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val bhwId: String, // From auth token
    val scanId: String,
    val patientId: String, // Denormalized for reporting

    // Earnings
    val amount: Float = 3.00f, // ₱3.00 per scan
    val earnedDate: Long = System.currentTimeMillis(), // Date only (start of day)
    val earnedAt: Long = System.currentTimeMillis(), // Exact timestamp

    // Status
    val status: String, // EARNED, REJECTED, PAID
    val rejectionReason: String? = null, // DAILY_CAP_REACHED, DUPLICATE, INVALID_SCAN

    // Daily Cap Tracking
    val daySequence: Int, // 1-50, position in daily cap
    val dailyTotal: Float, // Running total for the day

    // Payment
    val paidAt: Long? = null,
    val paymentBatch: String? = null,
    val paymentReference: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING",
    val syncedAt: Long? = null,
    val serverIncentiveId: String? = null
)
