package com.globaloutcomes.phi.presentation.survey.infectious

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
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.GoSegmentedButton

/**
 * Infectious Disease Survey Screen
 * Screens for TB, Dengue, COVID-19, and travel history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfectiousSurveyScreen(
    scanId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: InfectiousSurveyViewModel = hiltViewModel()
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
                title = { Text("Infectious Disease Screening") },
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

            Text(
                text = "Screen for common infectious diseases",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // TB Screening Section
            TbScreeningSection(
                persistentCough = state.persistentCough,
                coughDuration = state.coughDuration,
                bloodInSputum = state.bloodInSputum,
                nightSweats = state.nightSweats,
                unexplainedWeightLoss = state.unexplainedWeightLoss,
                fever = state.fever,
                tbContact = state.tbContact,
                onPersistentCoughChange = viewModel::updatePersistentCough,
                onCoughDurationChange = viewModel::updateCoughDuration,
                onBloodInSputumChange = viewModel::updateBloodInSputum,
                onNightSweatsChange = viewModel::updateNightSweats,
                onUnexplainedWeightLossChange = viewModel::updateUnexplainedWeightLoss,
                onFeverChange = viewModel::updateFever,
                onTbContactChange = viewModel::updateTbContact
            )

            // Dengue Screening Section
            DengueScreeningSection(
                suddenFever = state.suddenFever,
                severeHeadache = state.severeHeadache,
                painBehindEyes = state.painBehindEyes,
                jointPain = state.jointPain,
                rash = state.rash,
                bleeding = state.bleeding,
                onSuddenFeverChange = viewModel::updateSuddenFever,
                onSevereHeadacheChange = viewModel::updateSevereHeadache,
                onPainBehindEyesChange = viewModel::updatePainBehindEyes,
                onJointPainChange = viewModel::updateJointPain,
                onRashChange = viewModel::updateRash,
                onBleedingChange = viewModel::updateBleeding
            )

            // COVID-19 Screening Section
            CovidScreeningSection(
                covidSymptoms = state.covidSymptoms,
                covidContact = state.covidContact,
                covidVaccinated = state.covidVaccinated,
                covidVaccineDoses = state.covidVaccineDoses,
                onCovidSymptomsChange = viewModel::updateCovidSymptoms,
                onCovidContactChange = viewModel::updateCovidContact,
                onCovidVaccinatedChange = viewModel::updateCovidVaccinated,
                onCovidVaccineDosesChange = viewModel::updateCovidVaccineDoses
            )

            // Travel History Section
            TravelHistorySection(
                recentTravel = state.recentTravel,
                travelDestinations = state.travelDestinations,
                onRecentTravelChange = viewModel::updateRecentTravel,
                onAddDestination = viewModel::addTravelDestination,
                onRemoveDestination = viewModel::removeTravelDestination
            )

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
private fun TbScreeningSection(
    persistentCough: Boolean?,
    coughDuration: Int?,
    bloodInSputum: Boolean?,
    nightSweats: Boolean?,
    unexplainedWeightLoss: Boolean?,
    fever: Boolean?,
    tbContact: Boolean?,
    onPersistentCoughChange: (Boolean?) -> Unit,
    onCoughDurationChange: (Int?) -> Unit,
    onBloodInSputumChange: (Boolean?) -> Unit,
    onNightSweatsChange: (Boolean?) -> Unit,
    onUnexplainedWeightLossChange: (Boolean?) -> Unit,
    onFeverChange: (Boolean?) -> Unit,
    onTbContactChange: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tuberculosis (TB) Screening",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Persistent cough
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Persistent cough for 2+ weeks?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (persistentCough) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onPersistentCoughChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Cough duration (if yes)
            if (persistentCough == true) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "How many weeks?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = coughDuration?.toString() ?: "",
                        onValueChange = { value ->
                            onCoughDurationChange(value.toIntOrNull())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter weeks") },
                        singleLine = true
                    )
                }
            }

            // Blood in sputum
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Blood in sputum/cough?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (bloodInSputum) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onBloodInSputumChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Night sweats
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Night sweats?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (nightSweats) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onNightSweatsChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Unexplained weight loss
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Unexplained weight loss?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (unexplainedWeightLoss) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onUnexplainedWeightLossChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Fever
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Fever or chills?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (fever) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onFeverChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // TB contact
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Recent contact with TB patient?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (tbContact) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onTbContactChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DengueScreeningSection(
    suddenFever: Boolean?,
    severeHeadache: Boolean?,
    painBehindEyes: Boolean?,
    jointPain: Boolean?,
    rash: Boolean?,
    bleeding: Boolean?,
    onSuddenFeverChange: (Boolean?) -> Unit,
    onSevereHeadacheChange: (Boolean?) -> Unit,
    onPainBehindEyesChange: (Boolean?) -> Unit,
    onJointPainChange: (Boolean?) -> Unit,
    onRashChange: (Boolean?) -> Unit,
    onBleedingChange: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dengue Screening",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Sudden high fever
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Sudden high fever?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (suddenFever) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onSuddenFeverChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Severe headache
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Severe headache?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (severeHeadache) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onSevereHeadacheChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Pain behind eyes
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pain behind eyes?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (painBehindEyes) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onPainBehindEyesChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Joint/muscle pain
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Severe joint or muscle pain?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (jointPain) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onJointPainChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Rash
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Skin rash?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (rash) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onRashChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Bleeding
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Easy bruising or bleeding?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (bleeding) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onBleedingChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CovidScreeningSection(
    covidSymptoms: Boolean?,
    covidContact: Boolean?,
    covidVaccinated: Boolean?,
    covidVaccineDoses: Int?,
    onCovidSymptomsChange: (Boolean?) -> Unit,
    onCovidContactChange: (Boolean?) -> Unit,
    onCovidVaccinatedChange: (Boolean?) -> Unit,
    onCovidVaccineDosesChange: (Int?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "COVID-19 Screening",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // COVID symptoms
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Recent COVID-19 symptoms? (fever, cough, loss of taste/smell)",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (covidSymptoms) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onCovidSymptomsChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // COVID contact
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Recent contact with COVID-19 positive person?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (covidContact) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onCovidContactChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // COVID vaccination
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Vaccinated against COVID-19?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (covidVaccinated) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onCovidVaccinatedChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Vaccine doses (if vaccinated)
            if (covidVaccinated == true) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Number of vaccine doses",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = covidVaccineDoses?.toString() ?: "",
                        onValueChange = { value ->
                            onCovidVaccineDosesChange(value.toIntOrNull())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter number of doses") },
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelHistorySection(
    recentTravel: Boolean?,
    travelDestinations: List<String>,
    onRecentTravelChange: (Boolean?) -> Unit,
    onAddDestination: (String) -> Unit,
    onRemoveDestination: (String) -> Unit
) {
    var newDestination by remember { mutableStateOf("") }

    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Travel History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Recent travel
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Traveled outside barangay in past 14 days?",
                    style = MaterialTheme.typography.bodyMedium
                )
                GoSegmentedButton(
                    options = listOf("Yes", "No", "Unknown"),
                    selectedOption = when (recentTravel) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    },
                    onOptionSelected = { option ->
                        onRecentTravelChange(
                            when (option) {
                                "Yes" -> true
                                "No" -> false
                                else -> null
                            }
                        )
                    }
                )
            }

            // Travel destinations (if yes)
            if (recentTravel == true) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Destinations visited",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newDestination,
                            onValueChange = { newDestination = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Enter location") },
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newDestination.isNotBlank()) {
                                    onAddDestination(newDestination)
                                    newDestination = ""
                                }
                            },
                            enabled = newDestination.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }

                    // List of destinations
                    travelDestinations.forEach { destination ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = destination,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onRemoveDestination(destination) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
