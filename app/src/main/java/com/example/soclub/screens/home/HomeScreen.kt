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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.soclub.R

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val categories by viewModel.getCategories().observeAsState(emptyList())
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { categories.size })

    var showBottomSheet by remember { mutableStateOf(false) }
    var isSelectingArea by remember { mutableStateOf(true) }
    val selectedCities = remember { mutableStateListOf<String>() }
    val cities by viewModel.getCities().observeAsState(emptyList())
    val filteredActivities by viewModel.filteredActivities.observeAsState(emptyList()) // Bruk filtrerte aktiviteter

    // GPS-relaterte variabler
    val location by viewModel.getCurrentLocation().observeAsState(null)
    var userCity by remember { mutableStateOf<String?>(null) }

    // Når vi har GPS-posisjonen, bruk geokoding for å hente byen
    LaunchedEffect(location) {
        location?.let {
            userCity = viewModel.getCityFromLocation(it)
        }
    }

    // Når brukerens by er hentet, oppdater aktivitetene for "Forslag" kategorien
    val activities by viewModel.getActivities("Forslag").observeAsState(emptyList())

    LaunchedEffect(pagerState.currentPage) {
        val selectedCategory = categories.getOrElse(pagerState.currentPage) { "" }
        if (selectedCategory == "Forslag") {
            // Nullstill aktivitetene før vi henter nye for "Forslag"
            viewModel.resetActivities()  // Implementer denne i viewModel for å tømme aktiviteter
            viewModel.getActivities("Forslag")
        } else {
            viewModel.fetchAndFilterActivitiesByCities(selectedCities, selectedCategory)
        }
    }



    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            CategoryTabs(categories = categories, pagerState = pagerState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedCategory = categories.getOrElse(pagerState.currentPage) { "" }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryTitle(selectedCategory)

            // Vis filterikonet kun hvis kategorien ikke er "Forslag"
            if (selectedCategory != "Forslag") {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            showBottomSheet = true
                            isSelectingArea = true
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hvis vi har filtrerte aktiviteter, vis disse, ellers vis aktiviteter for den valgte kategorien
        if (filteredActivities.isNotEmpty()) {
            ActivityList(activities = filteredActivities, selectedCategory = selectedCategory, navController = navController)
        } else if (selectedCities.isNotEmpty() && filteredActivities.isEmpty()) {
            Text(
                text = "Det er ingen aktiviteter i ${selectedCities.joinToString(", ")} for denne kategorien.",
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            // Hvis "Forslag" er valgt, vis aktiviteter basert på brukerens by
            if (selectedCategory == "Forslag" && activities.isNotEmpty()) {
                ActivityList(activities = activities, selectedCategory = selectedCategory, navController = navController)
            } else {
                CategoryActivitiesPager(
                    categories = categories,
                    pagerState = pagerState,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            if (isSelectingArea) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Filtrer etter", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    FilterListItem(
                        title = "Område",
                        onClick = { isSelectingArea = false },
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSelectingArea = true }
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
                            .height(300.dp)  // Begrens høyden
                    ) {
                        items(cities) { city ->
                            CitySelectionItem(
                                city = city,
                                isSelected = selectedCities.contains(city), // Behold valgte byer
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Filtrer aktiviteter basert på valgte byer kun for den valgte kategorien
                            val selectedCategory = categories.getOrElse(pagerState.currentPage) { "" }
                            viewModel.fetchAndFilterActivitiesByCities(selectedCities, selectedCategory)
                            showBottomSheet = false // Skjul BottomSheet
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Text(text = "Søk")
                    }

                    if (filteredActivities.isNotEmpty()) {
                        Button(
                            onClick = {
                                val selectedCategory = categories.getOrElse(pagerState.currentPage) { "" }
                                selectedCities.clear() // Tøm listen over valgte byer
                                viewModel.resetFilter() // Nullstill filtreringen
                                viewModel.fetchAndFilterActivitiesByCities(emptyList(), selectedCategory) // Gjenopprett aktivitetene for valgt kategori
                                showBottomSheet = false // Skjul BottomSheet
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
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
    }
}
