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
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.location.Location
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.util.Locale

fun openGoogleMaps(context: Context, gmmIntentUri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityDetailScreen(
    navController: NavController,
    category: String?,
    activityId: String?,
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    val activity = viewModel.activity.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value
    val canRegister = viewModel.canRegister.collectAsState().value
    val currentParticipants = viewModel.currentParticipants.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            viewModel.loadActivityWithStatus(category, activityId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = {
            when {
                isLoading -> {
                    // Viser en progressindikator mens data lastes
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    // Viser en feilmelding hvis det oppsto en feil
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 16.sp)
                    }
                }
                else -> {
                    // Viser innholdet når det er lastet uten feil
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
                                onRegisterClick = {
                                    if (activityId != null && category != null) {
                                        viewModel.updateRegistrationForActivity(category, activityId, true)
                                        showSnackbar(true, snackbarHostState, scope, activity, currentParticipants)
                                    }
                                },
                                onUnregisterClick = {
                                    if (activityId != null && category != null) {
                                        viewModel.updateRegistrationForActivity(category, activityId, false)
                                        showSnackbar(false, snackbarHostState, scope, activity, currentParticipants)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}



fun showSnackbar(isRegistering: Boolean, snackbarHostState: SnackbarHostState, scope: CoroutineScope, activity: Activity?, currentParticipants: Int) {
    val maxParticipants = activity?.maxParticipants ?: 0
    val remainingSlots = maxParticipants - currentParticipants
    scope.launch {
        delay(500)
    val message = if (isRegistering) {
        if (remainingSlots > 0) {
            "Påmeldingen var vellykket."
        } else {
            "Påmeldingen var vellykket. Alle plasser er nå fylt opp."
        }
    } else {
        "Du har nå meldt deg ut av aktiviteten."
    }
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
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    val context = LocalContext.current
    val fullLocation = activity?.location ?: stringResource(R.string.unknown_location)
    val lastWord = fullLocation.substringAfterLast(" ")
    val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

    Column(modifier = Modifier.padding(16.dp)) {
        ActivityTitle(activity?.title ?: stringResource(R.string.activity_no_title))
        ActivityDate(date = activity?.date)
        if (activity?.startTime?.isNotEmpty() == true) {
            Text(
                text = "Starttid: ${activity.startTime}",
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        InfoRow(
            icon = Icons.Default.LocationOn,
            mainText = lastWord,
            subText = restOfAddress
        )

        InfoRow(
            icon = Icons.Default.People,
            mainText = if (currentParticipants == 0) {
                stringResource(R.string.participants_max, activity?.maxParticipants ?: 0)
            } else {
                stringResource(
                    R.string.participants_registered,
                    currentParticipants,
                    if (currentParticipants > 1) "e" else "",
                    activity?.maxParticipants ?: 0
                )
            },
            subText = stringResource(R.string.age_group, activity?.ageGroup ?: "Alle")
        )

        ActivityDescription(activity?.description ?: stringResource(R.string.unknown_description))

        // Send lokasjon og kontekst til ActivityGPSImage
        ActivityGPSImage(context = context, destinationLocation = fullLocation)

        ActivityRegisterButton(
            isRegistered = isRegistered,
            canRegister = canRegister,
            ageGroup = ageGroup,
            onRegisterClick = onRegisterClick,
            onUnregisterClick = onUnregisterClick
        )
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
fun ActivityDate(date: Timestamp?) {
    val formattedDate = date?.let {

        val sdf = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale("no", "NO"))
        sdf.format(it.toDate())
    } ?: "Ukjent dato"

    Text(
        text = "Dato: $formattedDate",
        modifier = Modifier.padding(vertical = 4.dp)
    )
}




fun splitDescriptionWithNaturalFlow(description: String, linesPerChunk: Int = 1): String {
    val sentences = description.split(Regex("(?<=\\.)\\s+")) // Splitter teksten ved punktum etterfulgt av mellomrom
    val result = StringBuilder()
    var currentLines = 0

    for (sentence in sentences) {
        // Legg til setningen til resultatet
        result.append(sentence.trim()).append(" ")
        currentLines++

        // Legg til linjeskift etter hver fjerde linje
        if (currentLines % linesPerChunk == 0) {
            result.append("\n\n") // Dobbelt linjeskift for ekstra mellomrom
        }
    }

    return result.toString().trim() // Fjerner eventuelt overflødig mellomrom
}





@Composable
fun ActivityDescription(description: String) {
    val formattedDescription = remember(description) {
        splitDescriptionWithNaturalFlow(description)
    }

    Text(
        text = formattedDescription,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}



@Composable
fun ActivityGPSImage(context: Context, destinationLocation: String) {
    // Hent brukerens plassering
    val userLocation = remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Be om plasseringstillatelser hvis det ikke allerede er gitt
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Tillatelsen er ikke gitt, håndter det (f.eks. vis en melding til brukeren)
            return@LaunchedEffect
        }

        // Få nåværende plassering
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                userLocation.value = location
                println("User's current location: ${location?.latitude}, ${location?.longitude}")
            }
    }

    // Lag URL for Google Maps Static API med destinasjon
    val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=${Uri.encode(destinationLocation)}&zoom=15&size=600x300&markers=color:red%7Clabel:S%7C${Uri.encode(destinationLocation)}&key=AIzaSyBm7zH5lmtMtmL1gz5b6Hau89lSpqv1pwY"

    // Sjekk om lokasjonen er tilgjengelig
    val locationReady = userLocation.value != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                if (locationReady) {
                    val location = userLocation.value
                    if (location != null) {
                        val startLat = location.latitude
                        val startLng = location.longitude

                        // Bygg Google Maps URL med nåværende posisjon og destinasjon
                        val gmmIntentUri = "https://www.google.com/maps/dir/?api=1" +
                                "&origin=$startLat,$startLng" + // Startposisjon
                                "&destination=${Uri.encode(destinationLocation)}"

                        openGoogleMaps(context, gmmIntentUri)
                    }
                } else {
                    // Hvis brukerens lokasjon ikke er tilgjengelig, åpne destinasjonen uten startpunkt
                    openGoogleMaps(context, "https://www.google.com/maps/search/?api=1&query=${Uri.encode(destinationLocation)}")
                }
            }
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(staticMapUrl),
            contentDescription = "Map of $destinationLocation",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ActivityRegisterButton(
    isRegistered: Boolean,
    canRegister: Boolean,
    ageGroup: Int,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {

    val buttonText = when {
        !canRegister -> stringResource(R.string.under_age_limit, ageGroup)
        isRegistered -> stringResource(R.string.unregister)
        else -> stringResource(R.string.registerr)
    }


    val buttonColor = when {
        !canRegister -> Color.Gray
        isRegistered -> Color.Red
        else -> Color.Black
    }

    Button(
        onClick = {
            if (!canRegister) return@Button
            if (isRegistered) onUnregisterClick() else onRegisterClick()
        },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp),
        enabled = canRegister
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

