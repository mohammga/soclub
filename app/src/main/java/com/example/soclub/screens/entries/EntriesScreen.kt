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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import java.util.Locale


@Composable
fun EntriesScreen(navController: NavHostController) {
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Tabs(selectedTab = selectedTab, setSelectedTab = setSelectedTab)

        if (selectedTab == 0) {
            ActiveEntriesList()
        }

        if (selectedTab == 1) {
            // utløpte aktiviteter
        }

        if (selectedTab == 2) {
            cancelled()

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
            text = { Text("Utløpte") },
            selected = selectedTab == 1,
            onClick = { setSelectedTab(1) }
        )

        Tab(
            text = { Text("Kansellerte") },
            selected = selectedTab == 2,
            onClick = { setSelectedTab(2) }
        )
    }
}



@Composable
fun ActiveEntriesList(viewModel: EntriesScreenViewModel = hiltViewModel()) {
    val activeActivities by viewModel.activeActivities.collectAsState()
    val isLoading by viewModel.isLoadingActive.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
                EntryItemCancelButton(
                    imageUrl = activity.imageUrl, // Send imageUrl fra databasen
                    title = activity.title,
                    date = activity.date,  // Vi bruker "date" nå, ikke "time"
                    onCancelClick = { /* Håndter kansellering */ }
                )
            }
        }
    }
}


//@Composable
//fun InactiveEntriesList(viewModel: EntriesScreenViewModel = hiltViewModel()) {
//    val inactiveActivities by viewModel.notActiveActivities.collectAsState()
//    val isLoadingInactive by viewModel.isLoadingInactive.collectAsState()
//
//    if (isLoadingInactive) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator() // Viser en spinner mens data lastes
//        }
//    } else if (inactiveActivities.isEmpty()) {
//        Text(text = "Ingen utgåtte aktiviteter funnet", modifier = Modifier.padding(16.dp))
//    } else {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(inactiveActivities.size) { index ->
//                val activity = inactiveActivities[index]
//                EntryItem(
//                    imageUrl = activity.imageUrl, // Bruk et standardbilde inntil dynamiske bilder er på plass
//                    title = activity.title,
//                    time = activity.date.toString(),
//                    onCancelClick = { /* Håndter kansellering */ }
//                )
//            }
//        }
//    }
//}



@Composable
fun cancelled(viewModel: EntriesScreenViewModel = hiltViewModel()) {
    val inactiveActivities by viewModel.notActiveActivities.collectAsState()
    val isLoadingInactive by viewModel.isLoadingInactive.collectAsState()

    if (isLoadingInactive) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator() // Viser en spinner mens data lastes
        }
    } else if (inactiveActivities.isEmpty()) {
        Text(text = "Ingen utgåtte aktiviteter funnet", modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(inactiveActivities.size) { index ->
                val activity = inactiveActivities[index]
                EntryItem(
                    imageUrl = activity.imageUrl, // Bruk et standardbilde inntil dynamiske bilder er på plass
                    title = activity.title,
                    date = activity?.date,
                    onCancelClick = { /* Håndter kansellering */ }
                )
            }
        }
    }
}

@Composable
fun EntryItem(
    imageUrl: String?,
    title: String,
    date: Timestamp?,  // Bruker Timestamp i stedet for String
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Håndter klikk på oppføringen */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EventImage(imageUrl) // Send imageUrl til EventImage

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DateDisplay(date = date)

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp)
        }
    }
}

@Composable
fun EntryItemCancelButton(
    imageUrl: String?,
    title: String,
    date: Timestamp?,  // Bruker Timestamp
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Håndter klikk på oppføringen */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EventImage(imageUrl) // Viser bildet

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Bruker DateDisplay-funksjonen for å vise dato
            DateDisplay(date = date)

            Spacer(modifier = Modifier.height(8.dp))

            CancelButton(onClick = onCancelClick)

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp)
        }
    }
}

@Composable
fun EventImage(imageUrl: String?) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl), // Bruk URL fra imageUrl
        contentDescription = null,
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun DateDisplay(date: Timestamp?) {
    // Sjekk om dato er null, hvis den er det viser vi en fallback tekst
    val formattedDate = date?.let {
        val sdf = SimpleDateFormat("EEEE, d. MMMM yyyy, HH:mm", Locale("no", "NO")) // Norsk lokalisering
        val dateStr = sdf.format(it.toDate())
        // Gjør første bokstav i ukedagen stor
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } ?: "Ukjent tid"  // Fallback om datoen er null

    // Vis formatert dato
    Text(
        text = formattedDate,
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
