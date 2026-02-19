package com.globaloutcomes.phi.presentation.survey.maternal

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
import com.globaloutcomes.phi.domain.model.MaternalComplication
import com.globaloutcomes.phi.domain.model.MaternalSymptom
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.GoSegmentedButton
import com.globaloutcomes.phi.presentation.components.ButtonVariant
import java.text.SimpleDateFormat
import java.util.*

/**
 * Maternal Survey Screen - Pregnancy Health Assessment
 *
 * Structured survey for pregnant patients:
 * - Pregnancy history (LMP, gestational age, gravidity, parity)
 * - Previous complications
 * - Current symptoms
 * - Prenatal care tracking
 * - Supplementation status
 * - Immunization (tetanus toxoid)
 * - Risk factor assessment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaternalSurveyScreen(
    scanId: String,
    onSurveyComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MaternalSurveyViewModel = hiltViewModel()
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
                        Text("Maternal Health Survey")
                        Text(
                            text = "Pregnancy Assessment",
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
                // Pregnancy Information
                SectionHeader("Pregnancy Information")
                PregnancyInfoSection(
                    state = state,
                    onLmpDateSelected = viewModel::onLmpDateSelected,
                    onGestationalAgeChanged = viewModel::onGestationalAgeChanged,
                    onGravidityChanged = viewModel::onGravidityChanged,
                    onParityChanged = viewModel::onParityChanged,
                    onAbortionsChanged = viewModel::onAbortionsChanged
                )

                // Previous Complications
                SectionHeader("Previous Pregnancy Complications")
                PreviousComplicationsSection(
                    selectedComplications = state.previousComplications,
                    onToggleComplication = viewModel::onToggleComplication
                )

                // Current Symptoms
                SectionHeader("Current Symptoms (if any)")
                CurrentSymptomsSection(
                    selectedSymptoms = state.currentSymptoms,
                    onToggleSymptom = viewModel::onToggleSymptom
                )

                // Prenatal Care
                SectionHeader("Prenatal Care")
                PrenatalCareSection(
                    state = state,
                    onPrenatalVisitsChanged = viewModel::onPrenatalVisitsChanged,
                    onLastVisitDateSelected = viewModel::onLastVisitDateSelected
                )

                // Supplementation
                SectionHeader("Supplementation")
                SupplementationSection(
                    state = state,
                    onTakingIronChanged = viewModel::onTakingIronChanged,
                    onTakingFolicAcidChanged = viewModel::onTakingFolicAcidChanged,
                    onTakingCalciumChanged = viewModel::onTakingCalciumChanged
                )

                // Immunization
                SectionHeader("Immunization")
                ImmunizationSection(
                    tetanusDoses = state.tetanusToxoidDoses,
                    onTetanusDosesChanged = viewModel::onTetanusDosesChanged
                )

                // Risk Factors
                SectionHeader("Risk Factors")
                RiskFactorsSection(
                    state = state,
                    onMultiplePregnancyChanged = viewModel::onMultiplePregnancyChanged,
                    onGestationalDiabetesChanged = viewModel::onGestationalDiabetesChanged,
                    onPreeclampsiaChanged = viewModel::onPreeclampsiaChanged
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PregnancyInfoSection(
    state: MaternalSurveyState,
    onLmpDateSelected: (Long) -> Unit,
    onGestationalAgeChanged: (Int) -> Unit,
    onGravidityChanged: (Int) -> Unit,
    onParityChanged: (Int) -> Unit,
    onAbortionsChanged: (Int) -> Unit
) {
    var showLmpPicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LMP Date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Last Menstrual Period (LMP)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                OutlinedButton(
                    onClick = { showLmpPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.lmpDate?.let { dateFormatter.format(Date(it)) } ?: "Select date"
                    )
                }
            }

            // Gestational Age (auto-calculated or manual)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gestational Age: ${state.gestationalAgeWeeks} weeks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (state.lmpDate != null) "Auto-calculated from LMP" else "Manual entry",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.gestationalAgeWeeks.toFloat(),
                    onValueChange = { onGestationalAgeChanged(it.toInt()) },
                    valueRange = 4f..42f,
                    steps = 37
                )
            }

            Divider()

            // Gravidity, Parity, Abortions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberStepper(
                    label = "Gravidity",
                    subtitle = "Total pregnancies",
                    value = state.gravidity,
                    onValueChange = onGravidityChanged,
                    modifier = Modifier.weight(1f)
                )

                NumberStepper(
                    label = "Parity",
                    subtitle = "Live births",
                    value = state.parity,
                    onValueChange = onParityChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            NumberStepper(
                label = "Abortions / Miscarriages",
                subtitle = "Previous losses",
                value = state.abortions,
                onValueChange = onAbortionsChanged,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // LMP Date Picker Dialog
    if (showLmpPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.lmpDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showLmpPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onLmpDateSelected)
                        showLmpPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLmpPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun PreviousComplicationsSection(
    selectedComplications: List<MaternalComplication>,
    onToggleComplication: (MaternalComplication) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Select any complications from previous pregnancies",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MaternalComplication.entries.forEach { complication ->
                FilterChip(
                    selected = selectedComplications.contains(complication),
                    onClick = { onToggleComplication(complication) },
                    label = {
                        Text(
                            when (complication) {
                                MaternalComplication.PREECLAMPSIA -> "Preeclampsia"
                                MaternalComplication.GESTATIONAL_DIABETES -> "Gestational Diabetes"
                                MaternalComplication.PRETERM_LABOR -> "Preterm Labor"
                                MaternalComplication.POSTPARTUM_HEMORRHAGE -> "Postpartum Hemorrhage"
                                MaternalComplication.CESAREAN_SECTION -> "Cesarean Section"
                                MaternalComplication.STILLBIRTH -> "Stillbirth"
                                MaternalComplication.MISCARRIAGE -> "Miscarriage"
                            }
                        )
                    },
                    leadingIcon = if (selectedComplications.contains(complication)) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            if (selectedComplications.isEmpty()) {
                Text(
                    text = "No complications selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CurrentSymptomsSection(
    selectedSymptoms: List<MaternalSymptom>,
    onToggleSymptom: (MaternalSymptom) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Select any symptoms you are currently experiencing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MaternalSymptom.entries.forEach { symptom ->
                FilterChip(
                    selected = selectedSymptoms.contains(symptom),
                    onClick = { onToggleSymptom(symptom) },
                    label = {
                        Text(
                            when (symptom) {
                                MaternalSymptom.SEVERE_HEADACHE -> "Severe Headache"
                                MaternalSymptom.BLURRED_VISION -> "Blurred Vision"
                                MaternalSymptom.ABDOMINAL_PAIN -> "Abdominal Pain"
                                MaternalSymptom.VAGINAL_BLEEDING -> "Vaginal Bleeding"
                                MaternalSymptom.DECREASED_FETAL_MOVEMENT -> "Decreased Fetal Movement"
                                MaternalSymptom.SEVERE_SWELLING -> "Severe Swelling"
                                MaternalSymptom.PERSISTENT_VOMITING -> "Persistent Vomiting"
                                MaternalSymptom.FEVER -> "Fever"
                                MaternalSymptom.DIFFICULTY_BREATHING -> "Difficulty Breathing"
                            }
                        )
                    },
                    leadingIcon = if (selectedSymptoms.contains(symptom)) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = if (isHighRiskSymptom(symptom)) {
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    } else {
                        FilterChipDefaults.filterChipColors()
                    }
                )
            }

            if (selectedSymptoms.isEmpty()) {
                Text(
                    text = "No symptoms (feeling well)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrenatalCareSection(
    state: MaternalSurveyState,
    onPrenatalVisitsChanged: (Int) -> Unit,
    onLastVisitDateSelected: (Long) -> Unit
) {
    var showLastVisitPicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NumberStepper(
                label = "Prenatal Visits",
                subtitle = "Total checkups so far",
                value = state.prenatalVisits,
                onValueChange = onPrenatalVisitsChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Last Prenatal Visit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                OutlinedButton(
                    onClick = { showLastVisitPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.lastPrenatalVisit?.let { dateFormatter.format(Date(it)) } ?: "Select date"
                    )
                }
            }
        }
    }

    // Last Visit Date Picker Dialog
    if (showLastVisitPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.lastPrenatalVisit ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showLastVisitPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onLastVisitDateSelected)
                        showLastVisitPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLastVisitPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SupplementationSection(
    state: MaternalSurveyState,
    onTakingIronChanged: (Boolean?) -> Unit,
    onTakingFolicAcidChanged: (Boolean?) -> Unit,
    onTakingCalciumChanged: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Are you currently taking the following supplements?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SupplementQuestion(
                supplement = "Iron Supplements",
                value = state.takingIronSupplements,
                onValueChange = onTakingIronChanged
            )

            SupplementQuestion(
                supplement = "Folic Acid",
                value = state.takingFolicAcid,
                onValueChange = onTakingFolicAcidChanged
            )

            SupplementQuestion(
                supplement = "Calcium",
                value = state.takingCalcium,
                onValueChange = onTakingCalciumChanged
            )
        }
    }
}

@Composable
private fun ImmunizationSection(
    tetanusDoses: Int,
    onTetanusDosesChanged: (Int) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberStepper(
                label = "Tetanus Toxoid Doses",
                subtitle = "Recommended: 2 doses during pregnancy",
                value = tetanusDoses,
                onValueChange = onTetanusDosesChanged,
                maxValue = 5,
                modifier = Modifier.fillMaxWidth()
            )

            if (tetanusDoses < 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Please ensure you receive ${2 - tetanusDoses} more dose(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskFactorsSection(
    state: MaternalSurveyState,
    onMultiplePregnancyChanged: (Boolean?) -> Unit,
    onGestationalDiabetesChanged: (Boolean?) -> Unit,
    onPreeclampsiaChanged: (Boolean?) -> Unit
) {
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RiskFactorQuestion(
                question = "Multiple pregnancy (twins, triplets)?",
                value = state.multiplePregnancy,
                onValueChange = onMultiplePregnancyChanged
            )

            RiskFactorQuestion(
                question = "Diagnosed with gestational diabetes?",
                value = state.gestationalDiabetes,
                onValueChange = onGestationalDiabetesChanged
            )

            RiskFactorQuestion(
                question = "Diagnosed with preeclampsia?",
                value = state.preeclampsia,
                onValueChange = onPreeclampsiaChanged
            )
        }
    }
}

@Composable
private fun NumberStepper(
    label: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    maxValue: Int = 10,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                enabled = value > 0
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease")
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = { if (value < maxValue) onValueChange(value + 1) },
                enabled = value < maxValue
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase")
            }
        }
    }
}

@Composable
private fun SupplementQuestion(
    supplement: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = supplement,
            style = MaterialTheme.typography.bodyMedium
        )
        GoSegmentedButton(
            selectedValue = value,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun RiskFactorQuestion(
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

private fun isHighRiskSymptom(symptom: MaternalSymptom): Boolean {
    return symptom in listOf(
        MaternalSymptom.VAGINAL_BLEEDING,
        MaternalSymptom.SEVERE_HEADACHE,
        MaternalSymptom.BLURRED_VISION,
        MaternalSymptom.ABDOMINAL_PAIN,
        MaternalSymptom.DIFFICULTY_BREATHING
    )
}
