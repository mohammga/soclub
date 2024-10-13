package com.example.soclub.screens.ads

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.models.Ad

@Composable
fun AdsScreen(
    navController: NavController,
    viewModel: AdsViewModel = hiltViewModel(),  // HiltViewModel-injeksjon
) {
    // Observerer aktiviteter fra ViewModel
    val activities = viewModel.activities.collectAsState()

    // Kaller fetchActivitiesByCreator når composable vises
    LaunchedEffect(Unit) {
        viewModel.fetchActivitiesByCreator()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bruker aktivitetsdataene fra ViewModel i stedet for hardkodede data
            items(activities.value.size) { index ->
                val activity = activities.value[index]
                EntryItem(
                    imageUrl = activity.imageUrl ?: "",  // Bruk imageUrl fra Activity-objektet
                    title = activity.title ?: "Ingen tittel",
                    time = activity.time ?: "Ukjent tid",
                    onCancelClick = { /* Håndter kanselleringsklikk */ }
                )
            }
        }
    }
}

@Composable
fun EntryItem(
    imageUrl: String,
    title: String,
    time: String,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Håndter klikk på oppføringen */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        EventImage(imageUrl)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            DeleteButton(onClick = onCancelClick)
        }
    }
}

@Composable
fun EventImage(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = null,
        modifier = Modifier
            .width(120.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun DeleteButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        Text(text = "Slett")
    }
}

@Preview(showBackground = true)
@Composable
fun AdsScreenPreview() {
    AdsScreen(navController = rememberNavController())
}
