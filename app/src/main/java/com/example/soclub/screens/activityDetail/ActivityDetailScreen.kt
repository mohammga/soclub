package com.example.soclub.screens.activityDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.rememberImagePainter
import com.example.soclub.service.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ActivityDetailScreen(
    navController: NavController,
    category: String?,   // Ta imot kategori
    activityId: String?,
    activityService: ActivityService,
    accountService: AccountService
) {
    val activity = remember { mutableStateOf<Activity?>(null) }
    val isRegistered = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            val fetchedActivity = activityService.getActivityById(category, activityId) // Bruk riktig kategori
            println("Hentet aktivitet: $fetchedActivity")
            activity.value = fetchedActivity

            // Sjekk om brukeren allerede er påmeldt
            val userId = accountService.currentUserId
            val registrationExists = activityService.isUserRegisteredForActivity(userId, activityId)
            isRegistered.value = registrationExists
        }
    }

    // Render innholdet basert på aktivitetens data
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        item { ActivityImage(imageUrl = activity.value?.imageUrl ?: "") }

        item {
            Box(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Column {
                    ActivityTitle(activity.value?.title ?: "Ingen tittel")
                    ActivityDate()
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        mainText = activity.value?.location ?: "Ukjent",
                        subText = activity.value?.restOfAddress ?: "Ukjent adresse"
                    )
                    // Andre InfoRow med personer-ikon
                    InfoRow(
                        icon = Icons.Default.People,
                        mainText = "Maks ${activity.value?.maxParticipants ?: "Ukjent"}",
                        subText = "Aldersgruppe: ${activity.value?.ageGroup ?: "Alle"}"
                    )
                    ActivityDescription(activity.value?.description ?: "Ingen beskrivelse")

                    ActivityGPSImage()
                    ActivityRegisterButton(
                        isRegistered = isRegistered.value,
                        onRegisterClick = {
                            coroutineScope.launch {
                                val userId = accountService.currentUserId
                                activityService.registerUserForActivity(userId, activityId!!)
                                isRegistered.value = true  // Oppdater statusen til "påmeldt"
                            }
                        },
                        onUnregisterClick = {
                            coroutineScope.launch {
                                val userId = accountService.currentUserId
                                activityService.unregisterUserFromActivity(userId, activityId!!)
                                isRegistered.value = false  // Oppdater statusen til "ikke påmeldt"
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityImage(imageUrl: String) {
    Image(
        painter = rememberImagePainter(imageUrl),
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
    isRegistered: Boolean,              // Sjekker om brukeren allerede er påmeldt
    onRegisterClick: () -> Unit,        // Funksjon som utføres ved påmelding
    onUnregisterClick: () -> Unit       // Funksjon som utføres ved avmelding
) {
    Button(
        onClick = {
            if (isRegistered) {
                onUnregisterClick()  // Hvis brukeren er påmeldt, meld brukeren av
            } else {
                onRegisterClick()  // Hvis brukeren ikke er påmeldt, meld brukeren på
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRegistered) Color.Red else Color.Black  // Farge basert på tilstanden
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp)
    ) {
        Text(
            text = if (isRegistered) "Meld deg ut" else "Meld deg på",  // Tekst basert på tilstanden
            color = Color.White
        )
    }
}



@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, mainText: String, subText: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.padding(end = 16.dp)
        ) {
            ElevatedCardExample(icon = icon)
        }
        Column {
            Text(
                text = mainText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
            Text(
                text = subText,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ElevatedCardExample(icon:androidx.compose.ui.graphics.vector.ImageVector) {
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
                modifier = Modifier
                    .size(30.dp)

            )
        }
    }
}
