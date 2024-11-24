package com.example.soclub.screens.activityDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import android.widget.Toast
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.google.firebase.Timestamp
import com.example.soclub.models.UserInfo
import androidx.compose.foundation.shape.CircleShape
import com.example.soclub.utils.openGoogleMaps
import com.example.soclub.utils.showToast
import com.example.soclub.utils.formatDate
import com.example.soclub.utils.formatDateWithoutTime
import com.example.soclub.utils.splitDescriptionWithNaturalFlow

/**
 * Main composable function for the Activity Detail Screen.
 * Displays the details of a specific activity, including title, description, and participant information.
 *
 * @param category The category of the activity.
 * @param activityId The unique identifier for the activity.
 * @param viewModel The ViewModel handling the activity's state.
 */
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
    val publisherUser = viewModel.publisherUser.collectAsState().value


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
                                publisherUser = publisherUser,
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

/**
 * Displays an image for the activity. If no image URL is provided, a placeholder image is used.
 *
 * @param imageUrl The URL of the image to display.
 */
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
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop
    )
}

/**
 * Displays the title of the activity in a large, bold font.
 *
 * @param title The title of the activity.
 */
@Composable
fun ActivityTitle(title: String) {
    Text(
        text = title,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )
}

/**
 * Displays the publisher's information, including their name, email, and profile image.
 *
 * @param publisherUser The user information of the publisher.
 * @param createdAt The timestamp when the activity was created.
 */
@Composable
fun PublisherInfo(publisherUser: UserInfo?, createdAt: Timestamp?) {
    val context = LocalContext.current
    val formattedDate2 = remember(createdAt) {
        createdAt?.let { formatDate(it) } ?: context.getString(R.string.unknown_date)
    }

    if (publisherUser != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val painter = if (publisherUser.imageUrl.isNotEmpty()) {
                        rememberAsyncImagePainter(publisherUser.imageUrl)
                    } else {
                        painterResource(id = R.drawable.user) // Default image
                    }

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        val fullName = "${publisherUser.firstname} ${publisherUser.lastname}"
                        Text(
                            text = fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.author),//Forfatter
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Text(
                            text = stringResource(R.string.published, formattedDate2),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val email = publisherUser.email
                    if (email.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, context.getString(R.string.no_email_app_found), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.email_missing), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = stringResource(R.string.contact))
            }
        }
    }
}


/**
 * Displays the content of an activity, including details like participants, registration buttons, and description.
 *
 * @param activity The activity object containing details.
 * @param currentParticipants The current number of participants.
 * @param isRegistered Indicates whether the user is registered for the activity.
 * @param isCreator Indicates whether the current user is the creator of the activity.
 * @param canRegister Indicates whether the user can register for the activity.
 * @param ageGroup The age group limit for the activity.
 * @param publisherUser The user who created the activity.
 * @param isProcessingRegistration Indicates if a registration or unregistration action is being processed.
 * @param onRegisterClick Callback for registering to the activity.
 * @param onUnregisterClick Callback for unregistering from the activity.
 */
@Composable
fun ActivityDetailsContent(
    activity: Activity?,
    currentParticipants: Int,
    isRegistered: Boolean,
    isCreator: Boolean,
    canRegister: Boolean,
    ageGroup: Int,
    publisherUser: UserInfo?,
    isProcessingRegistration: Boolean,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    val context = LocalContext.current
    val fullLocation = activity?.location ?: stringResource(R.string.unknown_location)

    Column(modifier = Modifier.padding(16.dp)) {
        ActivityTitle(activity?.title ?: stringResource(R.string.activity_no_title))

        Spacer(modifier = Modifier.height(16.dp))

        PublisherInfo(publisherUser, activity?.createdAt)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.Event,
                    mainText = activity?.date?.let { formatDateWithoutTime(it) } ?: stringResource(R.string.unknown_date),
                    //subText = "Dato"
                    subText = stringResource(R.string.date_label)
                )
                InfoRow(
                    icon = Icons.Default.People,
                    mainText = if (currentParticipants == 0) {
                        "${0} av ${activity?.maxParticipants ?: 0}"
                    } else {
                        "$currentParticipants av ${activity?.maxParticipants ?: 0}"
                    },
                    subText = stringResource(R.string.participants_label)
                )

            }
            Column(modifier = Modifier.weight(1f)
                .padding(end = 8.dp)) {
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    mainText = activity?.startTime ?:  stringResource(R.string.unknown_start_time),
                    subText = stringResource(R.string.start_time_label)
                )
                InfoRow(
                    icon = Icons.Default.Groups,
                    mainText = "${activity?.ageGroup ?: stringResource(R.string.alle_age_group)}+",
                    subText = stringResource(R.string.agelimt_label)
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
            stringResource(R.string.last_updated, activity?.lastUpdated?.let { formatDate(it) } ?: stringResource(R.string.unknown)),
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

/**
 * Displays a row of information with an icon, a main text, and a subtext.
 * Used for activity details such as date and participant count.
 *
 * @param icon The icon to display in the row.
 * @param mainText The main text content.
 * @param subText The subtext description.
 */
@Composable
fun InfoRow(
    icon: ImageVector,
    mainText: String,
    subText: String
) {
    val backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
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

/**
 * Displays the description of the activity with proper formatting for readability.
 *
 * @param description The description text of the activity.
 */
@Composable
fun ActivityDescription(description: String) {
    val formattedDescription = remember(description) {
        splitDescriptionWithNaturalFlow(description)
    }

    Text(
        text = stringResource(R.string.description_label),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )

    Text(
        text = formattedDescription,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

/**
 * Displays the GPS location of the activity and allows navigation to the location using Google Maps.
 *
 * @param context The application context.
 * @param destinationLocation The location of the activity as a string.
 */
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
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                lc
            )

            return@LaunchedEffect
        }

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
                                "&origin=$startLat,$startLng" +
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

/**
 * Displays a button for registering or unregistering from an activity.
 * Handles UI states for registration status and limitations.
 *
 * @param isRegistered Indicates whether the user is already registered.
 * @param isCreator Indicates whether the user is the creator of the activity.
 * @param canRegister Indicates whether the user can register for the activity.
 * @param currentParticipants The current number of participants.
 * @param maxParticipants The maximum number of participants allowed.
 * @param ageGroup The minimum age required for the activity.
 * @param isProcessingRegistration Indicates if a registration action is in progress.
 * @param onRegisterClick Callback for registering the user.
 * @param onUnregisterClick Callback for unregistering the user.
 */
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
                stringResource(R.string.unregistering_you)
            } else {
                stringResource(R.string.registering_you)
            }
        }
        isFull -> stringResource(R.string.no_places_left)
        isCreator -> stringResource(R.string.own_activity)
        !canRegister -> stringResource(R.string.under_age_limit, ageGroup)
        isRegistered -> stringResource(R.string.unregister)
        else -> stringResource(R.string.registerr)
    }

    val buttonColor = when {
        isFull -> MaterialTheme.colorScheme.secondary
        isCreator || !canRegister -> MaterialTheme.colorScheme.secondary
        isRegistered -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
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
        Text(text = buttonText)
    }
}