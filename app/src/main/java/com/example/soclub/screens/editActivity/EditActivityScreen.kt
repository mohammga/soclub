package com.example.soclub.screens.editActivity

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.soclub.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.core.content.ContextCompat

/**
 * Main screen for editing an activity.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param activityId The unique ID of the activity to be edited.
 * @param viewModel ViewModel containing the logic and state for the screen.
 * @param category The category of the activity being edited.
 */

@Composable
fun EditActivityScreen(
    navController: NavController,
    activityId: String,
    viewModel: EditActivityViewModel = hiltViewModel(),
    category: String
) {
    val uiState by viewModel.uiState
    val isSaving by viewModel.isSaving
    val isLoading by viewModel.isLoading
    LocalContext.current

    val locationSuggestions by remember { derivedStateOf { uiState.locationSuggestions } }
    val addressSuggestions by remember { derivedStateOf { uiState.addressSuggestions } }
    var locationConfirmed by remember { mutableStateOf(uiState.locationConfirmed) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadActivity(category, activityId)
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item {
                ImageUploadSection(
                    selectedImageUri = uiState.selectedImageUri,
                    onImageSelected = viewModel::onImageSelected,
                )
            }

            item {
                TitleField(
                    value = uiState.title,
                    onNewValue = viewModel::onTitleChange,
                    error = uiState.titleError,
                    enabled = !isSaving
                )
            }

            item {
                DescriptionField(
                    value = uiState.description,
                    onNewValue = viewModel::onDescriptionChange,
                    error = uiState.descriptionError,
                    enabled = !isSaving
                )
            }

            item {
                CategoryField(
                    value = uiState.category,
                    onNewValue = viewModel::onCategoryChange,
                    error = uiState.categoryError,
                    enabled = !isSaving
                )
            }

            item {
                LocationField(
                    initialValue = uiState.location,
                    onNewValue = { location ->
                        viewModel.onLocationChange(location)
                        locationConfirmed = false
                    },
                    suggestions = locationSuggestions,
                    onSuggestionClick = { suggestion ->
                        viewModel.onLocationSelected(suggestion)
                        locationConfirmed = true
                    },
                    error = uiState.locationError,
                    enabled = !isSaving
                )
            }

            if (uiState.locationConfirmed) {
                item {
                    AddressField(
                        initialValue = uiState.address,
                        onNewValue = { address -> viewModel.onAddressChange(address) },
                        suggestions = addressSuggestions,
                        onSuggestionClick = { suggestion -> viewModel.onAddressSelected(suggestion) },
                        isEnabled = uiState.locationConfirmed,
                        error = uiState.addressError,
                        enabled = !isSaving
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

            item {
                DateField(
                    value = uiState.date?.toDate()?.time ?: 0L,
                    onNewValue = viewModel::onDateChange,
                    error = uiState.dateError,
                    enabled = !isSaving
                )
            }

            item {
                StartTimeField(
                    value = uiState.startTime,
                    onNewValue = viewModel::onStartTimeChange,
                    error = uiState.startTimeError,
                    enabled = !isSaving
                )
            }

            item {
                MaxParticipantsField(
                    value = uiState.maxParticipants,
                    onNewValue = viewModel::onMaxParticipantsChange,
                    error = uiState.maxParticipantsError,
                    enabled = !isSaving
                )
            }

            item {
                AgeLimitField(
                    value = uiState.ageLimit,
                    onNewValue = viewModel::onAgeLimitChange,
                    error = uiState.ageLimitError,
                    enabled = !isSaving
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SaveChangesButton(
                    onClick = { viewModel.onSaveClick(navController, activityId, category) },
                    enabled = !isSaving,
                    isSaving = isSaving
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !isSaving
                ) {
                    Text(stringResource(R.string.delete))

                }
            }
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    viewModel.onDeleteClick(navController, category, activityId)
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
    }
}

/**
 * Dialog for confirming the deletion of an activity.
 *
 * @param onConfirm Callback invoked when the user confirms the deletion.
 * @param onDismiss Callback invoked when the user dismisses the dialog.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_deletion)) },
        text = { Text(stringResource(R.string.sure_you_want_to_delete)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Text field for entering the title of the activity.
 *
 * @param value The current value of the title.
 * @param onNewValue Callback invoked when the title value changes.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@Composable
fun TitleField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.title_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_title)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        enabled = enabled,
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

/**
 * Text field for entering the description of the activity.
 *
 * @param value The current value of the description.
 * @param onNewValue Callback invoked when the description value changes.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@Composable
fun DescriptionField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.description_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_description)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(150.dp),
        maxLines = 10,
        enabled = enabled,
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

/**
 * Dropdown field for selecting the category of the activity.
 *
 * @param value The current value of the category.
 * @param onNewValue Callback invoked when the category value changes.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Festivaler", "Klatring", "Mat", "Reise", "Trening")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(stringResource(id = R.string.category_label)) },
            placeholder = { Text(stringResource(id = R.string.placeholder_category)) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            isError = error != null,
            enabled = enabled,
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
                        onNewValue(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Dropdown field for selecting or entering the location of the activity.
 *
 * @param initialValue The initial value of the location.
 * @param onNewValue Callback invoked when the location value changes.
 * @param suggestions List of location suggestions displayed in the dropdown.
 * @param onSuggestionClick Callback invoked when a suggestion is selected.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationField(
    initialValue: String,
    onNewValue: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    error: String?,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = initialValue)) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onNewValue(newValue.text)
                expanded = newValue.text.isNotEmpty() && suggestions.isNotEmpty()
            },
            label = { Text(stringResource(id = R.string.location_label)) },
            placeholder = { Text(stringResource( R.string.location_label)) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            trailingIcon = {
                if (suggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            singleLine = true,
            isError = error != null,
            enabled = enabled,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.location_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            textFieldValue = TextFieldValue(
                                text = suggestion,
                                selection = TextRange(suggestion.length)
                            )
                            onSuggestionClick(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Dropdown field for selecting or entering the address of the activity.
 *
 * @param initialValue The initial value of the address.
 * @param onNewValue Callback invoked when the address value changes.
 * @param suggestions List of address suggestions displayed in the dropdown.
 * @param onSuggestionClick Callback invoked when a suggestion is selected.
 * @param isEnabled Flag indicating whether the field is visible.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressField(
    initialValue: String,
    onNewValue: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    isEnabled: Boolean,
    error: String?,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = initialValue)) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (isEnabled) {
                    textFieldValue = newValue
                    onNewValue(newValue.text)
                    expanded = newValue.text.isNotEmpty() && suggestions.isNotEmpty()
                }
            },
            label = { Text(stringResource(id = R.string.address_label)) },
            placeholder = { Text(stringResource(R.string.address_label)) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = enabled,
            trailingIcon = {
                if (suggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            singleLine = true,
            isError = error != null,
            supportingText = {
                if (error == null) {
                    Text(stringResource(id = R.string.address_supporting_text))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            textFieldValue = TextFieldValue(
                                text = suggestion,
                                selection = TextRange(suggestion.length)
                            )
                            onSuggestionClick(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Text field for displaying the postal code of the activity.
 *
 * @param value The current value of the postal code.
 * @param error Error message displayed if there is an error.
 */
@Composable
fun PostalCodeField(value: String, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(stringResource(id = R.string.postal_code_label)) },
        placeholder = { Text(stringResource(R.string.postal_code_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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

/**
 * Field for selecting the date of the activity using a date picker dialog.
 *
 * @param value The current date value in milliseconds.
 * @param onNewValue Callback invoked when a new date is selected.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    value: Long,
    onNewValue: (Timestamp) -> Unit,
    error: String?,
    enabled: Boolean
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (value != 0L) value else null
    )
    val isDatePickerVisible = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        OutlinedTextField(
            value = if (value != 0L) {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(value))
            } else "",
            onValueChange = {},
            label = { Text(stringResource(id = R.string.date_label)) },
            placeholder = { Text(stringResource(id = R.string.choose_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            isError = error != null,
            enabled = enabled,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(stringResource(R.string.choose_date))
                }
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(enabled = enabled) { isDatePickerVisible.value = true }
        )

        if (isDatePickerVisible.value) {
            DatePickerDialog(
                onDismissRequest = { isDatePickerVisible.value = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            val startOfTodayMillis = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            if (selectedMillis == null || selectedMillis < startOfTodayMillis) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.date_cannot_be_in_past),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val timestamp = Timestamp(Date(selectedMillis))
                                onNewValue(timestamp)
                                isDatePickerVisible.value = false
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isDatePickerVisible.value = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

/**
 * Field for selecting the start time of the activity using a time picker dialog.
 *
 * @param value The current value of the start time.
 * @param onNewValue Callback invoked when a new time is selected.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTimeField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    val isTimePickerVisible = remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(stringResource(R.string.start_time_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            isError = error != null,
            enabled = enabled,
            supportingText = {
                if (error == null) {
                    Text(stringResource(R.string.choose_activity_starttime))
                } else {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable { isTimePickerVisible.value = true }
        )

        if (isTimePickerVisible.value) {
            Dialog(onDismissRequest = { isTimePickerVisible.value = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp
                ) {
                    Column {
                        TimePicker(state = timePickerState)
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isTimePickerVisible.value = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                            TextButton(onClick = {
                                val hour = timePickerState.hour
                                val minute = timePickerState.minute
                                onNewValue(String.format("%02d:%02d", hour, minute))
                                isTimePickerVisible.value = false
                            }) {
                                Text(stringResource(R.string.ok))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Field for entering the maximum number of participants for the activity.
 *
 * @param value The current value of the maximum participants.
 * @param onNewValue Callback invoked when the value changes.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@Composable
fun MaxParticipantsField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.max_participants_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_max_participants)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        enabled = enabled,
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


/**
 * Field for entering the age limit for the activity.
 *
 * @param value The current value of the age limit.
 * @param onNewValue Callback invoked when the value changes.
 * @param error Error message displayed if there is an error.
 * @param enabled Flag indicating whether the field is editable.
 */
@Composable
fun AgeLimitField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.age_limit_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_age_limit)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                Text(stringResource(id = R.string.age_limit_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Section for uploading and managing an image for the activity.
 *
 * @param selectedImageUri The URI of the currently selected image.
 * @param onImageSelected Callback invoked when a new image is selected.
 * @param error Error message displayed if there is an error.
 */
@Composable
fun ImageUploadSection(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    error: String? = null,
) {
    val context = LocalContext.current

    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(
                context,
                R.string.galleriPermissionisrequired,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val handleImageClick = {
        when {
            ContextCompat.checkSelfPermission(context, galleryPermission) == PackageManager.PERMISSION_GRANTED -> {
                galleryLauncher.launch("image/*")
            }
            shouldShowRequestPermissionRationale(context, galleryPermission) -> {
                showPermissionDialog = true
            }
            else -> {
                permissionLauncher.launch(galleryPermission)
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.allow_premision_gallery)) },
            text = { Text(stringResource(R.string.this_app_need_gallery_premision)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(galleryPermission)
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { handleImageClick() }
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
            modifier = Modifier.clickable { handleImageClick() }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { onImageSelected(null) }
                )

                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.remove_image),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { handleImageClick() }
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.upload_new_picture),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { handleImageClick() }
                )
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * Determines whether the app should show a rationale for requesting a specific permission.
 *
 * @param context The current context.
 * @param permission The permission being requested.
 * @return `true` if a rationale should be shown, otherwise `false`.
 */

@SuppressLint("RestrictedApi")
fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    return if (context is ComponentActivity) {
        ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
    } else {
        false
    }
}

/**
 * Button for saving changes made to the activity.
 *
 * @param onClick Callback invoked when the button is clicked.
 * @param enabled Flag indicating whether the button is enabled.
 * @param isSaving Flag indicating whether the save operation is in progress.
 */
@Composable
fun SaveChangesButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isSaving: Boolean
) {
    val buttonText = if (isSaving) {
        stringResource(R.string.saving_changes_button)
    } else {
        stringResource(R.string.save_changes_button)
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled && !isSaving
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
        }
        Text(text = buttonText)
    }
}



