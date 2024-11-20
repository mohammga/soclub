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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import java.util.Locale
import android.icu.text.SimpleDateFormat
import com.google.firebase.Timestamp



@Composable
fun AdsScreen(
    navController: NavController,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchActivitiesByCreator()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            activities.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource (id = R.string.ingen_publiserte_annonser), color = MaterialTheme.colorScheme.onBackground)
                    
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activities.size) { index ->
                        val activity = activities[index]
                        EntryItem(
                            imageUrl = activity.imageUrl,
                            title = activity.title,
                            date = activity.date,
                            time = activity.startTime,
                            activityId = activity.creatorId,
                            category = activity.category,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EntryItem(
    imageUrl: String?,
    title: String?,
    date: Timestamp?,
    time: String?,
    activityId: String,
    category: String,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("editActivity/$category/$activityId")
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EventImage(imageUrl)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title ?: stringResource(R.string.no_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DateDisplay(date = date, time = time)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate("editActivity/$category/$activityId")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.Endre_annonse))
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp)
        }
    }
}

@Composable
fun DateDisplay(date: Timestamp?, time: String?) {
    val formattedDateTime = date?.let { it ->
        val sdf = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale("no", "NO"))
        val dateStr = sdf.format(it.toDate())
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } ?: "Ukjent dato"

    val displayText = if (time != null) {
        "$formattedDateTime, $time"
    } else {
        formattedDateTime
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}


@Composable
fun EventImage(imageUrl: String?) {
    val imagePainter = if (imageUrl.isNullOrEmpty()) {
        painterResource(id = R.drawable.placeholder)
    } else {
        rememberAsyncImagePainter(imageUrl)
    }

    Image(
        painter = imagePainter,
        contentDescription = null,
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}



