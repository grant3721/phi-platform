package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * BHW Incentive Entity - Earnings Tracker
 * ₱3.00 per validated scan, ₱150.00 daily cap
 */
@Entity(tableName = "bhw_incentives")
data class BhwIncentiveEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bhwId: String,
    val scanId: String,
    val date: Long,                         // epoch millis, date only (start of day)
    val amount: Double,                     // 3.0 or 0.0
    val status: String,                     // "earned" | "rejected" | "paid"
    val rejectionReason: String? = null,
    val dailyRunningTotal: Double,
    val createdAt: Long = System.currentTimeMillis()
)
