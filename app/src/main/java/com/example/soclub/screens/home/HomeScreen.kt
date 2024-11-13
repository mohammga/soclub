// File: com/example/soclub/screens/home/HomeScreen.kt
package com.example.soclub.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.soclub.models.Activity
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val categories by viewModel.getCategories().observeAsState(emptyList())
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingArea by remember { mutableStateOf(true) }
    val selectedCities by viewModel.selectedCities.observeAsState(emptyList())
    val cities by viewModel.getCities().observeAsState(emptyList())
    val hasLocationPermission by viewModel.hasLocationPermission.observeAsState(false)

    // Filter categories based on location permission
    val visibleCategories = if (hasLocationPermission) {
        categories
    } else {
        categories.filter { it != "Nærme Aktiviteter" }
    }

    // Initialize pagerState with visibleCategories.size
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { visibleCategories.size }
    )

    LaunchedEffect(Unit) {
        viewModel.fetchUserLocation()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (visibleCategories.isNotEmpty()) {
            CategoryTabs(categories = visibleCategories, pagerState = pagerState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Select the current category based on the current page in the pager
        val selectedCategory = visibleCategories.getOrNull(pagerState.currentPage) ?: ""

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedCategory,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            if (selectedCategory != "Nærme Aktiviteter") {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier.clickable {
                        showBottomSheet = true
                        isSelectingArea = true
                    }
                )
            }
        }

        if (selectedCategory != "Nærme Aktiviteter" && selectedCities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedCities.forEach { city ->
                    Chip(
                        text = city,
                        onRemove = { viewModel.updateSelectedCities(city, false) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display activities based on the selected category
        CategoryActivitiesPager(
            categories = visibleCategories,
            pagerState = pagerState,
            viewModel = viewModel,
            navController = navController,
            hasLocationPermission = hasLocationPermission
        )
    }

    // Filter bottom sheet for city selection and area settings
    if (showBottomSheet) {
        FilterBottomSheet(
            showBottomSheet = showBottomSheet,
            selectedCities = selectedCities,
            cities = cities,
            onDismissRequest = { showBottomSheet = false },
            onCitySelected = { city, isSelected -> viewModel.updateSelectedCities(city, isSelected) },
            onSearch = {
                showBottomSheet = false
            },
            onResetFilter = {
                viewModel.resetFilter()
                showBottomSheet = false
            }
        )
    }
}

@Composable
fun Chip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))  // Smaller rounding
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),  // Reduced padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,  // Smaller font size
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove $text",
            modifier = Modifier
                .size(16.dp)
                .clickable { onRemove() }
        )
    }
}

@Composable
fun CategoryActivitiesPager(
    categories: List<String>,
    pagerState: PagerState,
    viewModel: HomeViewModel,
    navController: NavHostController,
    hasLocationPermission: Boolean
) {
    val groupedActivities by viewModel.groupedActivities.observeAsState(emptyMap())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val activities by viewModel.activities.observeAsState(emptyList())

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true
    ) { page ->
        val selectedCategory = categories[page]

        // Determine if "Nærme Aktiviteter" is selected and if location permission is granted
        val isNearestActivitiesSelected = selectedCategory == "Nærme Aktiviteter" && hasLocationPermission

        // Fetch nearest activities when GPS is enabled and "Nærme Aktiviteter" is selected
        LaunchedEffect(isNearestActivitiesSelected) {
            if (isNearestActivitiesSelected) {
                viewModel.getNearestActivities()
            }
        }

        // Choose activities to display based on selected category and GPS access
        val activitiesToShow = if (isNearestActivitiesSelected) {
            activities // Use nearest activities if GPS access is granted
        } else {
            groupedActivities[selectedCategory] ?: emptyList() // Otherwise, use grouped activities
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                // Show a loading indicator while data is loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (activitiesToShow.isNotEmpty()) {
                    // Display the list of activities
                    ActivityList(
                        activities = activitiesToShow,
                        selectedCategory = selectedCategory,
                        navController = navController
                    )
                } else {
                    // Display a message if no activities are available
                    Text(
                        text = "Ingen aktiviteter tilgjengelig for $selectedCategory.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onCitySelected(it) },
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
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
        Icon(
            imageVector = Icons.Default.FilterList, // You can change to the appropriate icon
            contentDescription = "Arrow",
            tint = contentColor,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun CategoryTabs(categories: List<String>, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    val isLandscape = isLandscape()

    if (isLandscape) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                )
            },
            divider = {}
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    text = { Text(category) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
    } else {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                )
            },
            divider = {}
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    text = { Text(category) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
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
            val categoryToUse = if (selectedCategory == "Nærme Aktiviteter") {
                activity.category ?: "ukjent"
            } else {
                selectedCategory
            }
            ActivityItem(activity = activity) {
                navController.navigate("detail/${categoryToUse}/${activity.id}")
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = if (activity.imageUrl.isEmpty()) {
                painterResource(id = R.drawable.placeholder)
            } else {
                rememberAsyncImagePainter(activity.imageUrl)
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 100f
                    )
                )
                .clip(RoundedCornerShape(16.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = activity.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activity.location,
                fontSize = 14.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            val activityDateTime = combineDateAndTime(activity.date, activity.startTime)
            val formattedDateTime = activityDateTime?.let {
                val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                formatter.format(it)
            } ?: "Ukjent dato"

            Text(
                text = formattedDateTime,
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

fun combineDateAndTime(date: com.google.firebase.Timestamp?, timeString: String): Date? {
    if (date == null || timeString.isEmpty()) return null

    return try {
        val calendar = Calendar.getInstance()
        calendar.time = date.toDate()

        val timeParts = timeString.split(":")
        if (timeParts.size != 2) return null

        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.time
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    showBottomSheet: Boolean,
    selectedCities: List<String>,
    cities: List<String>,
    onDismissRequest: () -> Unit,
    onCitySelected: (String, Boolean) -> Unit,
    onSearch: () -> Unit,
    onResetFilter: () -> Unit
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismissRequest() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(text = "Filtrer etter område", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(cities) { city ->
                            CitySelectionItem(
                                city = city,
                                isSelected = selectedCities.contains(city),
                                onCitySelected = { isSelected ->
                                    onCitySelected(city, isSelected)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Vis treff")
                }

                if (selectedCities.isNotEmpty()) {
                    Button(
                        onClick = { onResetFilter() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Nullstill")
                    }
                }
            }
        }
    }
}

@Composable
fun isLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}
