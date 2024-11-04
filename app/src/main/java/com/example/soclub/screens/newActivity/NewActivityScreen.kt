package com.example.soclub.screens.newActivity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item {
                ImageUploadSection(
                    selectedImageUri = uiState.selectedImageUri,
                    onImageSelected = viewModel::onImageSelected
                )
            }

            item { TitleField(value = uiState.title, onNewValue = viewModel::onTitleChange, error = uiState.titleError) }

            item { DescriptionField(value = uiState.description, onNewValue = viewModel::onDescriptionChange, error = uiState.descriptionError) }

            item { CategoryField(value = uiState.category, onNewValue = viewModel::onCategoryChange, error = uiState.categoryError) }

            item {
                LocationField(
                    value = uiState.location,
                    onNewValue = { location ->
                        viewModel.onLocationChange(location)
                        viewModel.uiState.value = uiState.copy(locationConfirmed = false) // Reset confirmation
                    },
                    suggestions = locationSuggestions,
                    onSuggestionClick = { suggestion ->
                        viewModel.onLocationSelected(suggestion)
                        viewModel.uiState.value = uiState.copy(locationConfirmed = true) // Confirm selection
                    },
                    error = uiState.locationError
                )
            }

            if (uiState.locationConfirmed) {
                item {
                    AddressField(
                        value = uiState.address,
                        onNewValue = { address ->
                            viewModel.onAddressChange(address)
                            viewModel.uiState.value = uiState.copy(addressConfirmed = false) // Reset confirmation
                        },
                        suggestions = addressSuggestions,
                        onSuggestionClick = { suggestion ->
                            viewModel.onAddressSelected(suggestion)
                            viewModel.uiState.value = uiState.copy(addressConfirmed = true) // Confirm selection
                        },
                        isEnabled = uiState.locationConfirmed,
                        error = uiState.addressError
                    )
                }
            }

            if (uiState.addressConfirmed) {
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
        label = { Text(stringResource(id = R.string.title_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_title)) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error == null) {
                Text(stringResource(id = R.string.title_supporting_text))
            } else {
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
        label = { Text(stringResource(id = R.string.description_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_description)) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        maxLines = 5,
        isError = error != null,
        supportingText = {
            if (error == null) {
                Text(stringResource(id = R.string.description_supporting_text))
            } else {
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
            label = { Text(stringResource(id = R.string.category_label)) },
            placeholder = { Text(stringResource(id = R.string.placeholder_category)) },
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(vertical = 8.dp),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            isError = error != null,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.category_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
fun LocationField(value: String, onNewValue: (String) -> Unit, suggestions: List<String>, onSuggestionClick: (String) -> Unit, error: String?) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onNewValue(it)
                expanded = it.isNotEmpty() && suggestions.isNotEmpty()
            },
            label = { Text(stringResource(id = R.string.location_label)) },
            placeholder = { Text("Sted") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            isError = error != null,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.location_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        if (expanded) {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                items(suggestions) { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
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
fun AddressField(value: String, onNewValue: (String) -> Unit, suggestions: List<String>, onSuggestionClick: (String) -> Unit, isEnabled: Boolean, error: String?) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (isEnabled) {
                    onNewValue(it)
                    expanded = it.isNotEmpty() && suggestions.isNotEmpty()
                }
            },
            label = { Text(stringResource(id = R.string.address_label)) },
            placeholder = { Text("Adresse") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            enabled = isEnabled,
            isError = error != null,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.address_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        if (expanded) {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                items(suggestions) { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
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
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(stringResource(id = R.string.postal_code_label)) },
        placeholder = { Text("Postnummer") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        readOnly = true,
        isError = error != null,
        supportingText = {
            if (error == null) {
                Text(stringResource(id = R.string.postal_code_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun DateField(value: Long, onNewValue: (Timestamp) -> Unit, error: String?) {
    val context = LocalContext.current
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                onNewValue(Timestamp(Date(newDate.timeInMillis)))
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }

    // Display the formatted date or a default placeholder
    val dateText = if (value != 0L) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(value))
    } else {
        "Velg dato"
    }

    Box {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            label = { Text(stringResource(id = R.string.date_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            isError = error != null,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.date_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        // Invisible box overlay to handle clicks
        Box(
            modifier = Modifier
                .matchParentSize()  // Matches size of OutlinedTextField
                .alpha(0f)           // Makes the box invisible
                .clickable { datePickerDialog.show() }  // Opens DatePickerDialog on click
        )
    }
}



@Composable
fun StartTimeField(value: String, onNewValue: (String) -> Unit, error: String?) {
    val context = LocalContext.current
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onNewValue(String.format("%02d:%02d", hour, minute))
            },
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            true
        )
    }

    // State to handle focus
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box {
        OutlinedTextField(
            value = if (value.isNotEmpty()) value else "Velg tidspunkt",
            onValueChange = {},
            label = { Text(stringResource(id = R.string.start_time_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(focusRequester), // Set focus requester
            readOnly = true,
            isError = error != null,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.start_time_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        // Invisible overlay box to intercept clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable {
                    // Clear focus to prevent keyboard from appearing
                    focusManager.clearFocus()
                    // Show the TimePickerDialog
                    timePickerDialog.show()
                }
        )
    }
}


@Composable
fun MaxParticipantsField(value: String, onNewValue: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.max_participants_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_max_participants)) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error == null) {
                Text(stringResource(id = R.string.max_participants_supporting_text))
            } else {
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
        label = { Text(stringResource(id = R.string.age_limit_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_age_limit)) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = error != null,
        supportingText = {
            if (error == null) {
                Text(stringResource(id = R.string.age_limit_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun ImageUploadSection(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    error: String? = null
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { galleryLauncher.launch("image/*") }
                .padding(vertical = 8.dp)
        ) {
            if (selectedImageUri != null) {
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.change_ad_picture),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
        )

        if (selectedImageUri != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.remove_image),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { onImageSelected(null) }
                )

                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.remove_image),
                    tint = Color.Gray,
                    modifier = Modifier.clickable { onImageSelected(null) }
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.upload_new_picture),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.upload_new_picture),
                    tint = Color.Gray,
                    modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                )
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
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
