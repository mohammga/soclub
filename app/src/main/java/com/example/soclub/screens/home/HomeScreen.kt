package com.example.soclub.screens.home

import androidx.compose.foundation.layout.*   // For layout-elementer som Column, Row, etc.
import androidx.compose.material3.*   // For Material Design-komponenter som Text, Tab, etc.
import androidx.compose.runtime.Composable   // For å bruke Composable-funksjoner
import androidx.compose.runtime.livedata.observeAsState   // For å observere LiveData i Jetpack Compose
import androidx.compose.ui.Modifier   // For modifikatorer som fillMaxSize, padding, etc.
import androidx.compose.ui.unit.dp   // For å spesifisere dimensjoner i dp
import androidx.hilt.navigation.compose.hiltViewModel   // For å bruke Hilt ViewModel i Jetpack Compose
import androidx.navigation.NavHostController   // For navigasjon mellom skjermer i Jetpack Compose
import com.example.soclub.models.Activity   // Importer Activity-modellen

@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    // Observer kategorier fra ViewModel
    val categories = viewModel.getCategories().observeAsState(initial = emptyList())
    // Observer aktiviteter for den valgte kategorien ("Festivaler" som et eksempel)
    val activities = viewModel.getActivities("Festivaler").observeAsState(initial = emptyList())

    // Viser kategorien og aktivitetene på skjermen
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Vis kategorien
        Text(
            text = if (categories.value.isNotEmpty()) categories.value[0] else "Ingen kategorier funnet",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sjekker om det er aktiviteter og viser dem
        if (activities.value.isNotEmpty()) {
            // Vis detaljer om den første aktiviteten i listen
            val activity = activities.value[0]   // Henter den første aktiviteten
            ActivityDetails(activity = activity)
        } else {
            Text(text = "Ingen aktiviteter funnet", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ActivityDetails(activity: Activity) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Tittel: ${activity.title}", style = MaterialTheme.typography.titleLarge)
        Text(text = "Beskrivelse: ${activity.description}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Sted: ${activity.location}", style = MaterialTheme.typography.bodySmall)
        Text(text = "Aldersgruppe: ${activity.ageGroup}", style = MaterialTheme.typography.bodySmall)
        Text(text = "Maks deltakere: ${activity.maxParticipants}", style = MaterialTheme.typography.bodySmall)
    }
}
