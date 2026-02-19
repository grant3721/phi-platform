package com.globaloutcomes.phi.presentation.scan.result

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
import com.globaloutcomes.phi.domain.model.Biomarkers
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.GoRiskBadge
import com.globaloutcomes.phi.presentation.components.ButtonVariant
import com.globaloutcomes.phi.presentation.components.RiskLevel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Scan Result Screen - Displays all 34 biomarkers from completed scan
 * Organized by category: Cardiovascular, Respiratory, Bloodless Tests, HRV, ANS, Stress, Wellness
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    scanId: String,
    onNavigateToSurvey: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScanResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Signal quality indicator
                    state.scan?.let { scan ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${(scan.signalQuality * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(text = state.error!!)
                        GoButton(
                            text = "Back to Home",
                            onClick = onNavigateToHome,
                            variant = ButtonVariant.PRIMARY
                        )
                    }
                }
            }
            state.scan != null -> {
                ScanResultContent(
                    scan = state.scan!!,
                    isHighRisk = state.isHighRisk,
                    onNavigateToSurvey = { onNavigateToSurvey(scanId) },
                    onNavigateToHome = onNavigateToHome,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ScanResultContent(
    scan: com.globaloutcomes.phi.domain.model.Scan,
    isHighRisk: Boolean,
    onNavigateToSurvey: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Risk Alert (if applicable)
        if (isHighRisk) {
            HighRiskAlert()
        }

        // Overall Status Card
        OverallStatusCard(
            signalQuality = scan.signalQuality,
            scannedAt = scan.scannedAt,
            isHighRisk = isHighRisk
        )

        // Biomarker Categories
        CardiovascularSection(biomarkers = scan.biomarkers)
        RespiratorySection(biomarkers = scan.biomarkers)
        BloodlessTestsSection(biomarkers = scan.biomarkers)
        RiskIndicatorsSection(biomarkers = scan.biomarkers)
        HRVSection(biomarkers = scan.biomarkers)
        ANSSection(biomarkers = scan.biomarkers)
        StressSection(biomarkers = scan.biomarkers)
        WellnessSection(biomarkers = scan.biomarkers)

        // Actions
        Text(
            text = "Next Steps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        GoButton(
            text = "Continue to Health Survey",
            onClick = onNavigateToSurvey,
            variant = ButtonVariant.PRIMARY,
            icon = Icons.Filled.Check,
            modifier = Modifier.fillMaxWidth()
        )

        GoButton(
            text = "Back to Home",
            onClick = onNavigateToHome,
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HighRiskAlert() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "High-Risk Findings Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Please complete the health survey. Referral to health facility may be recommended.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun OverallStatusCard(
    signalQuality: Double,
    scannedAt: Long,
    isHighRisk: Boolean
) {
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
                    text = "Overall Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                GoRiskBadge(
                    level = if (isHighRisk) RiskLevel.HIGH_RISK else RiskLevel.NORMAL
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Signal Quality",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(signalQuality * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Scan Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(scannedAt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CardiovascularSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Cardiovascular") {
        BiomarkerRow("Pulse Rate", biomarkers.pulseRate, "bpm")
        BiomarkerRow("BP Systolic", biomarkers.bpSystolic, "mmHg")
        BiomarkerRow("BP Diastolic", biomarkers.bpDiastolic, "mmHg")
        BiomarkerRow("Mean Arterial Pressure", biomarkers.meanArterialPressure, "mmHg")
        BiomarkerRow("Pulse Pressure", biomarkers.pulsePressure, "mmHg")
        BiomarkerRow("Cardiac Workload", biomarkers.cardiacWorkload, "")
        BiomarkerRow("Heart Age", biomarkers.heartAge, "years")
    }
}

@Composable
private fun RespiratorySection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Respiratory") {
        BiomarkerRow("Respiration Rate", biomarkers.respirationRate, "breaths/min")
        BiomarkerRow("Oxygen Saturation", biomarkers.oxygenSaturation, "%")
    }
}

@Composable
private fun BloodlessTestsSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Bloodless Blood Tests") {
        BiomarkerRow("Hemoglobin", biomarkers.hemoglobin, "g/dL")
        BiomarkerRow("Hemoglobin A1c", biomarkers.hemoglobinA1c, "%")
    }
}

@Composable
private fun RiskIndicatorsSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Risk Indicators") {
        BiomarkerRow("ASCVD Risk", biomarkers.ascvdRisk, "")
        biomarkers.ascvdRiskLevel?.let {
            BiomarkerRow("ASCVD Risk Level", it, "")
        }
        BiomarkerRow("High Blood Pressure Risk", biomarkers.highBloodPressureRisk, "")
        BiomarkerRow("High Fasting Glucose Risk", biomarkers.highFastingGlucoseRisk, "")
        BiomarkerRow("High HbA1c Risk", biomarkers.highHemoglobinA1cRisk, "")
        BiomarkerRow("High Total Cholesterol Risk", biomarkers.highTotalCholesterolRisk, "")
        BiomarkerRow("Low Hemoglobin Risk", biomarkers.lowHemoglobinRisk, "")
    }
}

@Composable
private fun HRVSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Heart Rate Variability (HRV)") {
        BiomarkerRow("Mean RRI", biomarkers.meanRri, "ms")
        BiomarkerRow("RRI", biomarkers.rri, "ms")
        BiomarkerRow("SDNN", biomarkers.sdnn, "ms")
        BiomarkerRow("RMSSD", biomarkers.rmssd, "ms")
        BiomarkerRow("SD1", biomarkers.sd1, "ms")
        BiomarkerRow("SD2", biomarkers.sd2, "ms")
        BiomarkerRow("PRQ", biomarkers.prq, "")
        BiomarkerRow("LF/HF", biomarkers.lfhf, "")
    }
}

@Composable
private fun ANSSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Autonomic Nervous System (ANS)") {
        BiomarkerRow("PNS Index", biomarkers.pnsIndex, "")
        biomarkers.pnsZone?.let {
            BiomarkerRow("PNS Zone", it, "")
        }
        BiomarkerRow("SNS Index", biomarkers.snsIndex, "")
        biomarkers.snsZone?.let {
            BiomarkerRow("SNS Zone", it, "")
        }
    }
}

@Composable
private fun StressSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Stress Assessment") {
        biomarkers.stressLevel?.let {
            BiomarkerRow("Stress Level", it, "")
        }
        BiomarkerRow("Stress Index", biomarkers.stressIndex, "")
        BiomarkerRow("Normalized Stress Index", biomarkers.normalizedStressIndex, "")
    }
}

@Composable
private fun WellnessSection(biomarkers: Biomarkers) {
    BiomarkerSection(title = "Overall Wellness") {
        BiomarkerRow("Wellness Index", biomarkers.wellnessIndex, "")
        biomarkers.wellnessLevel?.let {
            BiomarkerRow("Wellness Level", it, "")
        }
    }
}

@Composable
private fun BiomarkerSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        GoCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun BiomarkerRow(
    label: String,
    value: Double?,
    unit: String
) {
    if (value != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${String.format("%.1f", value)} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BiomarkerRow(
    label: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(millis: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}
