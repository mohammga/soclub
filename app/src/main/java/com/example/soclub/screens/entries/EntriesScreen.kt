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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.soclub.R
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EntriesScreen(navController: NavHostController) {
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Tabs(selectedTab = selectedTab, setSelectedTab = setSelectedTab)

        if (selectedTab == 0) {
            ActiveEntriesList()
        } else {
            // Her kan logikk for å vise utgåtte oppføringer legges til hvis nødvendig
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activeActivities.size) { index ->
            val activity = activeActivities[index]
            EntryItem(
                imageRes = R.drawable.event1, // Bruk et standardbilde inntil dynamiske bilder er på plass
                title = activity.title,
                time = activity.date,
                onCancelClick = { /* Håndter kansellering */ }
            )
        }
    }
}

@Composable
fun EntryItem(
    imageRes: Int,
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
        EventImage(imageRes)

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
fun EventImage(imageRes: Int) {
    Image(
        painter = painterResource(id = imageRes),
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
