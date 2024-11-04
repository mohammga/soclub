package com.example.soclub.screens.home


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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.soclub.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val categories by viewModel.getCategories().observeAsState(emptyList())
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { categories.size })
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingArea by remember { mutableStateOf(true) }
    val selectedCities by viewModel.selectedCities.observeAsState(mutableListOf())
    val cities by viewModel.getCities().observeAsState(emptyList())
    val userCity by viewModel.userCity.observeAsState(null)

    LaunchedEffect(Unit) {
        viewModel.fetchUserLocation()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            CategoryTabs(categories = categories, pagerState = pagerState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedCategory = categories.getOrNull(pagerState.currentPage) ?: ""

        // Tittel og filter-ikon på samme linje
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp), // Fjern venstre padding her
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
                    modifier = Modifier
                        .clickable {
                            showBottomSheet = true
                            isSelectingArea = true
                        }
                )
            }
        }

        // Filterchips under tittelen med FlowRow for linjeskift
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

        if (selectedCategory == "Nærme Aktiviteter") {
            userCity?.let { city ->
                LaunchedEffect(city) {
                    viewModel.getNearestActivities()
                }
                NearActivities(viewModel = viewModel, navController = navController)
            } ?: run {
                Text(
                    text = "Henter nærmeste aktiviteter...",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            CategoryActivitiesPager(
                categories = categories,
                pagerState = pagerState,
                viewModel = viewModel,
                navController = navController
            )
        }
    }

    // BottomSheet for filtering
    if (showBottomSheet) {
        FilterBottomSheet(
            showBottomSheet = showBottomSheet,
            isSelectingArea = isSelectingArea,
            selectedCities = selectedCities,
            cities = cities,
            onDismissRequest = { showBottomSheet = false },
            onSelectArea = { isSelectingArea = it },
            onCitySelected = { city, isSelected -> viewModel.updateSelectedCities(city, isSelected) },
            onSearch = {
                viewModel.fetchAndGroupActivitiesByCities(selectedCities)
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
            .clip(RoundedCornerShape(12.dp))  // Mindre runding
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),  // Reduser padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,  // Mindre fontstørrelse
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove $text",
            modifier = Modifier
                .size(16.dp)  // Mindre ikonstørrelse
                .clickable { onRemove() }
        )
    }
}





@Composable
fun CategoryActivitiesPager(
    categories: List<String>,
    pagerState: PagerState,
    viewModel: HomeViewModel,
    navController: NavHostController
) {
    val groupedActivities by viewModel.groupedActivities.observeAsState(emptyMap())
    val isLoading by viewModel.isLoading.observeAsState(false)

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true
    ) { page ->
        val selectedCategory = categories[page]
        val activities = groupedActivities[selectedCategory] ?: emptyList()

        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator() // Viser loading-indikator til alt er klart
                }
            } else {
                if (activities.isNotEmpty()) {
                    ActivityList(
                        activities = activities,
                        selectedCategory = selectedCategory,
                        navController = navController
                    )
                } else {
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
            edgePadding = 2.dp,
            modifier = Modifier.fillMaxWidth()
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
        modifier = Modifier.padding(start = 16.dp)
    )
}

@Composable
fun ActivityList(activities: List<Activity>, selectedCategory: String, navController: NavHostController) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(activities) { activity ->
            // Hvis vi er på "Forslag", bruk aktiviteten sin kategori til å navigere
            val categoryToUse = if (selectedCategory == "Nærme Aktiviteter") {
                activity.category ?: "ukjent"  // Sørg for at aktiviteten har kategori
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Check if the imageUrl is null or empty, then show a placeholder
        if (activity.imageUrl.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = stringResource(id = R.string.change_ad_picture),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(activity.imageUrl),
                contentDescription = activity.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title for the activity
        Text(
            text = activity.title ?: "Ingen tittel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )

        // Show the city/location under the title
        Text(
            text = activity.location ?: "Ukjent sted",
            fontSize = 14.sp,
            color = Color.Gray, // Gi byen en lysere farge
            modifier = Modifier.align(Alignment.Start)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    showBottomSheet: Boolean,
    isSelectingArea: Boolean,
    selectedCities: List<String>,
    cities: List<String>,
    onDismissRequest: () -> Unit,
    onSelectArea: (Boolean) -> Unit,
    onCitySelected: (String, Boolean) -> Unit,
    onSearch: () -> Unit,
    onResetFilter: () -> Unit
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismissRequest() }
        ) {
            if (isSelectingArea) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Filtrer etter", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    FilterListItem(
                        title = "Område",
                        onClick = { onSelectArea(false) },
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectArea(true) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tilbake",
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(text = "Tilbake", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Velg By", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onSearch() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(text = "Søk")
                    }

                    if (selectedCities.isNotEmpty()) {
                        Button(
                            onClick = { onResetFilter() },  // Fjern tilbakestilling av `_selectedCities` her
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("Nullstill filtrering")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NearActivities(viewModel: HomeViewModel, navController: NavHostController) {
    val activities by viewModel.activities.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Henter de 10 nærmeste aktivitetene...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                CircularProgressIndicator()
            }
        }
    } else {
        if (activities.isNotEmpty()) {
            ActivityList(
                activities = activities,
                selectedCategory = "Nærme Aktiviteter",
                navController = navController
            )
        } else {
            Text(
                text = "Ingen aktiviteter tilgjengelig for Nærme Aktiviteter.",
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



