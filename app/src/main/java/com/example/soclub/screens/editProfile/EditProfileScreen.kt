package com.example.soclub.screens.editProfile

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.*
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.soclub.R
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(navController: NavController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ImageUploadSection(
                    viewModel::onImageSelected,
                    imageUrl = uiState.imageUrl
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    label = stringResource(id = R.string.profile_name_label),
                    value = uiState.firstname,
                    onValueChange = { viewModel.onNameChange(it) }
                )

                ProfileTextField(
                    label = stringResource(id = R.string.profile_lastname_label),
                    value = uiState.lastname,
                    onValueChange = { viewModel.onLastnameChange(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.errorMessage != 0) {
                    Text(
                        text = stringResource(id = uiState.errorMessage),
                        color = Color.Red
                    )
                }

                SaveButton(
                    onClick = {
                        viewModel.onSaveProfileClick(navController)
                        // Vis snackbar bare hvis det ikke er noen feilmelding
                        if (uiState.errorMessage == 0) {
                            coroutineScope.launch {
                                // Henter strengen fra strings.xml og viser snackbar
                                snackbarHostState.showSnackbar(
                                    message = "Personlig info er endret"
                                )
                            }
                        }
                    },
                    enabled = uiState.isDirty
                )

            }
        }
    )
}


@Composable
fun ImageUploadSection(
    onImageSelected: (String) -> Unit,
    imageUrl: String? // Add the user's current profile image URL
) {
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
            when {
                selectedImageUri != null -> {
                    // Show the selected image from the gallery
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = stringResource(id = R.string.selected_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                !imageUrl.isNullOrEmpty() -> {
                    // Show the current profile image if available
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = stringResource(id = R.string.profile_picture_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // Show placeholder image if no image is selected or no profile image exists
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.change_profile_picture),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // Display either the option to remove or upload a new image based on the selection
        if (selectedImageUri != null) {
            // Option to remove the selected image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedImageUri = null },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.remove_image)),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    ),
                    onClick = { selectedImageUri = null }
                )

                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.remove_image),
                    tint = Color.Gray
                )
            }
        } else {
            // Option to upload a new image
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
                    onClick = { galleryLauncher.launch("image/*") }
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
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun SaveButton(onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        enabled = enabled
    ) {
        Text(text = stringResource(id = R.string.save_changes_button), color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen(rememberNavController())
}
