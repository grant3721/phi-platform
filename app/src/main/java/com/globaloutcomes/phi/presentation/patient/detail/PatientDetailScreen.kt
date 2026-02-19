package com.globaloutcomes.phi.presentation.patient.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import java.text.SimpleDateFormat
import java.util.*

/**
 * Patient Detail Screen
 * Shows comprehensive patient information including scans, surveys, and referrals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patientId: String,
    onNavigateToScan: (String) -> Unit,
    onNavigateToScanResult: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PatientDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(patientId) {
        viewModel.loadPatient(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.patient != null) {
                        IconButton(onClick = { onNavigateToEdit(patientId) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.patient != null && !state.isLoading) {
                FloatingActionButton(
                    onClick = { onNavigateToScan(patientId) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Scan")
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadPatient(patientId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            state.patient != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Patient Info Card
                    PatientInfoCard(patient = state.patient!!)

                    // High-Risk Alert (if applicable)
                    if (state.patient!!.highRiskFlag) {
                        HighRiskAlertCard(
                            riskReasons = state.patient!!.highRiskReasons,
                            maternalHighRisk = state.patient!!.maternalHighRisk
                        )
                    }

                    // Quick Stats
                    QuickStatsCard(
                        totalScans = state.scans.size,
                        activeReferrals = state.activeReferralsCount,
                        completedSurveys = state.completedSurveysCount
                    )

                    // Latest Scan Summary (if available)
                    if (state.scans.isNotEmpty()) {
                        val latestScan = state.scans.first()
                        LatestScanCard(
                            scan = latestScan,
                            onClick = { onNavigateToScanResult(latestScan.id) }
                        )
                    }

                    // Scans Timeline
                    if (state.scans.isNotEmpty()) {
                        ScansTimelineCard(
                            scans = state.scans,
                            onScanClick = { scanId -> onNavigateToScanResult(scanId) }
                        )
                    } else {
                        NoScansCard()
                    }

                    // Referral History
                    if (state.referrals.isNotEmpty()) {
                        ReferralHistoryCard(referrals = state.referrals)
                    }

                    // Rescan Eligibility
                    RescanEligibilityCard(
                        eligibility = state.rescanEligibility,
                        onScanClick = { onNavigateToScan(patientId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientInfoCard(patient: Patient) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (patient.isPregnant) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Pregnant") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            Divider()

            // Demographics
            InfoRow(label = "Phone", value = patient.phoneNumber)
            InfoRow(label = "Age", value = "${patient.age} years")
            InfoRow(label = "Sex", value = patient.sex.name)
            InfoRow(label = "Barangay", value = patient.barangayName)

            if (patient.philHealthNumber != null) {
                InfoRow(label = "PhilHealth", value = patient.philHealthNumber!!)
            }

            // Pregnancy info (if pregnant)
            if (patient.isPregnant && patient.gestationalAgeWeeks != null) {
                Divider()
                Text(
                    text = "Pregnancy Information",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                InfoRow(
                    label = "Gestational Age",
                    value = "${patient.gestationalAgeWeeks} weeks"
                )
                if (patient.expectedDeliveryDate != null) {
                    InfoRow(
                        label = "Expected Delivery",
                        value = formatDate(patient.expectedDeliveryDate!!)
                    )
                }
            }

            // Messenger opt-in
            if (patient.messengerOptIn) {
                Divider()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Messenger notifications enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HighRiskAlertCard(
    riskReasons: List<String>,
    maternalHighRisk: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (maternalHighRisk) "HIGH-RISK PREGNANCY" else "HIGH-RISK PATIENT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            if (riskReasons.isNotEmpty()) {
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    riskReasons.forEach { reason ->
                        Text(
                            text = "• $reason",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    totalScans: Int,
    activeReferrals: Int,
    completedSurveys: Int
) {
    GoCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Filled.Favorite,
                label = "Scans",
                value = totalScans.toString()
            )
            StatItem(
                icon = Icons.Filled.Warning,
                label = "Referrals",
                value = activeReferrals.toString(),
                color = if (activeReferrals > 0) MaterialTheme.colorScheme.error else null
            )
            StatItem(
                icon = Icons.Filled.CheckCircle,
                label = "Surveys",
                value = completedSurveys.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color ?: MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LatestScanCard(
    scan: Scan,
    onClick: () -> Unit
) {
    GoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latest Scan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClick) {
                    Text("View Full Results")
                }
            }

            Divider()

            Text(
                text = formatDate(scan.scannedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Key vitals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    scan.systolicBP?.let {
                        VitalItem("BP", "$it/${scan.diastolicBP} mmHg")
                    }
                    scan.heartRateBPM?.let {
                        VitalItem("HR", "$it bpm")
                    }
                }
                Column {
                    scan.spo2?.let {
                        VitalItem("SpO₂", "$it%")
                    }
                    scan.respiratoryRate?.let {
                        VitalItem("RR", "$it /min")
                    }
                }
            }

            if (scan.riskFlags.isNotEmpty()) {
                Divider()
                Text(
                    text = "${scan.riskFlags.size} risk flag(s) detected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun VitalItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ScansTimelineCard(
    scans: List<Scan>,
    onScanClick: (String) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Scan History (${scans.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            scans.take(5).forEach { scan ->
                ScanTimelineItem(
                    scan = scan,
                    onClick = { onScanClick(scan.id) }
                )
            }

            if (scans.size > 5) {
                Text(
                    text = "+${scans.size - 5} more scans",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ScanTimelineItem(
    scan: Scan,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(scan.scannedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (scan.riskFlags.isNotEmpty()) {
                    Text(
                        text = "${scan.riskFlags.size} risk flag(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "View scan",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoScansCard() {
    GoCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No Scans Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tap the + button to perform the first scan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReferralHistoryCard(referrals: List<com.globaloutcomes.phi.domain.model.Referral>) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Referrals (${referrals.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            referrals.take(3).forEach { referral ->
                ReferralHistoryItem(referral = referral)
            }

            if (referrals.size > 3) {
                Text(
                    text = "+${referrals.size - 3} more referrals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ReferralHistoryItem(referral: com.globaloutcomes.phi.domain.model.Referral) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatTier(referral.tier),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatDate(referral.referredAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AssistChip(
            onClick = {},
            label = {
                Text(
                    text = referral.status.name,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
    }
}

@Composable
private fun RescanEligibilityCard(
    eligibility: String?,
    onScanClick: () -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rescan Eligibility",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            if (eligibility != null) {
                Text(
                    text = eligibility,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (eligibility.contains("eligible", ignoreCase = true) &&
                    !eligibility.contains("not eligible", ignoreCase = true)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GoButton(
                        text = "Perform New Scan",
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text(
                    text = "Loading eligibility...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTier(tier: com.globaloutcomes.phi.domain.model.ReferralTier): String {
    return when (tier) {
        com.globaloutcomes.phi.domain.model.ReferralTier.BHW_TO_BHS -> "BHW → BHS"
        com.globaloutcomes.phi.domain.model.ReferralTier.BHS_TO_RHU -> "BHS → RHU"
        com.globaloutcomes.phi.domain.model.ReferralTier.RHU_TO_HOSPITAL -> "RHU → Hospital"
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
