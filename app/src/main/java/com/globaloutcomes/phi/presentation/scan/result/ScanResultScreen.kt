package com.globaloutcomes.phi.presentation.scan.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Scan Result Screen - Placeholder
 * Full implementation coming next (displays all 34 biomarkers)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    scanId: String,
    onNavigateToSurvey: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Results") }
            )
        }
    ) { padding ->
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
                    text = "Scan Result Screen - Coming Soon",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Scan ID: $scanId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
