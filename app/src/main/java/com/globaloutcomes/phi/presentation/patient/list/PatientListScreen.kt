package com.globaloutcomes.phi.presentation.patient.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Patient List Screen - Placeholder
 * Full implementation in later phases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    onNavigateToPatientDetail: (String) -> Unit,
    onNavigateToNewPatient: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Patients") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Patient List - Coming Soon",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
