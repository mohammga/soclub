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

    // Fetch the activity details when the screen is first displayed
    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            viewModel.loadActivity(category, activityId)
        }
    }

    // Displaying the activity details using a lazy column for a scrollable layout
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            ActivityImage(imageUrl = activity?.imageUrl ?: "")  // Display the activity's image
        }

        item {
            // Display the rest of the activity details such as title, location, and registration button
            ActivityDetailsContent(
                activity = activity,
                isRegistered = isRegistered,
                onRegisterClick = { viewModel.registerForActivity(activityId!!) },
                onUnregisterClick = { viewModel.unregisterFromActivity(activityId!!) }
            )
        }
    }
}

@Composable
fun ActivityDetailsContent(
    activity: Activity?,
    isRegistered: Boolean,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    // Box layout for padding and alignment, using a column to organize content
    Box(modifier = Modifier.padding(16.dp)) {
        Column {
            ActivityTitle(activity?.title ?: "Ingen tittel")  // Display the title of the activity
            ActivityDate()  // Display the date of the activity (static for now)
            InfoRow(
                icon = Icons.Default.LocationOn,
                mainText = activity?.location ?: "Ukjent",
                subText = activity?.restOfAddress ?: "Ukjent adresse"
            )  // Display the location of the activity
            InfoRow(
                icon = Icons.Default.People,
                mainText = "Maks ${activity?.maxParticipants ?: "Ukjent"}",
                subText = "Aldersgruppe: ${activity?.ageGroup ?: "Alle"}"
            )  // Display the max participants and age group
            ActivityDescription(activity?.description ?: "Ingen beskrivelse")  // Show activity description
            ActivityGPSImage()  // Display a static GPS image for the activity location
            ActivityRegisterButton(
                isRegistered = isRegistered,
                onRegisterClick = onRegisterClick,
                onUnregisterClick = onUnregisterClick
            )  // Register/unregister button depending on user status
        }
    }
}

@Composable
fun ActivityImage(imageUrl: String) {
    // Displays the activity image using Coil for image loading
    Image(
        painter = rememberImagePainter(imageUrl),
        contentDescription = "Activity Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop  // Makes the image fill the width of the screen while cropping excess
    )
}

@Composable
fun ActivityTitle(title: String) {
    // Displays the title of the activity
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
fun ActivityDate() {
    // Displays the activity date (currently static)
    Text(
        text = "Tirsdag. 28. august 2024",
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ActivityDescription(description: String) {
    // Displays the description of the activity
    Text(
        text = description,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun ActivityGPSImage() {
    // Displays a static GPS image
    Image(
        painter = painterResource(R.drawable.gpsbilde1),
        contentDescription = "GPS-bilde",
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop  // Ensures the image scales and fits appropriately
    )
}

@Composable
fun ActivityRegisterButton(
    isRegistered: Boolean,
    onRegisterClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    // Changes button text and color depending on the registration status
    val buttonText = if (isRegistered) "Meld deg ut" else "Meld deg på"
    val buttonColor = if (isRegistered) Color.Red else Color.Black

    // Button to register or unregister for the activity
    Button(
        onClick = {
            if (isRegistered) onUnregisterClick() else onRegisterClick()
        },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp)
    ) {
        Text(text = buttonText, color = Color.White)  // Button text is dynamic based on registration status
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    mainText: String,
    subText: String
) {
    // Displays an icon with main and sub text, used for location and participant info
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(end = 16.dp)) {
            ElevatedCardExample(icon = icon)  // Displays the icon inside an elevated card
        }
        Column {
            Text(text = mainText, fontWeight = FontWeight.Bold, fontSize = 17.sp)  // Main text (e.g., location)
            Text(text = subText, color = Color.Gray)  // Subtext (e.g., address or age group)
        }
    }
}

@Composable
fun ElevatedCardExample(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    // Displays a circular elevated card with an icon in the center
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
            )  // Icon displayed inside the elevated card
        }
    }
}
