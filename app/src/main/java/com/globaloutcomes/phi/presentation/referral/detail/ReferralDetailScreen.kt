package com.globaloutcomes.phi.presentation.referral.detail

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
import com.globaloutcomes.phi.domain.model.ReferralStatus
import com.globaloutcomes.phi.domain.model.ReferralTier
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import java.text.SimpleDateFormat
import java.util.*

/**
 * Referral Detail Screen
 * Shows full details of a referral and allows status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralDetailScreen(
    referralId: String,
    onNavigateBack: () -> Unit,
    viewModel: ReferralDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    var showResolveDialog by remember { mutableStateOf(false) }
    var showEscalateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(referralId) {
        viewModel.loadReferral(referralId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Referral Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            state.referral != null -> {
                val referral = state.referral!!
                val patient = state.patient
                val scan = state.scan
                val isOverdue = referral.status == ReferralStatus.OVERDUE ||
                        (referral.status == ReferralStatus.PENDING && referral.dueBy < System.currentTimeMillis())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status header
                    StatusHeader(
                        status = referral.status,
                        tier = referral.tier,
                        isOverdue = isOverdue
                    )

                    // Patient info
                    if (patient != null) {
                        PatientInfoCard(patient = patient)
                    }

                    // Risk flags
                    if (referral.riskFlags.isNotEmpty()) {
                        RiskFlagsCard(riskFlags = referral.riskFlags)
                    }

                    // Scan data
                    if (scan != null) {
                        ScanDataCard(scan = scan)
                    }

                    // Timing info
                    TimingCard(
                        referredAt = referral.referredAt,
                        dueBy = referral.dueBy,
                        resolvedAt = referral.resolvedAt,
                        isOverdue = isOverdue
                    )

                    // Notes
                    if (!referral.notes.isNullOrBlank()) {
                        NotesCard(
                            title = "Initial Notes",
                            notes = referral.notes
                        )
                    }

                    // Resolution notes
                    if (!referral.resolutionNotes.isNullOrBlank()) {
                        NotesCard(
                            title = "Resolution Notes",
                            notes = referral.resolutionNotes
                        )
                    }

                    // Action buttons (only for pending/overdue)
                    if (referral.status == ReferralStatus.PENDING || referral.status == ReferralStatus.OVERDUE) {
                        ActionButtons(
                            onResolve = { showResolveDialog = true },
                            onEscalate = { showEscalateDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Resolve dialog
    if (showResolveDialog) {
        ResolveDialog(
            onDismiss = { showResolveDialog = false },
            onConfirm = { notes ->
                viewModel.resolveReferral(notes)
                showResolveDialog = false
            }
        )
    }

    // Escalate dialog
    if (showEscalateDialog) {
        EscalateDialog(
            currentTier = state.referral?.tier ?: ReferralTier.BHW_TO_BHS,
            onDismiss = { showEscalateDialog = false },
            onConfirm = { notes ->
                viewModel.escalateReferral(notes)
                showEscalateDialog = false
            }
        )
    }
}

@Composable
private fun StatusHeader(
    status: ReferralStatus,
    tier: ReferralTier,
    isOverdue: Boolean
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (statusText, containerColor, contentColor) = when {
                    isOverdue -> Triple(
                        "OVERDUE",
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer
                    )
                    status == ReferralStatus.PENDING -> Triple(
                        "PENDING",
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    status == ReferralStatus.CONFIRMED -> Triple(
                        "CONFIRMED",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    status == ReferralStatus.RESOLVED -> Triple(
                        "RESOLVED",
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    status == ReferralStatus.ESCALATED -> Triple(
                        "ESCALATED",
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    else -> Triple(
                        status.name,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = containerColor,
                        labelColor = contentColor
                    )
                )

                Text(
                    text = formatTier(tier),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PatientInfoCard(patient: com.globaloutcomes.phi.domain.model.Patient) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Patient Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            InfoRow(label = "Name", value = patient.fullName)
            InfoRow(label = "Phone", value = patient.phoneNumber)
            InfoRow(label = "Sex", value = patient.sex.name)
            if (patient.isPregnant) {
                InfoRow(
                    label = "Status",
                    value = "Pregnant (${patient.gestationalAgeWeeks} weeks)"
                )
            }
        }
    }
}

@Composable
private fun RiskFlagsCard(riskFlags: List<String>) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Risk Flags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Divider()

            riskFlags.forEach { flag ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = flag,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanDataCard(scan: com.globaloutcomes.phi.domain.model.Scan) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Scan Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            InfoRow(label = "Scan Date", value = formatDate(scan.performedAt))
            InfoRow(label = "Duration", value = "${scan.durationSeconds}s")
            InfoRow(label = "Signal Quality", value = scan.signalQuality ?: "N/A")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Key Vitals",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            scan.systolicBP?.let { InfoRow(label = "BP", value = "$it/${scan.diastolicBP} mmHg") }
            scan.heartRateBPM?.let { InfoRow(label = "Heart Rate", value = "$it bpm") }
            scan.spo2?.let { InfoRow(label = "SpO₂", value = "$it%") }
            scan.hemoglobin?.let { InfoRow(label = "Hemoglobin", value = "$it g/dL") }
        }
    }
}

@Composable
private fun TimingCard(
    referredAt: Long,
    dueBy: Long,
    resolvedAt: Long?,
    isOverdue: Boolean
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            InfoRow(label = "Referred", value = formatDate(referredAt))
            InfoRow(
                label = if (isOverdue) "Was Due" else "Due By",
                value = formatDate(dueBy),
                valueColor = if (isOverdue) MaterialTheme.colorScheme.error else null
            )

            if (resolvedAt != null) {
                InfoRow(label = "Resolved", value = formatDate(resolvedAt))
            }
        }
    }
}

@Composable
private fun NotesCard(title: String, notes: String) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onResolve: () -> Unit,
    onEscalate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GoButton(
            text = "Resolved at This Level",
            onClick = onResolve,
            modifier = Modifier.fillMaxWidth(),
            variant = "secondary"
        )

        GoButton(
            text = "Escalate to Next Tier",
            onClick = onEscalate,
            modifier = Modifier.fillMaxWidth(),
            variant = "primary"
        )
    }
}

@Composable
private fun ResolveDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resolve Referral") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add resolution notes:")
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter notes...") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(notes) },
                enabled = notes.isNotBlank()
            ) {
                Text("Resolve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EscalateDialog(
    currentTier: ReferralTier,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    val nextTier = when (currentTier) {
        ReferralTier.BHW_TO_BHS -> ReferralTier.BHS_TO_RHU
        ReferralTier.BHS_TO_RHU -> ReferralTier.RHU_TO_HOSPITAL
        ReferralTier.RHU_TO_HOSPITAL -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escalate Referral") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (nextTier != null) {
                    Text("Escalate to: ${formatTier(nextTier)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add escalation notes:")
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter notes...") },
                        minLines = 3
                    )
                } else {
                    Text("This referral is already at the highest tier.")
                }
            }
        },
        confirmButton = {
            if (nextTier != null) {
                TextButton(
                    onClick = { onConfirm(notes) },
                    enabled = notes.isNotBlank()
                ) {
                    Text("Escalate")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
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
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTier(tier: ReferralTier): String {
    return when (tier) {
        ReferralTier.BHW_TO_BHS -> "BHW → BHS"
        ReferralTier.BHS_TO_RHU -> "BHS → RHU"
        ReferralTier.RHU_TO_HOSPITAL -> "RHU → Hospital"
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}
