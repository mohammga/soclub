package com.example.soclub.screens.entries

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import com.example.soclub.models.Entry

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
fun ActiveEntriesList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activeEntries.size) { index ->
            val entry = activeEntries[index]
            EntryItem(
                imageRes = entry.imageRes,
                title = entry.title,
                time = entry.time,
                onCancelClick = { /* Håndter kanselleringsklikk */ }
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
        // Arrangement av bilde
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

            // Kanselleringsknapp
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

val activeEntries = listOf(
    Entry(R.drawable.event1, "Hvordan planlegge en tur til", "Ons, 19:00"),
    Entry(R.drawable.event2, "Mestrer kunsten å lage pasta", "Tor, 18:00"),
    Entry(R.drawable.event3, "Bygge en oppstartsbedrift", "Fre, 17:00"),
    Entry(R.drawable.event4, "Investere i eiendom", "Lør, 16:00"),
    Entry(R.drawable.event5, "Reise med barn", "Søn, 15:00"),
    Entry(R.drawable.event3, "Bygge en oppstartsbedrift", "Fre, 17:00"),
    Entry(R.drawable.event4, "Investere i eiendom", "Lør, 16:00"),
    Entry(R.drawable.event5, "Reise med barn", "Søn, 15:00"),
)

@Preview(showBackground = true)
@Composable
fun EntriesScreenPreview() {
    EntriesScreen(rememberNavController())
}
