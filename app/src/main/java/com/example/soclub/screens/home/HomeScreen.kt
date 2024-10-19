package com.example.soclub.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.pager.HorizontalPager
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val categories by viewModel.getCategories().observeAsState(emptyList())
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { categories.size })

    // BottomSheet state (using a simple Boolean)
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingArea by remember { mutableStateOf(true) } // To switch between area and city views
    val coroutineScope = rememberCoroutineScope()

    // Hold the selected cities
    val selectedCities = remember { mutableStateListOf<String>() }

    // Hent byer fra ViewModel ved hjelp av getCities()
    val cities by viewModel.getCities().observeAsState(emptyList())

    // Bottom sheet content
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            if (isSelectingArea) {
                // Områdevalg (Area Selection) - Stilet liste med svart bakgrunn
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Filtrer etter", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stilet listeelement med svart bakgrunn og hvit tekst for "Område"
                    FilterListItem(
                        title = "Område",
                        onClick = { isSelectingArea = false },
                        backgroundColor = MaterialTheme.colorScheme.primary, // Svart bakgrunn
                        contentColor = MaterialTheme.colorScheme.onPrimary // Hvit tekst og pil
                    )
                }
            } else {
                // Byvalg (City Selection) - Dynamisk liste over byer med checkbox og søk-knapp
                Column(modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                ) {
                    // Tilbake pil øverst for å navigere tilbake til områdevalg
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSelectingArea = true } // Tilbake til områdevalg
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tilbake",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = "Tilbake",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Vis valgte byer under tilbake-navigasjonen
                    if (selectedCities.isNotEmpty()) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            selectedCities.forEach { city ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = city, fontSize = 16.sp)
                                    IconButton(onClick = {
                                        selectedCities.remove(city) // Fjern byen fra listen
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Fjern $city"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Velg By", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamisk liste over byer
                    LazyColumn {
                        items(cities) { city ->
                            CitySelectionItem(
                                city = city,
                                isSelected = selectedCities.contains(city),
                                onCitySelected = { isSelected ->
                                    if (isSelected) {
                                        selectedCities.add(city)
                                    } else {
                                        selectedCities.remove(city)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Pusher innholdet oppover for å plassere knappen nederst

                    // Søk-knapp
                    Button(
                        onClick = {
                            // Handle search action here, for now we just close the sheet
                            showBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(text = "Søk")
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        if (categories.isNotEmpty()) {
            CategoryTabs(categories = categories, pagerState = pagerState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedCategory = categories.getOrElse(pagerState.currentPage) { "" }

        // Modified CategoryTitle to include a filter icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryTitle(selectedCategory)

            // Filter icon
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier
                    .clickable {
                        showBottomSheet = true
                        isSelectingArea = true // Start with area selection
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CategoryActivitiesPager(
            categories = categories,
            pagerState = pagerState,
            viewModel = viewModel,
            navController = navController
        )
    }
}

@Composable
fun CitySelectionItem(city: String, isSelected: Boolean, onCitySelected: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCitySelected(!isSelected) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = city,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onCitySelected(it) },
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun FilterListItem(
    title: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clip(RoundedCornerShape(16.dp)) // Legger til border radius
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(start = 16.dp)
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Arrow",
            tint = contentColor,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun CategoryTabs(categories: List<String>, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        edgePadding = 2.dp
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                text = { Text(category) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryTitle(category: String) {
    Text(
        text = category,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(start = 16.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryActivitiesPager(
    categories: List<String>,
    pagerState: PagerState,
    viewModel: HomeViewModel,
    navController: NavHostController
) {
    HorizontalPager(state = pagerState) { page ->
        val selectedCategory = categories[page]
        val activities by viewModel.getActivities(selectedCategory).observeAsState(emptyList())

        ActivityList(activities = activities, selectedCategory = selectedCategory, navController = navController)
    }
}

@Composable
fun ActivityList(activities: List<Activity>, selectedCategory: String, navController: NavHostController) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(activities) { activity ->
            ActivityItem(activity = activity) {
                navController.navigate("detail/${selectedCategory}/${activity.id}")
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
            painter = rememberAsyncImagePainter(activity.imageUrl),
            contentDescription = activity.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = activity.title ?: "Ingen tittel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}