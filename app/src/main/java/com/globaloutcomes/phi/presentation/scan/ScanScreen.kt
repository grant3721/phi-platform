package com.globaloutcomes.phi.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Scan Screen - Placeholder
 * Full implementation coming next (with CameraX + StubScanEngine)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    patientId: String,
    onNavigateToResult: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Scan") }
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
                    text = "Scan Screen - Coming Soon",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Patient ID: $patientId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
