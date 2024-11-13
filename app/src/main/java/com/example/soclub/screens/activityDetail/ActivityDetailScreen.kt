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
import com.example.soclub.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.models.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
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
import android.widget.Toast
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.res.painterResource
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
    val context = LocalContext.current
    val isCreator = viewModel.isCreator.collectAsState().value
    val isProcessingRegistration = viewModel.isProcessingRegistration.collectAsState().value


    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            viewModel.loadActivityWithStatus(category, activityId)
        }
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
                        Text(text = errorMessage, color = Color.Red, fontSize = 16.sp)
                    }
                }
                else -> {
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
                                isCreator = isCreator,
                                canRegister = canRegister,
                                isProcessingRegistration = isProcessingRegistration,
                                ageGroup = activity?.ageGroup ?: 0,
                                onRegisterClick = {
                                    if (activityId != null && category != null) {
                                        viewModel.updateRegistrationForActivity(activityId, true)
                                        showToast(context, true, activity, currentParticipants)
                                    }
                                },
                                onUnregisterClick = {
                                    if (activityId != null && category != null) {
                                        viewModel.updateRegistrationForActivity(activityId, false)
                                        showToast(context, false, activity, currentParticipants)
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


fun showToast(context: Context, isRegistering: Boolean, activity: Activity?, currentParticipants: Int) {
    val maxParticipants = activity?.maxParticipants ?: 0
    val remainingSlots = maxParticipants - currentParticipants

    val message = if (isRegistering) {
        if (remainingSlots > 0) {
            context.getString(R.string.registration_successful)//"Påmeldingen var vellykket."
        } else {
            context.getString(R.string.registration_successful_filled)//"Påmeldingen var vellykket. Alle plasser er nå fylt opp."
        }
    } else {
        context.getString(R.string.unregistered_successful)//"Du har nå meldt deg ut av aktiviteten."
    }

    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


@Composable
fun ActivityDetailsContent(
    activity: Activity?,
    currentParticipants: Int,
    isRegistered: Boolean,
    isCreator: Boolean,
    canRegister: Boolean,
    ageGroup: Int,
    isProcessingRegistration: Boolean,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    val context = LocalContext.current
    val fullLocation = activity?.location ?: stringResource(R.string.unknown_location)

    Column(modifier = Modifier.padding(16.dp)) {
        ActivityTitle(activity?.title ?: stringResource(R.string.activity_no_title))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(
                    icon = Icons.Default.Event,
                    mainText = activity?.date?.let { formatDateWithoutTime(it) } ?: "Ukjent dato",
                    subText = "Dato"
                )
                InfoRow(
                    icon = Icons.Default.People,
                    mainText = if (currentParticipants == 0) {
                        stringResource(R.string.participants_max, activity?.maxParticipants ?: 0)
                    } else {
                        stringResource(
                            R.string.participants_registered,
                            currentParticipants,
                            if (currentParticipants > 1) "e" else "e",
                            activity?.maxParticipants ?: 0
                        )
                    },
                    subText = "Deltakere"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    mainText = activity?.startTime ?: "Ukjent starttid",
                    subText = "Starttid"
                )
                InfoRow(
                    icon = Icons.Default.Groups,
                    mainText = "${activity?.ageGroup ?: "Alle"}+",
                    subText = "Aldersgruppe"
                )
            }
        }

        ActivityDescription(activity?.description ?: stringResource(R.string.unknown_description))


        Text(
            text = fullLocation,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ActivityGPSImage(context = context, destinationLocation = fullLocation)

        Text(
            text = "Siste endret: ${activity?.lastUpdated?.let { formatDate(it) } ?: "Ukjent"}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ActivityRegisterButton(
            isRegistered = isRegistered,
            isCreator = isCreator,
            canRegister = canRegister,
            currentParticipants = currentParticipants,
            maxParticipants = activity?.maxParticipants ?: 0,
            ageGroup = ageGroup,
            onRegisterClick = onRegisterClick,
            isProcessingRegistration = isProcessingRegistration,
            onUnregisterClick = onUnregisterClick
        )
    }
}



fun formatDate(date: Timestamp): String {
    val sdf = SimpleDateFormat("d. MMM yyyy, HH:mm", Locale("no", "NO"))
    return sdf.format(date.toDate())
}

fun formatDateWithoutTime(date: Timestamp): String {
    val sdf = SimpleDateFormat("d. MMM yyyy", Locale("no", "NO"))
    return sdf.format(date.toDate())
}



@Composable
fun ActivityImage(imageUrl: String) {
    val painter = if (imageUrl.isNotBlank()) {
        rememberAsyncImagePainter(model = imageUrl)
    } else {
        painterResource(id = R.drawable.placeholder)
    }

    Image(
        painter = painter,
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
        fontSize = 32.sp,
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
    val sentences = description.split(Regex("(?<=\\.)\\s+"))
    val result = StringBuilder()
    var currentLines = 0

    for (sentence in sentences) {
        result.append(sentence.trim()).append(" ")
        currentLines++

        if (currentLines % linesPerChunk == 0) {
            result.append("\n\n")
        }
    }

    return result.toString().trim()
}

@Composable
fun ActivityDescription(description: String) {
    val formattedDescription = remember(description) {
        splitDescriptionWithNaturalFlow(description)
    }

    Text(
        text = "Beskrivelse",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )

    Text(
        text = formattedDescription,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun ActivityGPSImage(context: Context, destinationLocation: String) {
    val userLocation = remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val lc = 1001
    val apiKey = try {
        val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        ai.metaData.getString("com.google.android.geo.API_KEY")
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    LaunchedEffect(Unit) {
        // Sjekker tillatelser
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as android.app.Activity, // Bruker fullt kvalifisert navn
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                lc
            )

            return@LaunchedEffect
        }

        // Få siste plassering hvis tillatelse er gitt
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                userLocation.value = location
                println("User's current location: ${location?.latitude}, ${location?.longitude}")
            }
    }
    val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=${Uri.encode(destinationLocation)}&zoom=15&size=600x300&markers=color:red%7Clabel:S%7C${Uri.encode(destinationLocation)}&key=$apiKey"
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

                        val gmmIntentUri = "https://www.google.com/maps/dir/?api=1" +
                                "&origin=$startLat,$startLng" + // Startposisjon
                                "&destination=${Uri.encode(destinationLocation)}"

                        openGoogleMaps(context, gmmIntentUri)
                    }
                } else {
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
    isCreator: Boolean,
    canRegister: Boolean,
    currentParticipants: Int,
    maxParticipants: Int,
    ageGroup: Int,
    isProcessingRegistration: Boolean,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    val isFull = currentParticipants >= maxParticipants

    val buttonText = when {
        isProcessingRegistration -> {
            if (isRegistered) {
                stringResource(R.string.unregistering_you) // "Melder deg ut..."
            } else {
                stringResource(R.string.registering_you) // "Melder deg..."
            }
        }
        isFull -> stringResource(R.string.no_places_left)
        isCreator -> stringResource(R.string.own_activity)
        !canRegister -> stringResource(R.string.under_age_limit, ageGroup)
        isRegistered -> stringResource(R.string.unregister)
        else -> stringResource(R.string.registerr)
    }

    val buttonColor = when {
        isFull -> Color.Gray
        isCreator || !canRegister -> Color.Gray
        isRegistered -> Color.Red
        else -> Color.Black
    }

    val buttonEnabled = !isCreator && canRegister && !isFull && !isProcessingRegistration

    Button(
        onClick = {
            if (isRegistered) {
                onUnregisterClick()
            } else {
                onRegisterClick()
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp),
        enabled = buttonEnabled
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
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Text(
                text = subText,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = mainText,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
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

