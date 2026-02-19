package com.globaloutcomes.phi.presentation.patient.registration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Registration Screen - Placeholder
 * Full implementation coming next (all patient registration fields)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateToScan: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Patient") }
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
                text = "Registration Screen - Coming Soon",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
