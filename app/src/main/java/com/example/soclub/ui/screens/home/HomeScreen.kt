package com.example.soclub.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.example.soclub.R

data class Activity(val imageResId: Int, val title: String, val description: String, val ageGroup: Any, val maxParticipants : Any, val location : Any )  {
}

@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = activity.imageResId),
            contentDescription = activity.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = activity.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val activities = listOf(
        Activity(R.drawable.yoga, "Yoga", "Beskrivelse", "18+", 20, "Oslo"),
        Activity(R.drawable.kaffe, "Kaffe", "Beskrivelse", "Alle aldre", 10, "Bergen"),
        Activity(R.drawable.svomming, "Svømming", "Beskrivelse", "Alle aldre", 30, "Stavanger"),
        Activity(R.drawable.svomming, "Svømming", "Beskrivelse", "Alle aldre", 30, "Stavanger")
    )
    val tabTitles = listOf("Forslag", "Fest", "Festival", "Trening", "Mat")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        ScrollableTabRow(selectedTabIndex = pagerState.currentPage,  edgePadding = 2.dp) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    modifier = Modifier.padding(10.dp),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title for the section
        Text(
            text = "Populære aktiviteter",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of activities
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities) { activity ->
                ActivityItem(activity = activity) {
                    // Naviger til detaljskjermen med aktivitetens informasjon
                    navController.navigate("detail")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}
