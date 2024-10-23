package com.example.soclub.screens.newActivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.soclub.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.Timestamp
import java.util.Date


@Composable
fun NewActivityScreen(navController: NavController, viewModel: NewActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val locationSuggestions by remember { derivedStateOf { uiState.locationSuggestions } }
    val addressSuggestions by remember { derivedStateOf { uiState.addressSuggestions } }
    val errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item { TitleField(value = uiState.title, onNewValue = viewModel::onTitleChange) }

            item { DescriptionField(value = uiState.description, onNewValue = viewModel::onDescriptionChange) }

            item { ImageUploadSection(viewModel::onImageSelected) }

            item { CategoryField(value = uiState.category, onNewValue = viewModel::onCategoryChange) }

            item {
                LocationField(
                    value = uiState.location,
                    onNewValue = viewModel::onLocationChange,
                    suggestions = locationSuggestions,
                    onSuggestionClick = viewModel::onLocationChange
                )
            }

            item {
                AddressField(
                    value = uiState.address,
                    onNewValue = viewModel::onAddressChange,
                    suggestions = addressSuggestions,
                    onSuggestionClick = viewModel::onAddressSelected,
                    isEnabled = uiState.location.isNotBlank()
                )
            }

            item {
                PostalCodeField(
                    value = uiState.postalCode,


                )
            }

            item { DateField(value = uiState.date?.toDate()?.time ?: 0L, onNewValue = viewModel::onDateChange) }

            item { StartTimeField(value = uiState.startTime, onNewValue = viewModel::onStartTimeChange) }

            item { MaxParticipantsField(value = uiState.maxParticipants, onNewValue = viewModel::onMaxParticipantsChange) }

            item { AgeLimitField(value = uiState.ageLimit, onNewValue = viewModel::onAgeLimitChange) }

            item { Spacer(modifier = Modifier.height(5.dp)) }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = stringResource(id = uiState.errorMessage!!),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item { PublishButton(navController, viewModel) }
        }
    }
}

@Composable
fun TitleField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_title)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun DescriptionField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_description)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        maxLines = 5
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryField(value: String, onNewValue: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Festivaler", "Forslag", "Klatring", "Mat", "Reise", "Trening")

    var selectedText by remember { mutableStateOf(value) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { onNewValue(it) },
            placeholder = { Text(stringResource(id = R.string.placeholder_category)) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category) },
                    onClick = {
                        selectedText = category
                        onNewValue(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LocationField(
    value: String,
    onNewValue: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var currentInput by remember { mutableStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bruk TextField for å representere søkefunksjonaliteten
        TextField(
            value = currentInput,
            onValueChange = {
                currentInput = it
                onNewValue(it)
                expanded = it.isNotEmpty() && suggestions.isNotEmpty()  // Vis forslag bare når det er input og forslag
            },
            placeholder = { Text("Søk etter sted...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Vis forslag i en liste under TextField
        if (expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)  // Begrens høyden på forslagene for å unngå at de tar over hele skjermen
            ) {
                items(suggestions) { suggestion ->
                    // Hver linje i forslagene
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            currentInput = suggestion
                            onSuggestionClick(suggestion)
                            expanded = false  // Lukk menyen etter valg
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun AddressField(
    value: String,
    onNewValue: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    isEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var currentInput by remember { mutableStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bruk TextField for søkefeltet
        TextField(
            value = currentInput,
            onValueChange = {
                if (isEnabled) {
                    currentInput = it
                    onNewValue(it)
                    expanded = it.isNotEmpty() && suggestions.isNotEmpty()  // Vis forslag bare hvis det er input og forslag
                }
            },
            placeholder = { Text("Søk etter adresse...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            enabled = isEnabled
        )

        // Vis forslag i en liste under TextField
        if (expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)  // Begrens høyden på forslagene for å unngå at de tar for mye plass
            ) {
                items(suggestions) { suggestion ->
                    // Hver linje i forslagene
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            currentInput = suggestion
                            onSuggestionClick(suggestion)
                            expanded = false  // Lukk menyen etter valg
                        }
                    )
                }
            }
        }
    }
}




@Composable
fun PostalCodeField(value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text("Postnummer") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = false  // Deaktiverer inputfeltet, slik at brukeren ikke kan trykke eller endre det
        )
    }
}



@Composable
fun DateField(value: Long, onNewValue: (Timestamp) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance()
            newDate.set(year, month, dayOfMonth)
            val timestamp = newDate.timeInMillis
            onNewValue(Timestamp(Date(timestamp)))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { datePickerDialog.show() }
            .border(1.dp, MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val formattedDate = if (value != 0L) {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(value))
        } else {
            "Velg dato"
        }
        Text(text = formattedDate)
    }
}


@Composable
fun StartTimeField(value: String, onNewValue: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val newTime = String.format("%02d:%02d", hourOfDay, minute)
            onNewValue(newTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { timePickerDialog.show() }
            .border(1.dp, MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val formattedTime = if (value.isNotEmpty()) {
            value
        } else {
            "Velg starttidspunkt"
        }
        Text(text = formattedTime)
    }
}

@Composable
fun MaxParticipantsField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_max_participants)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun AgeLimitField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_age_limit)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun ImageUploadSection(onImageSelected: (String) -> Unit) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri.toString())
            selectedImageUri = uri
        }
    }

    Column {
        Text(text = stringResource(id = R.string.upload_image), style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.choose_image))
        }

        selectedImageUri?.let {
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = it,
                contentDescription = stringResource(id = R.string.selected_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun PublishButton(navController: NavController, viewModel: NewActivityViewModel) {
    Button(
        onClick = { viewModel.onPublishClick(navController) },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(text = stringResource(id = R.string.publish_activity))
    }
}