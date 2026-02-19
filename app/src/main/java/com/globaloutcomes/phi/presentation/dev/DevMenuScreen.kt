package com.globaloutcomes.phi.presentation.dev

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.ButtonVariant

/**
 * Dev Menu Screen - Development utilities
 *
 * Features:
 * - Dedup bypass toggle (for rapid testing)
 * - Test patient generation (realistic fake data)
 * - Database reset (clear all data)
 * - Demo mode banner indicator
 *
 * Access: Long-press on app version in Settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevMenuScreen(
    onNavigateBack: () -> Unit,
    viewModel: DevMenuViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Menu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning Banner
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
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text(
                            text = "Development Mode Only",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "These features are for testing only. Do not use in production.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Demo Mode Toggle
            Text(
                text = "Demo Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bypass Duplicate Check",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Allow registering patients with duplicate phone numbers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.dedupBypassEnabled,
                            onCheckedChange = viewModel::toggleDedupBypass
                        )
                    }

                    if (state.dedupBypassEnabled) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Yellow banner will appear on registration screen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Test Data Generation
            Text(
                text = "Test Data Generation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Generate realistic test patients with random data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoButton(
                            text = "Generate 1 Patient",
                            onClick = { viewModel.generateTestPatients(1) },
                            enabled = !state.isGenerating,
                            isLoading = state.isGenerating,
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )

                        GoButton(
                            text = "Generate 10",
                            onClick = { viewModel.generateTestPatients(10) },
                            enabled = !state.isGenerating,
                            isLoading = state.isGenerating,
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.generatedCount > 0) {
                        Text(
                            text = "✓ Generated ${state.generatedCount} test patients",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Database Management
            Text(
                text = "Database Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Current Database Stats:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Patients:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${state.totalPatients}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Scans:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${state.totalScans}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider()

                    GoButton(
                        text = "Clear All Data",
                        onClick = { viewModel.showResetConfirmation() },
                        variant = ButtonVariant.DANGER,
                        icon = Icons.Filled.Delete,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "⚠️ This will delete all patients, scans, and surveys. Cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Status Messages
            if (state.statusMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isError)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = state.statusMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Reset Confirmation Dialog
        if (state.showResetDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideResetConfirmation() },
                icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
                title = { Text("Clear All Data?") },
                text = {
                    Text("This will permanently delete all patients, scans, and surveys. This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetDatabase()
                            viewModel.hideResetConfirmation()
                        }
                    ) {
                        Text("Delete All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideResetConfirmation() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
