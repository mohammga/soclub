package com.example.soclub.screens.editProfile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.soclub.R

/**
 * Composable function to display the Edit Profile screen.
 *
 * @param navController The [NavController] to manage navigation.
 * @param viewModel The [EditProfileViewModel] for handling profile logic.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(navController: NavController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val context = LocalContext.current
    val isSaving by viewModel.isSaving

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Scaffold(
        content = {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = errorMessage ?: stringResource(id = R.string.unknown_error_occurred),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ImageUploadSection(
                                imageUri = uiState.imageUri,
                                onImageSelected = viewModel::onImageSelected,
                                enabled = !isSaving
                            )

                        }

                        item {
                            ProfileTextField(
                                label = stringResource(id = R.string.profile_firstname_label),
                                value = uiState.firstname,
                                onValueChange = { viewModel.onNameChange(it) },
                                error = uiState.firstnameError?.let { stringResource(id = it) },
                                supportingText = stringResource(id = R.string.profile_firstname_supporting_text),
                                enabled = !isSaving
                            )
                        }

                        item {
                            ProfileTextField(
                                label = stringResource(id = R.string.profile_lastname_label),
                                value = uiState.lastname,
                                onValueChange = { viewModel.onLastnameChange(it) },
                                error = uiState.lastnameError?.let { stringResource(id = it) },
                                supportingText = stringResource(id = R.string.profile_lastname_supporting_text),
                                enabled = !isSaving
                            )
                        }

                        item {
                            SaveButton(
                                onClick = {
                                    viewModel.onSaveProfileClick(navController, context)
                                },
                                enabled = uiState.isDirty && !isSaving,
                                isSaving = isSaving
                            )
                        }
                    }
                }
            }
        }
    )
}

/**
 * Composable function to display a text field for editing profile information.
 *
 * @param label The label for the text field.
 * @param value The current value of the text field.
 * @param onValueChange Lambda to handle text changes.
 * @param error The error message to display, if any.
 * @param supportingText Supporting text to assist the user.
 * @param enabled Whether the text field is enabled or not.
 */
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                supportingText?.let { Text(text = it) }
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Composable function to display the image upload section.
 *
 * @param imageUri The URI of the currently selected image.
 * @param onImageSelected Lambda to handle image selection.
 * @param error The error message to display, if any.
 * @param enabled Whether the upload section is enabled or not.
 */
@Composable
fun ImageUploadSection(
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    error: String? = null,
    enabled: Boolean = true
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
            ContextCompat.checkSelfPermission(
                context,
                galleryPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
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
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
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
                .let {
                    if (enabled) it.clickable { handleImageClick() } else it
                }
                .padding(vertical = 8.dp)
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(id = R.string.selected_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = stringResource(id = R.string.profile_picture_description),
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
            text = stringResource(id = R.string.change_profile_picture),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = if (enabled) Modifier.clickable { handleImageClick() } else Modifier
        )

        if (imageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (enabled) it.clickable { onImageSelected(null) } else it
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.remove_image),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    ),
                    modifier = if (enabled) Modifier.clickable { onImageSelected(null) } else Modifier
                )

                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.remove_image),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = if (enabled) Modifier.clickable { onImageSelected(null) } else Modifier
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
                    modifier = if (enabled) Modifier.clickable { handleImageClick() } else Modifier
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.upload_new_picture),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = if (enabled) Modifier.clickable { handleImageClick() } else Modifier
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
 * Checks whether the app should show a rationale for requesting a permission.
 *
 * @param context The [Context] of the application.
 * @param permission The permission to check.
 * @return `true` if a rationale should be shown, otherwise `false`.
 */
fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    return if (context is ActivityResultRegistryOwner) {
        ActivityCompat.shouldShowRequestPermissionRationale(context as ComponentActivity, permission)
    } else {
        false
    }
}

/**
 * Composable function to display a save button.
 *
 * @param onClick Lambda to handle save button clicks.
 * @param enabled Whether the button is enabled or not.
 * @param isSaving Whether the save operation is currently in progress.
 */
@Composable
fun SaveButton(onClick: () -> Unit, enabled: Boolean, isSaving: Boolean) {
    val buttonText = if (isSaving) {
        stringResource(id = R.string.saving_changes_button)
    } else {
        stringResource(id = R.string.save_changes_button)
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = enabled
    ) {
        Text(text = buttonText)
    }
}