import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.screens.ads.AdsViewModel


@Composable
fun AdsScreen(
    navController: NavController,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchActivitiesByCreator()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                // Viser loading-indikator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                // Viser feilmelding hvis det ikke finnes annonser eller hvis en feil oppstår
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            activities.isEmpty() -> {
                // Hvis det ikke finnes annonser og ingen feil, vis en melding
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Ingen annonser funnet.", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            else -> {
                // Viser listen over annonser
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activities.size) { index ->
                        val activity = activities[index]
                        EntryItem(
                            imageUrl = activity.imageUrl,
                            title = activity.title,
                            time = activity.startTime,
                            activityId = activity.creatorId,
                            category = activity.category,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EntryItem(
    imageUrl: String?,
    title: String?,
    time: String?,
    activityId: String,
    category: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("editActivity/$category/$activityId")
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EventImage(imageUrl ?: "")

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title ?: "Ingen tittel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = time ?: "Ukjent tid",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Legger til "Endre annonse"-knappen
        Button(
            onClick = {
                navController.navigate("editActivity/$category/$activityId")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Endre annonse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(thickness = 1.dp)
    }
}



@Composable
fun EventImage(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = null,
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}


