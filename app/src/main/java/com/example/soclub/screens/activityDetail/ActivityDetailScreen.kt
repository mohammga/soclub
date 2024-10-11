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
import coil.compose.rememberImagePainter
import androidx.compose.runtime.collectAsState
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.models.Activity

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
    val canRegister = viewModel.canRegister.collectAsState().value  // Hent canRegister fra ViewModel


    // Fetch the activity details when the screen is first displayed
    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            viewModel.loadActivity(category, activityId)
        }
    }


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
                isRegistered = isRegistered,
                canRegister = canRegister,  // Sender canRegister til knappen
                ageGroup = activity?.ageGroup ?: 0,  // Passer inn aldersgrensen
                activityId = activityId,   // Sender inn activityId
                viewModel = viewModel,     // Sender inn ViewModel
                onRegisterClick = { viewModel.updateRegistrationForActivity(activityId!!, true) },
                onUnregisterClick = { viewModel.updateRegistrationForActivity(activityId!!, false) }
            )
        }
    }
}

@Composable
fun ActivityDetailsContent(
    activity: Activity?,
    isRegistered: Boolean,
    canRegister: Boolean,      // Legg til canRegister som parameter
    ageGroup: Int,             // Passer inn aldersgrensen
    activityId: String?,       // Legg til activityId som parameter
    viewModel: ActivityDetailViewModel, // Legg til viewModel som parameter
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {

    Box(modifier = Modifier.padding(16.dp)) {
        Column {
            ActivityTitle(activity?.title ?: "Ingen tittel")
            ActivityDate()
            InfoRow(
                icon = Icons.Default.LocationOn,
                mainText = activity?.location ?: "Ukjent",
                subText = activity?.restOfAddress ?: "Ukjent adresse"
            )
            InfoRow(
                icon = Icons.Default.People,
                mainText = "Maks ${activity?.maxParticipants ?: "Ukjent"}",
                subText = "Aldersgruppe: ${activity?.ageGroup ?: "Alle"}"
            )
            ActivityDescription(activity?.description ?: "Ingen beskrivelse")
            ActivityGPSImage()
            ActivityRegisterButton(
                isRegistered = isRegistered,
                canRegister = canRegister,  // Passer canRegister-verdien til knappen
                ageGroup = ageGroup,        // Passer aldersgrensen for sjekk
                onRegisterClick = {
                    if (activityId != null) {
                        viewModel.updateRegistrationForActivity(activityId, true)
                    }
                },
                onUnregisterClick = {
                    if (activityId != null) {
                        viewModel.updateRegistrationForActivity(activityId, false)
                    }
                }
            )
        }
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
    val buttonText = if (isRegistered) {
        "Meld deg ut"
    } else if (!canRegister) {
        "Du er under aldersgrensen ($ageGroup)"
    } else {
        "Meld deg på"
    }

    val buttonColor = if (isRegistered || !canRegister) Color.Red else Color.Black

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
