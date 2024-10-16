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
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay


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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            viewModel.loadActivity(category, activityId)
        }
    }



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
    Column(modifier = Modifier.padding(16.dp)) {
        ActivityTitle(activity?.title ?: stringResource(R.string.activity_no_title))
        ActivityDate()
        InfoRow(
            icon = Icons.Default.LocationOn,
            mainText = activity?.location ?: stringResource(R.string.unknown_location),
            subText = activity?.restOfAddress ?: stringResource(R.string.unknown_address)
        )

        InfoRow(
            icon = Icons.Default.People,
            mainText = if (currentParticipants == 0) {
                stringResource(R.string.participants_max, activity?.maxParticipants?:0)
            } else {
                stringResource(R.string.participants_registered,
                    currentParticipants,
                    if (currentParticipants > 1) "e" else "",
                    activity?.maxParticipants ?:0

                    )
            },
            subText = stringResource(R.string.age_group, activity?.ageGroup ?: "Alle")
        )

        ActivityDescription(activity?.description ?: stringResource(R.string.unknown_description))
        ActivityGPSImage()
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

