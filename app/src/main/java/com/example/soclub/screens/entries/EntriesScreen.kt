package com.example.soclub.screens.entries

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


@Composable
fun EntriesScreen(navController: NavHostController) {
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Tabs(selectedTab = selectedTab, setSelectedTab = setSelectedTab)

        if (selectedTab == 0) {
            ActiveEntriesList()
        } else {
           InactiveEntriesList()
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
            text = { Text("Utgåtte") },
            selected = selectedTab == 1,
            onClick = { setSelectedTab(1) }
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
                EntryItem(
                    imageUrl = activity.imageUrl, // Send imageUrl fra databasen
                    title = activity.title,
                    time = activity.date,
                    onCancelClick = { /* Håndter kansellering */ }
                )
            }
        }
    }
}


@Composable
fun InactiveEntriesList(viewModel: EntriesScreenViewModel = hiltViewModel()) {
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
                    time = activity.date,
                    onCancelClick = { /* Håndter kansellering */ }
                )
            }
        }
    }
}



@Composable
fun EntryItem(
    imageUrl: String?, // Endre fra imageRes til imageUrl
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
        EventImage(imageUrl) // Send imageUrl til EventImage

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

            CancelButton(onClick = onCancelClick)
        }
    }
}

@Composable
fun EventImage(imageUrl: String?) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl), // Bruk URL fra imageUrl
        contentDescription = null,
        modifier = Modifier
            .width(120.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
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
