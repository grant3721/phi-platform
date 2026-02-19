package com.globaloutcomes.phi.presentation.patient.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.presentation.components.GoButton
import com.globaloutcomes.phi.presentation.components.GoCard
import com.globaloutcomes.phi.presentation.components.ButtonVariant
import java.text.SimpleDateFormat
import java.util.*

/**
 * Registration Screen - Complete patient registration with validation
 * First vertical slice: Form → Validation → Dedup → Save → Navigate to Scan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateToScan: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // Navigate to scan when registration complete
    LaunchedEffect(state.registrationComplete) {
        if (state.registrationComplete && state.registeredPatient != null) {
            onNavigateToScan(state.registeredPatient!!.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Patient") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner
            if (state.registrationError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.registrationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Duplicate patient alert
            if (state.duplicatePatient != null) {
                DuplicatePatientAlert(
                    patient = state.duplicatePatient!!,
                    onNavigateToPatient = { onNavigateBack() }
                )
            }

            // Contact Information Section
            SectionHeader("Contact Information")
            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.phoneNumber,
                            onValueChange = viewModel::onPhoneNumberChanged,
                            label = { Text("Phone Number *") },
                            placeholder = { Text("09171234567") },
                            isError = state.phoneError != null,
                            supportingText = state.phoneError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = viewModel::onCheckDuplicate,
                            enabled = !state.isCheckingDuplicate && state.phoneNumber.isNotBlank(),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            if (state.isCheckingDuplicate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Check")
                            }
                        }
                    }
                }
            }

            // Personal Information Section
            SectionHeader("Personal Information")
            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = viewModel::onFirstNameChanged,
                        label = { Text("First Name *") },
                        isError = state.firstNameError != null,
                        supportingText = state.firstNameError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = viewModel::onLastNameChanged,
                        label = { Text("Last Name *") },
                        isError = state.lastNameError != null,
                        supportingText = state.lastNameError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date of Birth Picker
                    DateOfBirthPicker(
                        selectedDate = state.dateOfBirth,
                        onDateSelected = viewModel::onDateOfBirthChanged,
                        error = state.dateOfBirthError
                    )

                    // Sex Selector
                    SexSelector(
                        selectedSex = state.sex,
                        onSexSelected = viewModel::onSexChanged
                    )
                }
            }

            // Location Section
            SectionHeader("Location")
            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarangayDropdown(
                        barangays = state.barangays,
                        selectedBarangay = state.selectedBarangay,
                        onBarangaySelected = viewModel::onBarangaySelected,
                        error = state.barangayError
                    )

                    if (state.selectedBarangay != null) {
                        OutlinedTextField(
                            value = state.selectedBarangay!!.municipality,
                            onValueChange = {},
                            label = { Text("Municipality") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Optional IDs Section
            SectionHeader("Health IDs (Optional)")
            GoCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.philHealthNumber ?: "",
                        onValueChange = viewModel::onPhilHealthNumberChanged,
                        label = { Text("PhilHealth Number") },
                        placeholder = { Text("01-234567890-1") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.philsysId ?: "",
                        onValueChange = viewModel::onPhilsysIdChanged,
                        label = { Text("PhilSys ID") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Pregnancy Section (Female only)
            if (state.sex == "F") {
                PregnancySection(
                    isPregnant = state.isPregnant,
                    gestationalAgeWeeks = state.gestationalAgeWeeks,
                    onPregnantChanged = viewModel::onPregnantChanged,
                    onGestationalAgeChanged = viewModel::onGestationalAgeChanged
                )
            }

            // Messenger Opt-in Section (for future Phase 13)
            MessengerOptInSection(
                optedIn = state.messengerOptIn,
                contactMethod = state.messengerContactMethod,
                contactValue = state.messengerContactValue,
                onOptInChanged = viewModel::onMessengerOptInChanged,
                onContactMethodChanged = viewModel::onMessengerContactMethodChanged,
                onContactValueChanged = viewModel::onMessengerContactValueChanged
            )

            // Register Button
            GoButton(
                text = if (state.isRegistering) "Registering..." else "Register & Start Scan",
                onClick = viewModel::onRegister,
                enabled = !state.isRegistering && state.duplicatePatient == null,
                isLoading = state.isRegistering,
                variant = ButtonVariant.PRIMARY,
                icon = Icons.Filled.Check,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
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
private fun DuplicatePatientAlert(
    patient: com.globaloutcomes.phi.domain.model.Patient,
    onNavigateToPatient: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Patient Already Registered",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Text(
                text = "${patient.fullName}\nRegistered: ${formatDate(patient.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            TextButton(onClick = onNavigateToPatient) {
                Text("View Patient Record")
            }
        }
    }
}

@Composable
private fun DateOfBirthPicker(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    error: String?
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    OutlinedTextField(
        value = selectedDate?.let { dateFormatter.format(Date(it)) } ?: "",
        onValueChange = {},
        label = { Text("Date of Birth *") },
        placeholder = { Text("Select date") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Select date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SexSelector(
    selectedSex: String,
    onSexSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sex *",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSex == "M",
                onClick = { onSexSelected("M") },
                label = { Text("Male") },
                leadingIcon = if (selectedSex == "M") {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            FilterChip(
                selected = selectedSex == "F",
                onClick = { onSexSelected("F") },
                label = { Text("Female") },
                leadingIcon = if (selectedSex == "F") {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarangayDropdown(
    barangays: List<com.globaloutcomes.phi.domain.model.Barangay>,
    selectedBarangay: com.globaloutcomes.phi.domain.model.Barangay?,
    onBarangaySelected: (com.globaloutcomes.phi.domain.model.Barangay) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredBarangays = remember(barangays, searchQuery) {
        if (searchQuery.isBlank()) {
            barangays
        } else {
            barangays.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.municipality.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedBarangay?.displayName ?: "",
            onValueChange = {},
            label = { Text("Barangay *") },
            placeholder = { Text("Select barangay") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            filteredBarangays.take(50).forEach { barangay ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(barangay.name)
                            Text(
                                text = barangay.municipality,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onBarangaySelected(barangay)
                        expanded = false
                        searchQuery = ""
                    }
                )
            }
        }
    }
}

@Composable
private fun PregnancySection(
    isPregnant: Boolean,
    gestationalAgeWeeks: Int?,
    onPregnantChanged: (Boolean) -> Unit,
    onGestationalAgeChanged: (Int?) -> Unit
) {
    SectionHeader("Maternal Health")
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isPregnant,
                    onCheckedChange = onPregnantChanged
                )
                Text("Currently Pregnant")
            }

            if (isPregnant) {
                OutlinedTextField(
                    value = gestationalAgeWeeks?.toString() ?: "",
                    onValueChange = { value ->
                        onGestationalAgeChanged(value.toIntOrNull())
                    },
                    label = { Text("Gestational Age (weeks)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MessengerOptInSection(
    optedIn: Boolean,
    contactMethod: String?,
    contactValue: String?,
    onOptInChanged: (Boolean) -> Unit,
    onContactMethodChanged: (String?) -> Unit,
    onContactValueChanged: (String?) -> Unit
) {
    SectionHeader("Messenger Notifications (Optional)")
    GoCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = optedIn,
                    onCheckedChange = onOptInChanged
                )
                Column {
                    Text("Send results via Messenger")
                    Text(
                        text = "Receive scan results and health reminders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (optedIn) {
                Text(
                    text = "Feature coming soon in Phase 13",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
