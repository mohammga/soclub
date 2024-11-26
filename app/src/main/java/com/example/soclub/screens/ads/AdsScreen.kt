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

/**
 * Composable for displaying the Ads screen.
 *
 * - Shows a loading indicator while fetching data.
 * - Displays an error message if fetching data fails.
 * - Displays a message if there are no published ads.
 * - Displays a list of activities published by the creator.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param viewModel ViewModel instance for managing ads data and state.
 */
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

/**
 * Composable for displaying a single entry in the ads list.
 *
 * - Shows the activity image, title, date, and time.
 * - Includes a button for editing the activity.
 * - Navigates to the edit activity screen when clicked.
 *
 * @param imageUrl URL of the activity's image.
 * @param title Title of the activity.
 * @param date Timestamp of the activity date.
 * @param time Start time of the activity.
 * @param activityId Unique identifier of the activity.
 * @param category Category of the activity.
 * @param navController Navigation controller for navigating to the edit screen.
 */
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

/**
 * Composable for displaying the formatted date and time of an activity.
 *
 * - Formats the date to a readable format (e.g., "Monday, 1. January 2024").
 * - Appends the activity's start time if available.
 *
 * @param date Timestamp representing the activity's date.
 * @param time Optional string representing the activity's start time.
 */
@Composable
fun DateDisplay(date: Timestamp?, time: String?) {
    val formattedDateTime = date?.let { it ->
        val sdf = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale("no", "NO"))
        val dateStr = sdf.format(it.toDate())
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } ?: stringResource(id = R.string.unknown_date)

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

/**
 * Composable for displaying an image for an activity.
 *
 * - Displays a placeholder image if no URL is provided.
 * - Clips the image into a rounded rectangle shape.
 *
 * @param imageUrl URL of the image to display.
 */
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



