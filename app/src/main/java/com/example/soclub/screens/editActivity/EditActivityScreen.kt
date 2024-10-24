package com.example.soclub.screens.editActivity

import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.*

@Composable
fun EditActivityScreen(
    navController: NavController,
    viewModel: EditActivityViewModel = hiltViewModel(),
    category: String,
    activityId: String // Sørg for at activityId er en parameter her
) {
    val uiState by viewModel.uiState

    // Kall til ViewModel for å laste aktiviteten basert på category og activityId
    LaunchedEffect(Unit) {
        viewModel.loadActivity(category, activityId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item { TitleField(value = uiState.title, onNewValue = viewModel::onTitleChange) }
            item { DescriptionField(value = uiState.description, onNewValue = viewModel::onDescriptionChange) }
            item { StartTimeField(value = uiState.startTime, onNewValue = viewModel::onStartTimeChange) }
            item { MaxParticipantsField(value = uiState.maxParticipants, onNewValue = viewModel::onMaxParticipantsChange) }
            item { SaveChangesButton(navController, viewModel, category, activityId) }
        }
    }
}


@Composable
fun TitleField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Tittel") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun DescriptionField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Beskrivelse") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        maxLines = 5
    )
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
        val formattedTime = if (value.isNotEmpty()) value else "Velg starttidspunkt"
        Text(text = formattedTime)
    }
}



@Composable
fun MaxParticipantsField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text("Maks antall deltakere") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun SaveChangesButton(navController: NavController, viewModel: EditActivityViewModel, category: String, activityId: String) {
    Button(
        onClick = { viewModel.onSaveClick(navController, category, activityId) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Lagre endringer")
    }
}
