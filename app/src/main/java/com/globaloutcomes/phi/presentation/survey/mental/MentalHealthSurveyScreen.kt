package com.globaloutcomes.phi.presentation.survey.mental

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.domain.model.PhqSeverity
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard

/**
 * Mental Health Survey Screen (PHQ-9)
 * Patient Health Questionnaire for depression screening
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalHealthSurveyScreen(
    scanId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: MentalHealthSurveyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(scanId) {
        viewModel.initSurvey(scanId)
    }

    // Navigate to home after successful submission
    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mental Health Screening (PHQ-9)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth()
            )

            // Instructions
            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Patient Health Questionnaire (PHQ-9)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Over the last 2 weeks, how often have you been bothered by any of the following problems?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // PHQ-9 Questions
            PhqQuestion(
                number = 1,
                question = "Little interest or pleasure in doing things",
                selectedScore = state.littleInterest,
                onScoreSelected = viewModel::updateLittleInterest
            )

            PhqQuestion(
                number = 2,
                question = "Feeling down, depressed, or hopeless",
                selectedScore = state.feelingDown,
                onScoreSelected = viewModel::updateFeelingDown
            )

            PhqQuestion(
                number = 3,
                question = "Trouble falling or staying asleep, or sleeping too much",
                selectedScore = state.troubleSleeping,
                onScoreSelected = viewModel::updateTroubleSleeping
            )

            PhqQuestion(
                number = 4,
                question = "Feeling tired or having little energy",
                selectedScore = state.feelingTired,
                onScoreSelected = viewModel::updateFeelingTired
            )

            PhqQuestion(
                number = 5,
                question = "Poor appetite or overeating",
                selectedScore = state.poorAppetite,
                onScoreSelected = viewModel::updatePoorAppetite
            )

            PhqQuestion(
                number = 6,
                question = "Feeling bad about yourself — or that you are a failure or have let yourself or your family down",
                selectedScore = state.feelingBad,
                onScoreSelected = viewModel::updateFeelingBad
            )

            PhqQuestion(
                number = 7,
                question = "Trouble concentrating on things, such as reading or watching television",
                selectedScore = state.troubleConcentrating,
                onScoreSelected = viewModel::updateTroubleConcentrating
            )

            PhqQuestion(
                number = 8,
                question = "Moving or speaking so slowly that other people could have noticed. Or the opposite — being so fidgety or restless that you have been moving around a lot more than usual",
                selectedScore = state.movingSlow,
                onScoreSelected = viewModel::updateMovingSlow
            )

            PhqQuestion(
                number = 9,
                question = "Thoughts that you would be better off dead, or of hurting yourself in some way",
                selectedScore = state.thoughtsHurting,
                onScoreSelected = viewModel::updateThoughtsHurting,
                isWarning = true
            )

            // Score Summary
            if (state.totalScore != null && state.severityLevel != null) {
                ScoreSummary(
                    totalScore = state.totalScore!!,
                    severityLevel = state.severityLevel!!
                )
            }

            // Submit Button
            GoButton(
                text = "Complete Survey",
                onClick = viewModel::submitSurvey,
                enabled = state.canSubmit && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PhqQuestion(
    number: Int,
    question: String,
    selectedScore: Int?,
    onScoreSelected: (Int) -> Unit,
    isWarning: Boolean = false
) {
    GoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Question text
            Text(
                text = "$number. $question",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            // Score options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreOption(
                    score = 0,
                    label = "Not at all",
                    isSelected = selectedScore == 0,
                    onClick = { onScoreSelected(0) }
                )
                ScoreOption(
                    score = 1,
                    label = "Several days",
                    isSelected = selectedScore == 1,
                    onClick = { onScoreSelected(1) }
                )
                ScoreOption(
                    score = 2,
                    label = "More than half the days",
                    isSelected = selectedScore == 2,
                    onClick = { onScoreSelected(2) }
                )
                ScoreOption(
                    score = 3,
                    label = "Nearly every day",
                    isSelected = selectedScore == 3,
                    onClick = { onScoreSelected(3) }
                )
            }
        }
    }
}

@Composable
private fun ScoreOption(
    score: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label)
                Text("($score)", style = MaterialTheme.typography.bodySmall)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ScoreSummary(
    totalScore: Int,
    severityLevel: PhqSeverity
) {
    val (severityText, severityColor, recommendation) = when (severityLevel) {
        PhqSeverity.MINIMAL -> Triple(
            "Minimal depression",
            MaterialTheme.colorScheme.primary,
            "No treatment needed at this time. Continue routine monitoring."
        )
        PhqSeverity.MILD -> Triple(
            "Mild depression",
            MaterialTheme.colorScheme.tertiary,
            "Consider watchful waiting and follow-up in 2 weeks. Support and education may be helpful."
        )
        PhqSeverity.MODERATE -> Triple(
            "Moderate depression",
            MaterialTheme.colorScheme.secondary,
            "Consider counseling and/or medication. Referral to mental health services recommended."
        )
        PhqSeverity.MODERATELY_SEVERE -> Triple(
            "Moderately severe depression",
            MaterialTheme.colorScheme.error,
            "Immediate referral to mental health services. Counseling and medication strongly recommended."
        )
        PhqSeverity.SEVERE -> Triple(
            "Severe depression",
            MaterialTheme.colorScheme.error,
            "URGENT: Immediate referral to mental health specialist required. Active treatment and close monitoring essential."
        )
    }

    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PHQ-9 Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Score:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$totalScore / 27",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = severityText.uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = severityColor.copy(alpha = 0.2f),
                    labelColor = severityColor
                )
            )

            Text(
                text = "Recommendation:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (severityLevel == PhqSeverity.MODERATELY_SEVERE || severityLevel == PhqSeverity.SEVERE) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚠️ High-Risk Alert",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "A referral will be automatically created for mental health services.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
