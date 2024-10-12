package com.example.soclub.screens.activityDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.soclub.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.models.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope


@Composable
fun ActivityDetailScreen(
    navController: NavController,
    category: String?,
    activityId: String?,
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    // Collecting the activity details and registration status from the ViewModel
    val activity = viewModel.activity.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value
    val canRegister = viewModel.canRegister.collectAsState().value
    val currentParticipants = viewModel.currentParticipants.collectAsState().value


    // Snackbar state to show messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Fetch the activity details when the screen is first displayed
    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            viewModel.loadActivity(category, activityId)
        }
    }

    // Scaffold for structured layout
    Scaffold(

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    ActivityImage(imageUrl = activity?.imageUrl ?: "")
                }

                item {
                    ActivityDetailsContent(
                        activity = activity,
                        currentParticipants = currentParticipants,
                        isRegistered = isRegistered,
                        canRegister = canRegister,
                        ageGroup = activity?.ageGroup ?: 0,
                        activityId = activityId,
                        category = category,
                        viewModel = viewModel,
                        onRegisterClick = {
                            if (activityId != null && category != null) {
                                viewModel.updateRegistrationForActivity(category, activityId, true)
                                showSnackbar(true, snackbarHostState, scope, activity)  // Pass 'true' for registering
                            }
                        },
                        onUnregisterClick = {
                            if (activityId != null && category != null) {
                                viewModel.updateRegistrationForActivity(category, activityId, false)
                                showSnackbar(false, snackbarHostState, scope, activity)  // Pass 'false' for unregistering
                            }
                        }
                    )
                }
            }
        }
    )
}


fun showSnackbar(isRegistering: Boolean, snackbarHostState: SnackbarHostState, scope: CoroutineScope, activity: Activity?) {
    val maxParticipants = activity?.maxParticipants ?: 0
    val currentParticipants = activity?.currentParticipants ?: 0
    val remainingSlots = maxParticipants - currentParticipants

    val message = if (isRegistering) {
        if (remainingSlots > 0) {
            "Påmeldingen var vellykket. Det er $remainingSlots av $maxParticipants plasser igjen."
        } else {
            "Påmeldingen var vellykket. Alle plasser er nå fylt opp."
        }
    } else {
        "Du har nå meldt deg ut av aktiviteten."
    }

    scope.launch {
        snackbarHostState.showSnackbar(message)
    }
}
@Composable
fun ActivityDetailsContent(
    activity: Activity?,
    currentParticipants: Int,
    isRegistered: Boolean,
    canRegister: Boolean,
    ageGroup: Int,
    activityId: String?,
    category: String?,
    viewModel: ActivityDetailViewModel,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        ActivityTitle(activity?.title ?: "Ingen tittel")
        ActivityDate()
        InfoRow(
            icon = Icons.Default.LocationOn,
            mainText = activity?.location ?: "Ukjent",
            subText = activity?.restOfAddress ?: "Ukjent adresse"
        )
        InfoRow(
            icon = Icons.Default.People,
            mainText = "Maks ${activity?.maxParticipants ?: 0}",
            subText = "Aldersgruppe: ${activity?.ageGroup ?: "Alle"}"
        )
        ActivityDescription(activity?.description ?: "Ingen beskrivelse")
        ActivityGPSImage()
        ActivityRegisterButton(
            isRegistered = isRegistered,
            canRegister = canRegister,
            ageGroup = ageGroup,
            onRegisterClick = onRegisterClick,
            onUnregisterClick = onUnregisterClick
        )

        Text(text = "Registrerte deltakere: $currentParticipants")
        Text(text = "Maks antall deltakere: ${activity?.maxParticipants ?: 0}")
    }
}


@Composable
fun ActivityImage(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = "Activity Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ActivityTitle(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
fun ActivityDate() {
    Text(
        text = "Tirsdag. 28. august 2024",
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ActivityDescription(description: String) {
    Text(
        text = description,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun ActivityGPSImage() {
    Image(
        painter = painterResource(R.drawable.gpsbilde1),
        contentDescription = "GPS-bilde",
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ActivityRegisterButton(
    isRegistered: Boolean,
    canRegister: Boolean,
    ageGroup: Int,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    // Bestem teksten som skal vises på knappen
    val buttonText = when {
        !canRegister -> "Du er under aldersgrensen ($ageGroup)"  // Brukeren oppfyller ikke alderskravet
        isRegistered -> "Meld deg ut"  // Brukeren er allerede registrert
        else -> "Meld deg på"  // Brukeren kan melde seg på
    }

    // Bestem fargen på knappen
    val buttonColor = when {
        !canRegister -> Color.Gray  // Brukeren oppfyller ikke alderskravet, så knappen blir grå
        isRegistered -> Color.Red  // Brukeren er registrert, så knappen blir rød
        else -> Color.Black  // Brukeren kan melde seg på, så knappen blir svart
    }

    Button(
        onClick = {
            if (!canRegister) return@Button  // Hvis brukeren ikke kan melde seg, gjør ingenting
            if (isRegistered) onUnregisterClick() else onRegisterClick()
        },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp),
        enabled = canRegister  // Deaktiver knappen hvis brukeren ikke oppfyller alderskravet
    ) {
        Text(text = buttonText, color = Color.White)
    }
}


@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    mainText: String,
    subText: String
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(end = 16.dp)) {
            ElevatedCardExample(icon = icon)
        }
        Column {
            Text(text = mainText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(text = subText, color = Color.Gray)
        }
    }
}

@Composable
fun ElevatedCardExample(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ElevatedCard(
        modifier = Modifier.size(50.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.LightGray)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

