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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Main composable function for the Home Screen.
 * Displays activity categories, a filter option, and activities grouped by category.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param viewModel The ViewModel handling state for the Home Screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val categories by viewModel.getCategories().observeAsState(emptyList())
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingArea by remember { mutableStateOf(true) }
    val selectedCities by viewModel.selectedCities.observeAsState(emptyList())
    val cities by viewModel.getCities().observeAsState(emptyList())
    val hasLocationPermission by viewModel.hasLocationPermission.observeAsState(false)
    val visibleCategories = if (hasLocationPermission) {
        categories
    } else {
        categories.filter { it != "Nærme Aktiviteter" }
    }

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

        CategoryActivitiesPager(
            categories = visibleCategories,
            pagerState = pagerState,
            viewModel = viewModel,
            navController = navController,
            hasLocationPermission = hasLocationPermission
        )
    }

    if (showBottomSheet) {
        FilterBottomSheet(
            showBottomSheet = true,
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


/**
 * Displays a row of tabs for navigating between categories.
 *
 * @param categories List of category names.
 * @param pagerState The pager state for handling the current selected tab.
 */
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

/**
 * Displays a horizontal pager for activities grouped by category.
 *
 * @param categories List of category names to display.
 * @param pagerState The state of the pager, handling current page and scrolling.
 * @param viewModel The ViewModel for retrieving activity data.
 * @param navController Navigation controller for navigating to the detail screen.
 * @param hasLocationPermission Boolean indicating whether the app has location permissions.
 */
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

        val isNearestActivitiesSelected = selectedCategory == "Nærme Aktiviteter" && hasLocationPermission

        LaunchedEffect(isNearestActivitiesSelected) {
            if (isNearestActivitiesSelected) {
                viewModel.getNearestActivities()
            }
        }

        val activitiesToShow = if (isNearestActivitiesSelected) {
            activities
        } else {
            groupedActivities[selectedCategory] ?: emptyList()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (activitiesToShow.isNotEmpty()) {
                if (isNearestActivitiesSelected) {
                    ActivityList(
                        activities = activitiesToShow,
                        selectedCategory = selectedCategory,
                        navController = navController,
                        useStaggeredGrid = true
                    )
                } else {
                    ActivityList(
                        activities = activitiesToShow,
                        selectedCategory = selectedCategory,
                        navController = navController,
                        useStaggeredGrid = false
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.no_activities_available, selectedCategory),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


/**
 * Displays a list of activities for a selected category.
 *
 * @param activities List of activities to display.
 * @param selectedCategory The currently selected category.
 * @param navController Navigation controller for navigating to the activity details screen.
 */

@Composable
fun ActivityList(
    activities: List<Activity>,
    selectedCategory: String,
    navController: NavHostController,
    useStaggeredGrid: Boolean
) {
    if (useStaggeredGrid) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(activities) { index, activity ->
                val height = calculateHeightBasedOnIndex(index)
                ActivityItemWithDynamicHeight(
                    activity = activity,
                    height = height
                ) {
                    navController.navigate("detail/${activity.category}/${activity.id}")
                }
            }

            if (activities.size % 2 != 0) {
                item {
                    Spacer(
                        modifier = Modifier
                            .height(0.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    } else {
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
}


/**
 * Dynamisk høyde basert på en fast sekvens: liten, stor - stor, liten - osv.
 */
fun calculateHeightBasedOnIndex(index: Int): Dp {
    return when (index % 4) {
        0 -> 200.dp
        1 -> 350.dp
        2 -> 350.dp
        3 -> 200.dp
        else -> 200.dp
    }
}

/**
 * Viser aktivitet med dynamisk høyde.
 */
@Composable
fun ActivityItemWithDynamicHeight(activity: Activity, height: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
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
            modifier = Modifier.fillMaxSize()
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

            val cityName = getCityNameFromAddress(activity.location)

            Text(
                text = cityName,
                fontSize = 12.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activity.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


/**
 * Displays an individual activity item with title, location, and date.
 *
 * @param activity The activity object containing details.
 * @param onClick Callback function for when the activity is clicked.
 */
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

            val cityName = getCityNameFromAddress(activity.location)

            Text(
                text = cityName,
                fontSize = 12.sp,
                color = Color.White

            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activity.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Ekstraherer bynavn fra adresse.
 */
fun getCityNameFromAddress(address: String): String {
    return address.split(",", " ").lastOrNull { it.isNotBlank() } ?: ""
}

/**
 * Displays a chip with a removable option.
 *
 * @param text The text to display inside the chip.
 * @param onRemove Callback function for removing the chip.
 */
@Composable
fun Chip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
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



/**
 * Displays a city selection item with a checkbox for selecting or deselecting.
 *
 * @param city The name of the city.
 * @param isSelected Boolean indicating whether the city is selected.
 * @param onCitySelected Callback function invoked when the selection state changes.
 */
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

/**
 * Displays a filter list item with a clickable row.
 *
 * @param title The title of the filter item.
 * @param onClick Callback function invoked when the item is clicked.
 * @param backgroundColor The background color of the item.
 * @param contentColor The text and icon color of the item.
 */
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
            imageVector = Icons.Default.FilterList,
            contentDescription = "Arrow",
            tint = contentColor,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

/**
 * Displays a bottom sheet for filtering activities by area.
 *
 * @param showBottomSheet Boolean indicating whether the bottom sheet is visible.
 * @param selectedCities List of currently selected cities.
 * @param cities List of available cities.
 * @param onDismissRequest Callback function for dismissing the bottom sheet.
 * @param onCitySelected Callback function for selecting or deselecting a city.
 * @param onSearch Callback function for executing the search.
 * @param onResetFilter Callback function for resetting the filter.
 */
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
                Text(stringResource(R.string.filter_by_area), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    Text(stringResource(R.string.Show_hits))
                }

                if (selectedCities.isNotEmpty()) {
                    Button(
                        onClick = { onResetFilter() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.Reset))
                    }
                }
            }
        }
    }
}

/**
 * Determines if the device is in landscape orientation.
 *
 * @return True if the orientation is landscape, false otherwise.
 */
@Composable
fun isLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}


