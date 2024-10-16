import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.soclub.models.createActivity
import com.example.soclub.screens.editActivity.EditActivityViewModel

@Composable
fun EditActivityScreen(
    navController: NavController,
    viewModel: EditActivityViewModel = hiltViewModel(),
    category: String,
    activityId: String
) {
    // Legg til tilstand for å vise dialogen
    var showDialog by remember { mutableStateOf(false) }

    // Variabler for de nåværende verdiene
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var ageGroup by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Variabler for de opprinnelige verdiene fra aktiviteten
    var originalTitle by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }
    var originalMaxParticipants by remember { mutableStateOf("") }
    var originalAgeGroup by remember { mutableStateOf("") }
    var originalLocation by remember { mutableStateOf("") }
    var originalDate by remember { mutableStateOf("") }
    var originalTime by remember { mutableStateOf("") }
    var originalImageUrl by remember { mutableStateOf("") }

    // Bildeseleksjon
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageUrl = it.toString()
        }
    }

    // Hent aktivitet fra ViewModel
    val activity = viewModel.getActivity(category, activityId).observeAsState()

    // Når aktiviteten hentes, sett både de opprinnelige og gjeldende verdiene
    LaunchedEffect(activity.value) {
        activity.value?.let {
            originalTitle = it.title ?: ""
            originalDescription = it.description ?: ""
            originalMaxParticipants = it.maxParticipants.toString()
            originalAgeGroup = it.ageGroup.toString()
            originalLocation = it.location + " " + it.restOfAddress
            originalDate = it.date ?: ""
            originalTime = it.time ?: ""
            originalImageUrl = it.imageUrl

            // Sett de gjeldende verdiene lik de opprinnelige
            title = originalTitle
            description = originalDescription
            maxParticipants = originalMaxParticipants
            ageGroup = originalAgeGroup
            location = originalLocation
            date = originalDate
            time = originalTime
            imageUrl = originalImageUrl
        }
    }

    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    // Funksjon for å sjekke om det er noen endringer
    val hasChanges = title != originalTitle || description != originalDescription ||
            maxParticipants != originalMaxParticipants || ageGroup != originalAgeGroup ||
            location != originalLocation || date != originalDate || time != originalTime ||
            imageUrl != originalImageUrl

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Tittel") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Beskrivelse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                maxLines = 5
            )

            selectedImageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Valgt bilde",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            } ?: run {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Opprinnelig bilde",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            }

            // Knapp for å velge et nytt bilde
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center // Sentrerer knappen horisontalt
            ) {
                Button(
                    onClick = { launcher.launch("image/*") }
                ) {
                    Text(text = "Velg bilde")
                }
            }

            TextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                placeholder = { Text("Maks antall deltakere") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = ageGroup,
                onValueChange = { ageGroup = it },
                placeholder = { Text("Aldersgruppe") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Adresse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = date,
                onValueChange = { date = it },
                placeholder = { Text("Dato") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = time,
                onValueChange = { time = it },
                placeholder = { Text("Tidspunkt") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Knapp for å lagre endringer
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = hasChanges && !isUpdating // Knappen er deaktivert hvis ingen endringer er gjort eller oppdatering pågår
            ) {
                Text(text = if (isUpdating) "Lagrer..." else "Lagre endringer")
            }
        }
    }

    // Dialog for å bekrefte lagring av endringer
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Bekreftelse") },
            text = { Text("Er du sikker på at du vil lagre endringene?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateActivity(
                            category = category,
                            activityId = activityId,
                            updatedActivity = createActivity(
                                title = title,
                                description = description,
                                maxParticipants = maxParticipants.toInt(),
                                ageGroup = ageGroup.toInt(),
                                location = location,
                                date = date,
                                time = time,
                                imageUrl = imageUrl  // Oppdatert bilde-URL
                            )
                        )
                        showDialog = false
                    }
                ) {
                    Text("Bekreft")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Avbryt")
                }
            }
        )
    }

    // Hvis oppdateringen er vellykket, naviger tilbake
    if (updateSuccess == true) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}
