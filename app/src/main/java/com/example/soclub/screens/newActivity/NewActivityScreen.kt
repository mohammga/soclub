package com.example.soclub.screens.newActivity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    var showAddressField by remember { mutableStateOf(false) }
    var showPostalCodeField by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item { ImageUploadSection(viewModel::onImageSelected) }

            item { TitleField(value = uiState.title, onNewValue = viewModel::onTitleChange, error = uiState.titleError) }

            item { DescriptionField(value = uiState.description, onNewValue = viewModel::onDescriptionChange, error = uiState.descriptionError) }

            item { CategoryField(value = uiState.category, onNewValue = viewModel::onCategoryChange, error = uiState.categoryError) }

            item {
                LocationField(
                    value = uiState.location,
                    onNewValue = { location ->
                        viewModel.onLocationChange(location)
                        showAddressField = location.isNotBlank()
                    },
                    suggestions = locationSuggestions,
                    onSuggestionClick = { suggestion ->
                        viewModel.onLocationChange(suggestion)
                        showAddressField = true
                    },
                    error = uiState.locationError
                )
            }

            if (showAddressField) {
                item {
                    AddressField(
                        value = uiState.address,
                        onNewValue = { address ->
                            viewModel.onAddressChange(address)
                            showPostalCodeField = address.isNotBlank()
                        },
                        suggestions = addressSuggestions,
                        onSuggestionClick = { suggestion ->
                            viewModel.onAddressSelected(suggestion)
                            showPostalCodeField = true
                        },
                        isEnabled = true,
                        error = uiState.addressError
                    )
                }
            }

            if (showPostalCodeField) {
                item {
                    PostalCodeField(
                        value = uiState.postalCode,
                        error = uiState.postalCodeError
                    )
                }
            }

            item { DateField(value = uiState.date?.toDate()?.time ?: 0L, onNewValue = viewModel::onDateChange, error = uiState.dateError) }

            item { StartTimeField(value = uiState.startTime, onNewValue = viewModel::onStartTimeChange, error = uiState.startTimeError) }

            item { MaxParticipantsField(value = uiState.maxParticipants, onNewValue = viewModel::onMaxParticipantsChange, error = uiState.maxParticipantsError) }

            item { AgeLimitField(value = uiState.ageLimit, onNewValue = viewModel::onAgeLimitChange, error = uiState.ageLimitError) }

            item { Spacer(modifier = Modifier.height(5.dp)) }

            item { PublishButton(navController, viewModel) }
        }
    }
}


@Composable
fun TitleField(value: String, onNewValue: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_title)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun DescriptionField(value: String, onNewValue: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_description)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        maxLines = 5,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryField(value: String, onNewValue: (String) -> Unit, error: String?) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Festivaler", "Klatring", "Mat", "Reise", "Trening")

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
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
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
    onSuggestionClick: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    var currentInput by remember { mutableStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = currentInput,
            onValueChange = {
                currentInput = it
                onNewValue(it)
                expanded = it.isNotEmpty() && suggestions.isNotEmpty()
            },
            placeholder = { Text("Sted") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        if (expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(suggestions) { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            currentInput = suggestion
                            onSuggestionClick(suggestion)
                            expanded = false
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
    isEnabled: Boolean,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    var currentInput by remember { mutableStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = currentInput,
            onValueChange = {
                if (isEnabled) {
                    currentInput = it
                    onNewValue(it)
                    expanded = it.isNotEmpty() && suggestions.isNotEmpty()
                }
            },
            placeholder = { Text("Adresse") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            enabled = isEnabled,
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        if (expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(suggestions) { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            currentInput = suggestion
                            onSuggestionClick(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PostalCodeField(value: String, error: String?) {
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
            readOnly = true,
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun DateField(value: Long, onNewValue: (Timestamp) -> Unit, error: String?) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() } // Sikrer at klikk på hele boksen åpner dialogen
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = if (value != 0L) {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(value))
                } else {
                    "Velg dato"
                },
                onValueChange = { /* Ikke i bruk, dialogen setter verdien direkte */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }, // Ekstra sikkerhet for å sikre klikkrespons
                isError = error != null,
                readOnly = true,
                supportingText = {
                    if (error != null) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() }) // Utvider klikkområdet
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun StartTimeField(value: String, onNewValue: (String) -> Unit, error: String?) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() }
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = value.ifEmpty { "Velg starttidspunkt" },
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                isError = error != null,
                readOnly = true,
                supportingText = {
                    if (error != null) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            // Spacer for å utvide klikkområdet
            Spacer(modifier = Modifier.matchParentSize().clickable { timePickerDialog.show() })
        }
    }
}

@Composable
fun MaxParticipantsField(value: String, onNewValue: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_max_participants)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun AgeLimitField(value: String, onNewValue: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_age_limit)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (selectedImageUri != null) {
                // Vise det valgte bildet
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = stringResource(id = R.string.selected_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Vise plassholderbildet
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = stringResource(id = R.string.change_ad_picture),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.change_ad_picture),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // Endre innholdet i raden basert på om et bilde er valgt eller ikke
        if (selectedImageUri != null) {
            // Vise alternativet for å fjerne bilde
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Ingen handling, bildesletting håndteres av knappen over */ },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.remove_image)), // Oppdatere teksten til "Fjern bilde"
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    ),
                    onClick = {
                        selectedImageUri = null // Fjern bildet
                    }
                )

                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.remove_image),
                    tint = Color.Gray
                )
            }
        } else {
            // Vise alternativet for å laste opp nytt bilde
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { galleryLauncher.launch("image/*") },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.upload_new_picture)),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    ),
                    onClick = { galleryLauncher.launch("image/*") },
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.upload_new_picture),
                    tint = Color.Gray
                )
            }
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
