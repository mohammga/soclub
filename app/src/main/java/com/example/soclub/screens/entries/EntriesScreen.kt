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
import androidx.compose.ui.platform.LocalContext // Importer dette
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import com.google.firebase.Timestamp
import java.util.Locale

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
            text = { Text("Aktive") },
            selected = selectedTab == 0,
            onClick = { setSelectedTab(0) }
        )

        Tab(
            text = { Text("Kansellerte") },
            selected = selectedTab == 1,
            onClick = { setSelectedTab(1) }
        )
    }
}

@Composable
fun ActiveEntriesList(navController: NavHostController, viewModel: EntriesScreenViewModel = hiltViewModel()) {
    val activeActivities by viewModel.activeActivities.collectAsState()
    val isLoading by viewModel.isLoadingActive.collectAsState()
    LocalContext.current

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (activeActivities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Det er ingen aktiviteter som er aktive", modifier = Modifier.padding(16.dp))
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
                    onCancelClick = { viewModel.cancelRegistration(activity.id) }, // Ikke nødvendig å passere Context her
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
            Text(text = "Det er ingen aktiviteter som er kansellerte", modifier = Modifier.padding(16.dp))
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

@Composable
fun ActiveEntryItem(
    imageUrl: String?,
    title: String,
    date: Timestamp?,
    time: String?,
    onCancelClick: () -> Unit,
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
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DateDisplay(date = date, time = time)
            Spacer(modifier = Modifier.height(8.dp))
            CancelButton(onClick = onCancelClick)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp)
        }
    }
}

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
            Text(text = title ?: "Ukjent tittel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DateDisplay(date = date, time = time)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp)
        }
    }
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

@Composable
fun DateDisplay(date: Timestamp?, time: String?) {
    val formattedDateTime = date?.let {
        val sdf = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale("no", "NO"))
        val dateStr = sdf.format(it.toDate())
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } ?: "Ukjent dato"

    // Koble sammen dato og tid hvis begge er tilgjengelige
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
fun CancelButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        Text(text = "Kanseller")
    }
}
