package com.example.soclub.screens.activityDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.models.UserInfo
import com.example.soclub.utils.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Main composable function for displaying activity details.
 * Shows information like title, description, participants, and registration buttons.
 *
 * @param category The category of the activity.
 * @param activityId The unique identifier of the activity.
 * @param viewModel The ViewModel responsible for managing the activity state.
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
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
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
                                        viewModel.updateRegistrationForActivity(activityId, category, true)
                                        showToast(context, true, activity, currentParticipants)
                                    }
                                },
                                onUnregisterClick = {
                                    if (activityId != null && category != null) {
                                        viewModel.updateRegistrationForActivity(activityId, category, false)
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
 * Displays the detailed content of the activity, including the title, participants, description, and registration options.
 *
 * @param activity The activity object containing the details.
 * @param currentParticipants The number of participants currently registered.
 * @param isRegistered Whether the current user is already registered.
 * @param isCreator Whether the current user is the creator of the activity.
 * @param canRegister Whether the user is eligible to register for the activity.
 * @param ageGroup The age group requirement for the activity.
 * @param publisherUser The user who created the activity.
 * @param isProcessingRegistration Whether a registration or unregistration process is ongoing.
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
                    subText = stringResource(R.string.date_label)
                )
                InfoRow(
                    icon = Icons.Default.People,
                    mainText = "$currentParticipants / ${activity?.maxParticipants ?: 0}",
                    subText = stringResource(R.string.participants_label)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    mainText = activity?.startTime ?: stringResource(R.string.unknown_start_time),
                    subText = stringResource(R.string.start_time_label)
                )
                InfoRow(
                    icon = Icons.Default.Groups,
                    mainText = "${activity?.ageGroup ?: 0}+",
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
            text = stringResource(R.string.last_updated, activity?.lastUpdated?.let { formatDate(it) }
                ?: stringResource(R.string.unknown)),
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
 * Displays a styled information row with an icon, main text, and subtext.
 *
 * @param icon The icon to display in the row.
 * @param mainText The main information to display.
 * @param subText The secondary information (label).
 */
@Composable
fun InfoRow(icon: ImageVector, mainText: String, subText: String) {
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
 * Displays the description of the activity in a readable format.
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
 * Displays a clickable GPS map showing the location of the activity.
 * Allows users to open navigation in Google Maps.
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
            }
    }

    val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=${Uri.encode(destinationLocation)}&zoom=15&size=600x300&markers=color:red%7Clabel:S%7C${Uri.encode(destinationLocation)}&key=$apiKey"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                val gmmIntentUri = userLocation.value?.let {
                    "https://www.google.com/maps/dir/?api=1&origin=${it.latitude},${it.longitude}&destination=${
                        Uri.encode(
                            destinationLocation
                        )
                    }"
                } ?: "https://www.google.com/maps/search/?api=1&query=${
                    Uri.encode(
                        destinationLocation
                    )
                }"

                openGoogleMaps(context, gmmIntentUri)
            }
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(staticMapUrl),
            contentDescription = "Map of $destinationLocation",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Displays a button for registering or unregistering from an activity.
 *
 * @param isRegistered Indicates whether the user is already registered.
 * @param isCreator Indicates whether the user is the creator of the activity.
 * @param canRegister Indicates whether the user can register for the activity.
 * @param currentParticipants The current number of participants.
 * @param maxParticipants The maximum number of participants allowed.
 * @param ageGroup The age group limit for the activity.
 * @param isProcessingRegistration Indicates whether a registration/unregistration is in progress.
 * @param onRegisterClick Callback when the user clicks to register.
 * @param onUnregisterClick Callback when the user clicks to unregister.
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
        isProcessingRegistration -> if (isRegistered) {
            stringResource(R.string.unregistering_you)
        } else {
            stringResource(R.string.registering_you)
        }
        isFull -> stringResource(R.string.no_places_left)
        isCreator -> stringResource(R.string.own_activity)
        !canRegister -> stringResource(R.string.under_age_limit, ageGroup)
        isRegistered -> stringResource(R.string.unregister)
        else -> stringResource(R.string.registerr)
    }

    val buttonColor = when {
        isFull || isCreator || !canRegister -> MaterialTheme.colorScheme.secondary
        isRegistered -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

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
        enabled = !isCreator && canRegister && !isFull && !isProcessingRegistration
    ) {
        Text(text = buttonText)
    }
}

/**
 * Displays the title of the activity in a bold font.
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
 * Displays the publisher's information, including name, email, and profile image.
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
                        painterResource(id = R.drawable.user)
                    }

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
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
                            text = stringResource(R.string.author),
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
