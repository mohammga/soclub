package com.example.soclub.screens.newActivity

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.soclub.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import java.util.Locale





@Composable
fun NewActivityScreen(navController: NavController, viewModel: NewActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item { ImageUploadSection(viewModel::onImageSelected) }

            item { TitleField(value = uiState.title, onNewValue = viewModel::onTitleChange) }

            item { DescriptionField(value = uiState.description, onNewValue = viewModel::onDescriptionChange) }

            item { CategoryField(value = uiState.category, onNewValue = viewModel::onCategoryChange) }

            item { LocationField(value = uiState.location, onNewValue = viewModel::onLocationChange) }

            item { AddressField(value = uiState.address, onNewValue = viewModel::onAddressChange) }

            item { PostalCodeField(value = uiState.postalCode, onNewValue = viewModel::onPostalCodeChange) }

            item { DateField(value = uiState.date, onNewValue = viewModel::onDateChange) }

            item { MaxParticipantsField(value = uiState.maxParticipants, onNewValue = viewModel::onMaxParticipantsChange) }

            item { AgeLimitField(value = uiState.ageLimit, onNewValue = viewModel::onAgeLimitChange) }

            item { Spacer(modifier = Modifier.height(5.dp)) }


            if (uiState.errorMessage != 0) {
                item {
                Text(
                    text = stringResource(id = uiState.errorMessage),
                    color = Color.Red
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

    // Status for den valgte kategorien
    var selectedText by remember { mutableStateOf(value) }

    // dette er Material 3 ExposedDropdownMenuBox
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }  // dropdown
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { onNewValue(it) },
            placeholder = { Text(stringResource(id = R.string.placeholder_category)) },
            modifier = Modifier
                .menuAnchor()  //
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,  // dette stoper bruken å endre påde
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        // Material 3 ExposedDropdownMenu
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category) },
                    onClick = {
                        // oppdaterer catagorien
                        selectedText = category
                        // sender katagori tilbake
                        onNewValue(category)
                        expanded = false
                    }
                )
            }
        }
    }
}



@Composable
fun LocationField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Sted") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun AddressField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Adresse") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun PostalCodeField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Postnummer") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun DateField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Dato") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
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
    val context = LocalContext.current
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

@Preview(showBackground = true)
@Composable
fun NewActivityScreenPreview() {
    NewActivityScreen(rememberNavController())
}
