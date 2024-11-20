package com.example.soclub.screens.entries

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import com.google.firebase.Timestamp
import java.util.Locale

/**
 * Composable function to display the entries screen with active and cancelled entries.
 *
 * @param navController The [NavHostController] to handle navigation.
 */
@Composable
fun EntriesScreen(navController: NavHostController) {
    val (selectedTab, setSelectedTab) = remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Tabs(selectedTab = selectedTab, setSelectedTab = setSelectedTab)

        when (selectedTab) {
            0 -> ActiveEntriesList(navController)
            1 -> CancelledEntriesList(navController)
        }
    }
}

/**
 * Composable function to display tabs for selecting between active and cancelled entries.
 *
 * @param selectedTab The index of the currently selected tab.
 * @param setSelectedTab Lambda function to set the selected tab index.
 */
@Composable
fun Tabs(selectedTab: Int, setSelectedTab: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab,
        indicator = { tabPositions ->
            SecondaryIndicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .height(4.dp)
            )
        }
    ) {
        Tab(
            text = { Text(stringResource(R.string.Aktive)) },
            selected = selectedTab == 0,
            onClick = { setSelectedTab(0) }
        )

        Tab(
            text = { Text(stringResource(R.string.cancelled)) },
            selected = selectedTab == 1,
            onClick = { setSelectedTab(1) }
        )
    }
}

/**
 * Composable function to display a list of active entries.
 *
 * @param navController The [NavHostController] to handle navigation.
 * @param viewModel The [EntriesScreenViewModel] for managing active entries.
 */
@Composable
fun ActiveEntriesList(
    navController: NavHostController,
    viewModel: EntriesScreenViewModel = hiltViewModel()
) {
    val activeActivities by viewModel.activeActivities.collectAsState()
    val isLoading by viewModel.isLoadingActive.collectAsState()
    val isProcessingCancellation by viewModel.isProcessingCancellation.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (activeActivities.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.No_Aktivirty_is_activ),
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activeActivities.size) { index ->
                val activity = activeActivities[index]
                ActiveEntryItem(
                    imageUrl = activity.imageUrl,
                    title = activity.title,
                    time = activity.startTime,
                    date = activity.date,
                    isProcessingCancellation = isProcessingCancellation == activity.id,
                    onCancelClick = { viewModel.cancelRegistration(activity.id) },
                    onClick = {
                        activity.category?.let { category ->
                            activity.id.let { id ->
                                navController.navigate("detail/$category/$id")
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Composable function to display a list of cancelled entries.
 *
 * @param navController The [NavHostController] to handle navigation.
 * @param viewModel The [EntriesScreenViewModel] for managing cancelled entries.
 */
@Composable
fun CancelledEntriesList(navController: NavHostController, viewModel: EntriesScreenViewModel = hiltViewModel()) {
    val cancelledActivities by viewModel.notActiveActivities.collectAsState()
    val isLoading by viewModel.isLoadingInactive.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (cancelledActivities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.No_Aktivirty_is_cancelled),modifier = Modifier.padding(16.dp))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cancelledActivities.size) { index ->
                val activity = cancelledActivities[index]
                CancelledEntryItem(
                    imageUrl = activity.imageUrl,
                    title = activity.title,
                    date = activity.date,
                    time = activity.startTime,
                    onClick = {
                        activity.category?.let { category ->
                            activity.id.let { id ->
                                navController.navigate("detail/$category/$id")
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Composable function to display an active entry item.
 *
 * @param imageUrl The URL of the entry's image.
 * @param title The title of the entry.
 * @param date The date of the entry.
 * @param time The time of the entry.
 * @param isProcessingCancellation Whether the cancellation is being processed.
 * @param onCancelClick Lambda to handle cancellation clicks.
 * @param onClick Lambda to handle item clicks.
 */
@Composable
fun ActiveEntryItem(
    imageUrl: String?,
    title: String,
    date: Timestamp?,
    time: String?,
    isProcessingCancellation: Boolean,
    onCancelClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        EventImage(imageUrl)
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DateDisplay(date = date, time = time)
            Spacer(modifier = Modifier.height(8.dp))
            CancelButton(
                onClick = onCancelClick,
                isProcessing = isProcessingCancellation
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp)
        }
    }
}

/**
 * Composable function to display a cancelled entry item.
 *
 * @param imageUrl The URL of the entry's image.
 * @param title The title of the entry.
 * @param date The date of the entry.
 * @param time The time of the entry.
 * @param onClick Lambda to handle item clicks.
 */
@Composable
fun CancelledEntryItem(
    imageUrl: String?,
    title: String?,
    time: String?,
    date: Timestamp?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EventImage(imageUrl)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title ?: stringResource(R.string.unknown_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DateDisplay(date = date, time = time)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp)
        }
    }
}

/**
 * Composable function to display an image for an entry.
 *
 * @param imageUrl The URL of the image to display.
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

/**
 * Composable function to display the date and time of an entry.
 *
 * @param date The date of the entry.
 * @param time The time of the entry.
 */
@Composable
fun DateDisplay(date: Timestamp?, time: String?) {
    val formattedDateTime = date?.let { it ->
        val sdf = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale("no", "NO"))
        val dateStr = sdf.format(it.toDate())
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } ?: stringResource(R.string.unknown_dato)

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
 * Composable function to display a button for cancelling an entry.
 *
 * @param onClick Lambda to handle button clicks.
 * @param isProcessing Whether the cancellation is being processed.
 */
@Composable
fun CancelButton(onClick: () -> Unit, isProcessing: Boolean) {
    val buttonText = if (isProcessing) {
        stringResource(R.string.cancelling_you)
    } else {
        stringResource(R.string.kanseller)
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        enabled = !isProcessing
    ) {
        Text(text = buttonText)
    }
}

