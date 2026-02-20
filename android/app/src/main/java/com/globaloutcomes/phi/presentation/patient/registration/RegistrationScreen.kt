package com.globaloutcomes.phi.presentation.patient.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.globaloutcomes.phi.domain.model.Barangay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: (patientId: String) -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // TODO: Load barangays from repository via ViewModel
    val barangays = remember {
        listOf(
            Barangay(
                id = "brgy-001",
                name = "Barangay 1",
                municipality = "Manila",
                province = "Metro Manila",
                region = "NCR",
                islandGroup = "Luzon"
            ),
            Barangay(
                id = "brgy-002",
                name = "Barangay 2",
                municipality = "Quezon City",
                province = "Metro Manila",
                region = "NCR",
                islandGroup = "Luzon"
            )
        )
    }

    // Handle success navigation
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.registeredPatientId != null) {
            onRegistrationSuccess(state.registeredPatientId!!)
        }
    }

    // Date picker state
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Barangay dropdown state
    var barangayExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Patient") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Contact Information
                Text(
                    "Contact Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Phone Number
                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { viewModel.onEvent(RegistrationEvent.PhoneNumberChanged(it)) },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("09171234567") },
                    supportingText = {
                        state.validationErrors["phoneNumber"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    isError = state.validationErrors.containsKey("phoneNumber"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (state.phoneNumber.length == 11) {
                            IconButton(onClick = { viewModel.onEvent(RegistrationEvent.CheckDuplicate) }) {
                                if (state.isDuplicateCheck) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = "Check duplicate")
                                }
                            }
                        }
                    }
                )

                // Duplicate warning
                if (state.duplicatePatient != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Patient Already Registered",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Name: ${state.duplicatePatient!!.fullName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Age: ${state.duplicatePatient!!.age}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.onEvent(RegistrationEvent.ProceedWithDuplicate) }
                            ) {
                                Text("Proceed Anyway")
                            }
                        }
                    }
                }

                Divider()

                // Section: Personal Information
                Text(
                    "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // First Name
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onEvent(RegistrationEvent.FirstNameChanged(it)) },
                    label = { Text("First Name *") },
                    supportingText = {
                        state.validationErrors["firstName"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    isError = state.validationErrors.containsKey("firstName"),
                    modifier = Modifier.fillMaxWidth()
                )

                // Last Name
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onEvent(RegistrationEvent.LastNameChanged(it)) },
                    label = { Text("Last Name *") },
                    supportingText = {
                        state.validationErrors["lastName"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    isError = state.validationErrors.containsKey("lastName"),
                    modifier = Modifier.fillMaxWidth()
                )

                // Birthdate
                OutlinedTextField(
                    value = state.birthdate?.let {
                        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Birthdate *") },
                    readOnly = true,
                    supportingText = {
                        state.validationErrors["birthdate"]?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    isError = state.validationErrors.containsKey("birthdate"),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    }
                )

                // Sex Selection
                Column {
                    Text(
                        "Sex *",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.sex == "male",
                            onClick = { viewModel.onEvent(RegistrationEvent.SexSelected("male")) },
                            label = { Text("Male") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = state.sex == "female",
                            onClick = { viewModel.onEvent(RegistrationEvent.SexSelected("female")) },
                            label = { Text("Female") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Barangay Dropdown
                ExposedDropdownMenuBox(
                    expanded = barangayExpanded,
                    onExpandedChange = { barangayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = barangays.find { it.id == state.selectedBarangayId }?.fullLocation ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Barangay *") },
                        supportingText = {
                            state.validationErrors["barangay"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = state.validationErrors.containsKey("barangay"),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = barangayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = barangayExpanded,
                        onDismissRequest = { barangayExpanded = false }
                    ) {
                        barangays.forEach { barangay ->
                            DropdownMenuItem(
                                text = { Text(barangay.fullLocation) },
                                onClick = {
                                    viewModel.onEvent(RegistrationEvent.BarangaySelected(barangay.id))
                                    barangayExpanded = false
                                }
                            )
                        }
                    }
                }

                Divider()

                // Section: Optional Information
                Text(
                    "Optional Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // PhilHealth Number
                OutlinedTextField(
                    value = state.philHealthNumber,
                    onValueChange = { viewModel.onEvent(RegistrationEvent.PhilHealthChanged(it)) },
                    label = { Text("PhilHealth Number") },
                    placeholder = { Text("12-345678901-2") },
                    modifier = Modifier.fillMaxWidth()
                )

                // PhilSys Number
                OutlinedTextField(
                    value = state.philSysNumber,
                    onValueChange = { viewModel.onEvent(RegistrationEvent.PhilSysChanged(it)) },
                    label = { Text("PhilSys Number") },
                    placeholder = { Text("1234-5678-9012-3456") },
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()

                // Section: Maternal Health
                if (state.sex == "female") {
                    Text(
                        "Maternal Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Currently Pregnant", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.isPregnant,
                            onCheckedChange = {
                                viewModel.onEvent(RegistrationEvent.PregnancyToggled(it))
                            }
                        )
                    }

                    if (state.isPregnant) {
                        OutlinedTextField(
                            value = state.gestationalAgeWeeks?.toString() ?: "",
                            onValueChange = {
                                it.toIntOrNull()?.let { weeks ->
                                    viewModel.onEvent(RegistrationEvent.GestationalAgeChanged(weeks))
                                }
                            },
                            label = { Text("Gestational Age (weeks) *") },
                            supportingText = {
                                state.validationErrors["gestationalAge"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            isError = state.validationErrors.containsKey("gestationalAge"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider()
                }

                // Section: Communication Preferences
                Text(
                    "Communication Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Facebook Messenger",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Receive scan results via Messenger",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = state.messengerOptIn,
                            onCheckedChange = {
                                viewModel.onEvent(RegistrationEvent.MessengerOptInToggled(it))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { viewModel.onEvent(RegistrationEvent.SubmitRegistration) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Register Patient", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Error message
                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    viewModel.onEvent(RegistrationEvent.BirthdateSelected(millis))
                                }
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

            // Loading Overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
