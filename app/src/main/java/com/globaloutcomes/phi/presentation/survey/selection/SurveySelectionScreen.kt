package com.globaloutcomes.phi.presentation.survey.selection

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
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard

/**
 * Survey Selection Screen
 * Determines which surveys should be completed based on patient status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveySelectionScreen(
    scanId: String,
    onNavigateToNcdSurvey: (String) -> Unit,
    onNavigateToMaternalSurvey: (String) -> Unit,
    onNavigateToInfectiousSurvey: (String) -> Unit,
    onNavigateToMentalHealthSurvey: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SurveySelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(scanId) {
        viewModel.loadScanAndPatient(scanId)
    }

    // Navigate to home if all surveys completed
    LaunchedEffect(state.allSurveysCompleted) {
        if (state.allSurveysCompleted) {
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Surveys") },
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadScanAndPatient(scanId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Instructions
                    GoCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Complete Health Surveys",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Please complete the following surveys to build a comprehensive health profile.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress indicator
                    if (state.totalSurveys > 0) {
                        ProgressCard(
                            completed = state.completedSurveys,
                            total = state.totalSurveys
                        )
                    }

                    // Required Surveys Section
                    if (state.requiredSurveys.isNotEmpty()) {
                        Text(
                            text = "Required Surveys",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        state.requiredSurveys.forEach { survey ->
                            SurveyCard(
                                survey = survey,
                                onStart = {
                                    when (survey.type) {
                                        SurveyType.NCD -> onNavigateToNcdSurvey(scanId)
                                        SurveyType.MATERNAL -> onNavigateToMaternalSurvey(scanId)
                                        SurveyType.INFECTIOUS -> onNavigateToInfectiousSurvey(scanId)
                                        SurveyType.MENTAL_HEALTH -> onNavigateToMentalHealthSurvey(scanId)
                                    }
                                }
                            )
                        }
                    }

                    // Optional Surveys Section
                    if (state.optionalSurveys.isNotEmpty()) {
                        Text(
                            text = "Optional Surveys",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        state.optionalSurveys.forEach { survey ->
                            SurveyCard(
                                survey = survey,
                                onStart = {
                                    when (survey.type) {
                                        SurveyType.NCD -> onNavigateToNcdSurvey(scanId)
                                        SurveyType.MATERNAL -> onNavigateToMaternalSurvey(scanId)
                                        SurveyType.INFECTIOUS -> onNavigateToInfectiousSurvey(scanId)
                                        SurveyType.MENTAL_HEALTH -> onNavigateToMentalHealthSurvey(scanId)
                                    }
                                }
                            )
                        }
                    }

                    // Skip button (if all required surveys completed)
                    if (state.canSkipRemaining) {
                        OutlinedButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Skip Optional Surveys")
                        }
                    }

                    // Complete button (if all surveys done)
                    if (state.allSurveysCompleted) {
                        GoButton(
                            text = "Complete",
                            onClick = onNavigateToHome,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(completed: Int, total: Int) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Survey Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { completed.toFloat() / total.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SurveyCard(
    survey: SurveyInfo,
    onStart: () -> Unit
) {
    GoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        survey.icon,
                        contentDescription = null,
                        tint = if (survey.completed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = survey.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = survey.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "~${survey.estimatedMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (survey.completed) {
                AssistChip(
                    onClick = {},
                    label = { Text("Completed") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                Button(onClick = onStart) {
                    Text("Start")
                }
            }
        }
    }
}

data class SurveyInfo(
    val type: SurveyType,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val required: Boolean,
    val completed: Boolean
)
