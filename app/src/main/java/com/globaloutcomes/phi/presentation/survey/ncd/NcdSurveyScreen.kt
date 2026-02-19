package com.globaloutcomes.phi.presentation.survey.ncd

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
import com.globaloutcomes.phi.domain.model.AlcoholConsumption
import com.globaloutcomes.phi.domain.model.SmokingStatus
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.GoSegmentedButton
import com.globaloutcomes.phi.presentation.components.ButtonVariant

/**
 * NCD Survey Screen - Non-Communicable Disease Risk Assessment
 *
 * Structured survey with:
 * - Diabetes screening questions
 * - Hypertension screening
 * - Cardiovascular risk factors
 * - Lifestyle assessment (smoking, alcohol, activity, diet)
 * - Current medications
 * - Other conditions
 *
 * All inputs are structured (no free text except medications)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcdSurveyScreen(
    scanId: String,
    onSurveyComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NcdSurveyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Navigate when survey complete
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onSurveyComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Health Survey")
                        Text(
                            text = "Non-Communicable Diseases",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Diabetes Screening
                SectionHeader("Diabetes Screening")
                DiabetesSection(
                    state = state,
                    onHasDiabetesChanged = viewModel::onHasDiabetesChanged,
                    onDiabetesFamilyHistoryChanged = viewModel::onDiabetesFamilyHistoryChanged,
                    onFrequentUrinationChanged = viewModel::onFrequentUrinationChanged,
                    onExcessiveThirstChanged = viewModel::onExcessiveThirstChanged,
                    onUnexplainedWeightLossChanged = viewModel::onUnexplainedWeightLossChanged
                )

                // Hypertension Screening
                SectionHeader("Hypertension Screening")
                HypertensionSection(
                    state = state,
                    onHasHypertensionChanged = viewModel::onHasHypertensionChanged,
                    onHypertensionFamilyHistoryChanged = viewModel::onHypertensionFamilyHistoryChanged,
                    onFrequentHeadachesChanged = viewModel::onFrequentHeadachesChanged,
                    onDizzinessChanged = viewModel::onDizzinessChanged
                )

                // Cardiovascular Risk
                SectionHeader("Cardiovascular Risk")
                CardiovascularSection(
                    state = state,
                    onHeartDiseaseFamilyHistoryChanged = viewModel::onHeartDiseaseFamilyHistoryChanged,
                    onChestPainChanged = viewModel::onChestPainChanged,
                    onShortnessOfBreathChanged = viewModel::onShortnessOfBreathChanged
                )

                // Lifestyle Factors
                SectionHeader("Lifestyle Assessment")
                LifestyleSection(
                    state = state,
                    onSmokingStatusChanged = viewModel::onSmokingStatusChanged,
                    onCigarettesPerDayChanged = viewModel::onCigarettesPerDayChanged,
                    onAlcoholConsumptionChanged = viewModel::onAlcoholConsumptionChanged,
                    onPhysicalActivityChanged = viewModel::onPhysicalActivityChanged,
                    onDietQualityChanged = viewModel::onDietQualityChanged
                )

                // Current Medications
                SectionHeader("Current Medications")
                MedicationsSection(
                    medications = state.currentMedications,
                    onAddMedication = viewModel::onAddMedication,
                    onRemoveMedication = viewModel::onRemoveMedication
                )

                // Submit Button
                GoButton(
                    text = if (state.isSaving) "Saving..." else "Complete Survey",
                    onClick = viewModel::submitSurvey,
                    enabled = state.canSubmit && !state.isSaving,
                    isLoading = state.isSaving,
                    variant = ButtonVariant.PRIMARY,
                    icon = Icons.Filled.Check,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DiabetesSection(
    state: NcdSurveyState,
    onHasDiabetesChanged: (Boolean?) -> Unit,
    onDiabetesFamilyHistoryChanged: (Boolean?) -> Unit,
    onFrequentUrinationChanged: (Boolean?) -> Unit,
    onExcessiveThirstChanged: (Boolean?) -> Unit,
    onUnexplainedWeightLossChanged: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyQuestion(
                question = "Have you been diagnosed with diabetes?",
                value = state.hasDiabetes,
                onValueChange = onHasDiabetesChanged
            )

            SurveyQuestion(
                question = "Does anyone in your family have diabetes?",
                value = state.diabetesFamilyHistory,
                onValueChange = onDiabetesFamilyHistoryChanged
            )

            SurveyQuestion(
                question = "Do you urinate frequently?",
                value = state.frequentUrination,
                onValueChange = onFrequentUrinationChanged
            )

            SurveyQuestion(
                question = "Do you experience excessive thirst?",
                value = state.excessiveThirst,
                onValueChange = onExcessiveThirstChanged
            )

            SurveyQuestion(
                question = "Have you had unexplained weight loss?",
                value = state.unexplainedWeightLoss,
                onValueChange = onUnexplainedWeightLossChanged
            )
        }
    }
}

@Composable
private fun HypertensionSection(
    state: NcdSurveyState,
    onHasHypertensionChanged: (Boolean?) -> Unit,
    onHypertensionFamilyHistoryChanged: (Boolean?) -> Unit,
    onFrequentHeadachesChanged: (Boolean?) -> Unit,
    onDizzinessChanged: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyQuestion(
                question = "Have you been diagnosed with high blood pressure?",
                value = state.hasHypertension,
                onValueChange = onHasHypertensionChanged
            )

            SurveyQuestion(
                question = "Does anyone in your family have high blood pressure?",
                value = state.hypertensionFamilyHistory,
                onValueChange = onHypertensionFamilyHistoryChanged
            )

            SurveyQuestion(
                question = "Do you have frequent headaches?",
                value = state.frequentHeadaches,
                onValueChange = onFrequentHeadachesChanged
            )

            SurveyQuestion(
                question = "Do you experience dizziness?",
                value = state.dizziness,
                onValueChange = onDizzinessChanged
            )
        }
    }
}

@Composable
private fun CardiovascularSection(
    state: NcdSurveyState,
    onHeartDiseaseFamilyHistoryChanged: (Boolean?) -> Unit,
    onChestPainChanged: (Boolean?) -> Unit,
    onShortnessOfBreathChanged: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyQuestion(
                question = "Does anyone in your family have heart disease?",
                value = state.heartDiseaseFamilyHistory,
                onValueChange = onHeartDiseaseFamilyHistoryChanged
            )

            SurveyQuestion(
                question = "Do you experience chest pain?",
                value = state.chestPain,
                onValueChange = onChestPainChanged
            )

            SurveyQuestion(
                question = "Do you have shortness of breath?",
                value = state.shortnessOfBreath,
                onValueChange = onShortnessOfBreathChanged
            )
        }
    }
}

@Composable
private fun LifestyleSection(
    state: NcdSurveyState,
    onSmokingStatusChanged: (SmokingStatus) -> Unit,
    onCigarettesPerDayChanged: (Int) -> Unit,
    onAlcoholConsumptionChanged: (AlcoholConsumption) -> Unit,
    onPhysicalActivityChanged: (Int) -> Unit,
    onDietQualityChanged: (Int) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smoking Status
            Text(
                text = "Smoking Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SmokingStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.smokingStatus == status,
                        onClick = { onSmokingStatusChanged(status) },
                        label = {
                            Text(
                                when (status) {
                                    SmokingStatus.NEVER -> "Never"
                                    SmokingStatus.FORMER -> "Former"
                                    SmokingStatus.CURRENT -> "Current"
                                }
                            )
                        }
                    )
                }
            }

            // Cigarettes per day (if current smoker)
            if (state.smokingStatus == SmokingStatus.CURRENT) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cigarettes per day: ${state.cigarettesPerDay}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.cigarettesPerDay.toFloat(),
                        onValueChange = { onCigarettesPerDayChanged(it.toInt()) },
                        valueRange = 0f..40f,
                        steps = 39
                    )
                }
            }

            Divider()

            // Alcohol Consumption
            Text(
                text = "Alcohol Consumption",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlcoholConsumption.entries.forEach { consumption ->
                    FilterChip(
                        selected = state.alcoholConsumption == consumption,
                        onClick = { onAlcoholConsumptionChanged(consumption) },
                        label = {
                            Text(
                                when (consumption) {
                                    AlcoholConsumption.NONE -> "None"
                                    AlcoholConsumption.OCCASIONAL -> "Occasional"
                                    AlcoholConsumption.MODERATE -> "Moderate"
                                    AlcoholConsumption.HEAVY -> "Heavy"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Divider()

            // Physical Activity Level
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Physical Activity Level: ${state.physicalActivityLevel}/5",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "1 = Sedentary, 5 = Very Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.physicalActivityLevel.toFloat(),
                    onValueChange = { onPhysicalActivityChanged(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }

            Divider()

            // Diet Quality
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Diet Quality: ${state.dietQuality}/5",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "1 = Poor, 5 = Excellent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.dietQuality.toFloat(),
                    onValueChange = { onDietQualityChanged(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        }
    }
}

@Composable
private fun MedicationsSection(
    medications: List<String>,
    onAddMedication: (String) -> Unit,
    onRemoveMedication: (String) -> Unit
) {
    var newMedication by remember { mutableStateOf("") }

    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "List any medications you are currently taking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Add medication input
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMedication,
                    onValueChange = { newMedication = it },
                    label = { Text("Medication name") },
                    placeholder = { Text("e.g., Metformin") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newMedication.isNotBlank()) {
                            onAddMedication(newMedication.trim())
                            newMedication = ""
                        }
                    },
                    enabled = newMedication.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add medication")
                }
            }

            // Medication list
            if (medications.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    medications.forEach { medication ->
                        AssistChip(
                            onClick = { onRemoveMedication(medication) },
                            label = { Text(medication) },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = "No medications added",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SurveyQuestion(
    question: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium
        )
        GoSegmentedButton(
            selectedValue = value,
            onValueChange = onValueChange
        )
    }
}
