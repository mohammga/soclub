package com.example.soclub.screens.newActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.soclub.R
import com.example.soclub.screens.editActivity.shouldShowRequestPermissionRationale
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable for the screen to create a new activity.
 *
 * @param navController The NavController to manage navigation.
 * @param viewModel The ViewModel responsible for handling the logic for the new activity.
 */
@Composable
fun NewActivityScreen(
    navController: NavController,
    viewModel: NewActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val locationSuggestions by remember { derivedStateOf { uiState.locationSuggestions } }
    val addressSuggestions by remember { derivedStateOf { uiState.addressSuggestions } }
    val isPublishing by viewModel.isPublishing
    var locationConfirmed by remember { mutableStateOf(false) }

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
                    enabled = !isPublishing
                )
            }

            item {
                DescriptionField(
                    value = uiState.description,
                    onNewValue = viewModel::onDescriptionChange,
                    error = uiState.descriptionError,
                    enabled = !isPublishing
                )
            }

            item {
                CategoryField(
                    value = uiState.category,
                    onNewValue = viewModel::onCategoryChange,
                    error = uiState.categoryError,
                    enabled = !isPublishing
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
                    enabled = !isPublishing
                )
            }

            if (locationConfirmed) {
                item {
                    AddressField(
                        initialValue = uiState.address,
                        onNewValue = { address ->
                            viewModel.onAddressChange(address)
                        },
                        suggestions = addressSuggestions,
                        onSuggestionClick = { suggestion ->
                            viewModel.onAddressSelected(suggestion)
                        },
                        isEnabled = locationConfirmed,
                        error = uiState.addressError,
                        enabled = !isPublishing
                    )
                }
            }

            if (uiState.addressConfirmed) {
                item {
                    PostalCodeField(
                        value = uiState.postalCode,
                        error = uiState.postalCodeError,
                        enabled = !isPublishing
                    )
                }
            }

            item {
                DateField(
                    value = uiState.date?.toDate()?.time ?: 0L,
                    onNewValue = viewModel::onDateChange,
                    error = uiState.dateError,
                    enabled = !isPublishing
                )

            }

            item {
                StartTimeField(
                    value = uiState.startTime,
                    onNewValue = viewModel::onStartTimeChange,
                    error = uiState.startTimeError,
                    enabled = !isPublishing
                )
            }

            item {
                MaxParticipantsField(
                    value = uiState.maxParticipants,
                    onNewValue = viewModel::onMaxParticipantsChange,
                    error = uiState.maxParticipantsError,
                    enabled = !isPublishing
                )
            }

            item {
                AgeLimitField(
                    value = uiState.ageLimit,
                    onNewValue = viewModel::onAgeLimitChange,
                    error = uiState.ageLimitError,
                    enabled = !isPublishing
                )
            }

            item { Spacer(modifier = Modifier.height(5.dp)) }

            item { PublishButton(navController, viewModel, isPublishing) }
        }
    }
}

/**
 * Composable for entering the title of the activity.
 *
 * @param value The current value of the title input field.
 * @param onNewValue Lambda to handle updates to the title.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
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
        isError = error != null,
        enabled = enabled,
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
 * Composable for entering the description of the activity.
 *
 * @param value The current value of the description input field.
 * @param onNewValue Lambda to handle updates to the description.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
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
        isError = error != null,
        enabled = enabled,
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
 * Composable for selecting the category of the activity.
 *
 * @param value The current value of the selected category.
 * @param onNewValue Lambda to handle category selection.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Festivaler", "Klatring", "Mat", "Reise", "Trening")
    var selectedText by remember { mutableStateOf(value) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {
                selectedText = it
                onNewValue(it)
            },
            label = { Text(stringResource(id = R.string.category_label)) },
            placeholder = { Text(stringResource(id = R.string.placeholder_category)) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
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

/**
 * Composable for entering the location of the activity.
 *
 * @param initialValue The initial value of the location field.
 * @param onNewValue Lambda to handle updates to the location.
 * @param suggestions A list of suggested locations.
 * @param onSuggestionClick Lambda to handle the selection of a suggestion.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
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
            placeholder = { Text(stringResource(R.string.location_label)) },
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
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(stringResource(id = R.string.location_supporting_text))
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
 * Composable for entering the address of the activity.
 *
 * @param initialValue The initial value of the address field.
 * @param onNewValue Lambda to handle updates to the address.
 * @param suggestions A list of suggested addresses.
 * @param onSuggestionClick Lambda to handle the selection of a suggestion.
 * @param isEnabled Flag to indicate if the field should be editable.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
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
            placeholder = { Text(stringResource(id = R.string.address_label)) },
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
            enabled = isEnabled && enabled,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(stringResource(id = R.string.address_supporting_text))
                }
            }
        )
        if (suggestions.isNotEmpty() && textFieldValue.text.isNotBlank()) {
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
 * Composable for displaying the postal code of the activity.
 *
 * @param value The current value of the postal code field.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
 */
@Composable
fun PostalCodeField(value: String, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(stringResource(id = R.string.postal_code_label)) },
        placeholder = { Text(stringResource(id = R.string.postal_code_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        readOnly = true,
        isError = error != null,
        enabled = enabled,
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
 * Composable for selecting the date of the activity.
 *
 * @param value The current timestamp for the selected date.
 * @param onNewValue Lambda to handle updates to the date.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the date picker.
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
                .clickable { isDatePickerVisible.value = true }
        )

        if (isDatePickerVisible.value) {
            DatePickerDialog(
                onDismissRequest = { isDatePickerVisible.value = false },
                confirmButton = {
                    TextButton(onClick = {
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
                    }) {
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
 * Composable for selecting the start time of the activity.
 *
 * @param value The current value of the time field.
 * @param onNewValue Lambda to handle updates to the start time.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the time picker.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTimeField(
    value: String,
    onNewValue: (String) -> Unit,
    error: String?,
    enabled: Boolean
) {
    val isTimePickerVisible = remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(stringResource(id = R.string.start_time_label)) },
            placeholder = { Text(stringResource(id = R.string.choose_activity_starttime)) },
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
                    Text(stringResource(R.string.choose_activity_starttime))
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
                                val newStartTime = String.format("%02d:%02d", hour, minute)
                                onNewValue(newStartTime)
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
 * Composable for entering the maximum number of participants for the activity.
 *
 * @param value The current value of the max participants field.
 * @param onNewValue Lambda to handle updates to the number of participants.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
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
        isError = error != null,
        enabled = enabled,
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
 * Composable for entering the age limit for the activity.
 *
 * @param value The current value of the age limit field.
 * @param onNewValue Lambda to handle updates to the age limit.
 * @param error An optional error message for validation.
 * @param enabled Flag to enable or disable the input field.
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
 * Composable for uploading an image for the activity.
 *
 * @param selectedImageUri The URI of the currently selected image.
 * @param onImageSelected Lambda to handle image selection.
 * @param error An optional error message for validation.
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
                        color =  MaterialTheme.colorScheme.onSurfaceVariant,
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
 * Composable for the publish button.
 *
 * @param navController The NavController to manage navigation after publishing.
 * @param viewModel The ViewModel for managing publish logic.
 * @param isPublishing Flag indicating whether the publishing process is ongoing.
 */
@Composable
fun PublishButton(navController: NavController, viewModel: NewActivityViewModel, isPublishing: Boolean) {
    val buttonText = if (isPublishing) {
        stringResource(id = R.string.publishing_activity)
    } else {
        stringResource(id = R.string.publish_activity)
    }

    Button(
        onClick = { viewModel.onPublishClick(navController) },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isPublishing
    ) {
        Text(text = buttonText)
    }
}
