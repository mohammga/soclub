package com.example.soclub.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.soclub.models.Activity
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.soclub.R
import androidx.compose.foundation.pager.HorizontalPager




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    // Henter kategorier fra ViewModel (som blir brukt som tabs)
    val categories by viewModel.getCategories().observeAsState(emptyList())

    // Behandler hvilken kategori som er valgt
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { categories.size })

    Column(modifier = Modifier.fillMaxSize()) {

        // Dynamisk Tab row for kategoriene fra databasen
        if (categories.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,  // Synkronisert med PagerState
                edgePadding = 2.dp
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        text = { Text(category) },
                        selected = pagerState.currentPage == index,  // Vis den riktige linjen
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)  // Oppdater siden basert på valgt kategori
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Innholdet basert på valgt kategori
        HorizontalPager(
            state = pagerState,

        ) { page ->
            // Henter aktiviteter basert på den valgte kategorien (via index)
            val selectedCategory = categories[page]
            val activities by viewModel.getActivities(selectedCategory).observeAsState(emptyList())

            // Liste over aktiviteter for valgt kategori
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activities) { activity ->
                    ActivityItem(activity = activity) {
                        // Naviger til detaljskjerm med aktivitetens informasjon
                        navController.navigate("detail")
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {

        Image(
            painter = painterResource(id = R.drawable.yoga), // Bruk et statisk bilde som placeholder
            contentDescription = activity.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Viser aktivitetens tittel og informasjon hentet fra Firestore
        Text(
            text = activity.title ?: "Ingen tittel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(text = activity.description ?: "Ingen beskrivelse", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Sted: ${activity.location ?: "Ukjent"}", style = MaterialTheme.typography.bodySmall)
        Text(text = "Aldersgruppe: ${activity.ageGroup ?: "Uspesifisert"}", style = MaterialTheme.typography.bodySmall)
    }
}
