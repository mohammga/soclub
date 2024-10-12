package com.example.soclub.screens.newActivity

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import java.text.SimpleDateFormat
import java.util.*




@Composable
fun NewActivityScreen(navController: NavController, viewModel: NewActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
    ) {

        TitleField(value = uiState.title, onNewValue = viewModel::onTitleChange)

        DescriptionField(value = uiState.description, onNewValue = viewModel::onDescriptionChange)

        ImageUploadSection(viewModel::onImageSelected)

        CategoryField(value = uiState.category, onNewValue = viewModel::onCategoryChange)

        LocationField(value = uiState.location, onNewValue = viewModel::onLocationChange)

        AddressField(value = uiState.address, onNewValue = viewModel::onAddressChange)

        PostalCodeField(value = uiState.postalCode, onNewValue = viewModel::onPostalCodeChange)

        DateField(value = uiState.date, onNewValue = viewModel::onDateChange)  // Bruker DatePickerDialog for å velge dato

        MaxParticipantsField(value = uiState.maxParticipants, onNewValue = viewModel::onMaxParticipantsChange)

        AgeLimitField(value = uiState.ageLimit, onNewValue = viewModel::onAgeLimitChange)

        Spacer(modifier = Modifier.height(5.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
            )
        }

        PublishButton(navController, viewModel)
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
            .padding(vertical = 3.dp),
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
            .padding(vertical = 2.dp),
        maxLines = 5
    )
}

@Composable
fun CategoryField(value: String, onNewValue: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Festivaler", "Forslag", "Klatring", "Mat", "Reise", "Trening")

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = { onNewValue(it) },
            placeholder = { Text(stringResource(id = R.string.placeholder_category)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        //dropdown
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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

@Composable
fun LocationField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("sted") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
            .padding(vertical = 2.dp),
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
            .padding(vertical = 2.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}
@Composable
fun DateField(value: String, onNewValue: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Formatter som inkluderer ukedag og dato
    val dateFormat = SimpleDateFormat("EEEE.dd.MM.yyyy", Locale.getDefault())
    val formattedDate = remember { mutableStateOf(value.ifBlank { dateFormat.format(calendar.time) }) }

    // DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Oppdater datoen når brukeren har valgt
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            // Bruker format som inkluderer ukedag
            formattedDate.value = dateFormat.format(selectedDate.time)
            onNewValue(formattedDate.value)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // Bruk pointerInput for å håndtere klikk
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable {
                println("Opening DatePickerDialog")  // Feilsøking: sjekk om klikket registreres
                datePickerDialog.show()
            } // Håndter klikk med clickable
            .border(1.dp, MaterialTheme.colorScheme.primary)  // Visuell grense for å indikere klikkbarhet
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = formattedDate.value)
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
            .padding(vertical = 2.dp),
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
            .padding(vertical = 2.dp),
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
            onImageSelected(uri.toString())  // Send URI-en til funksjonen
            selectedImageUri = uri           // Lagre valgt URI lokalt
        }
    }

    Column {
        Text(text = stringResource(id = R.string.upload_image), style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = { galleryLauncher.launch("image/*") },  // Start galleri
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.choose_image))
        }

        selectedImageUri?.let {
            Text(text = it.toString()) // Viser valgt bilde-URI hvis den er valgt
        }
    }
}

@Composable
fun PublishButton(navController: NavController, viewModel: NewActivityViewModel) {
    Button(
        onClick = { viewModel.onPublishClick(navController) },
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
    ) {
        Text(text = stringResource(id = R.string.publish_activity))
    }
}

@Preview(showBackground = true)
@Composable
fun NewActivityScreenPreview() {
    NewActivityScreen(rememberNavController())
}
