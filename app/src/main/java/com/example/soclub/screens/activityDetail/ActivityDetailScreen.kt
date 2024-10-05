package com.example.soclub.screens.activityDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService

@Composable
fun ActivityDetailScreen(
    navController: NavController,
    category: String?,   // Ta imot kategori
    activityId: String?,
    activityService: ActivityService
) {
    val activity = remember { mutableStateOf<Activity?>(null) }

    LaunchedEffect(activityId, category) {
        if (activityId != null && category != null) {
            val fetchedActivity = activityService.getActivityById(category, activityId) // Bruk riktig kategori
            println("Hentet aktivitet: $fetchedActivity")
            activity.value = fetchedActivity
        }
    }

    // Render innholdet basert på aktivitetens data
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item { ActivityImage() }
        item { ActivityTitle(activity.value?.title ?: "Ingen tittel") }
        item { ActivityDate() }
        item { InfoRow(activity.value?.location ?: "Ukjent", activity.value?.ageGroup ?: "Alle") }
        item { ActivityDescription(activity.value?.description ?: "Ingen beskrivelse") }
        item { ActivityGPSImage() }
        item { ActivityRegisterButton() }
    }
}

@Composable
fun ActivityImage() {
    Image(
        painter = painterResource(id = R.drawable.yoga),
        contentDescription = "Welcome Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
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
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ActivityRegisterButton() {
    Button(
        onClick = { /* Legg til handling for knappen */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(48.dp)
    ) {
        Text(text = "Meld deg", color = Color.White)
    }
}


@Composable
fun InfoRow(mainText: String, subText: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.padding(end = 16.dp)
        ) {
            ElevatedCardExample()
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
fun ElevatedCardExample() {
    ElevatedCard(
        modifier = Modifier.size(50.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.LightGray)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_action_name),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
