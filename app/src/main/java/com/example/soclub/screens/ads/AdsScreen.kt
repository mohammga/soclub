import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    val activities = viewModel.activities.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.fetchActivitiesByCreator()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities.value.size) { index ->
                val activity = activities.value[index]
                EntryItem(
                    imageUrl = activity.imageUrl,
                    title = activity.title,
                    time = activity.time,
                    activityId = activity.creatorId, // activityId her representerer creatorId fra backend
                    category = activity.category, // Pass in the correct category for this activity
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun EntryItem(
    imageUrl: String?,
    title: String?,
    time: String?,
    activityId: String,  // Pass activityId for navigation
    category: String,  // Pass category for navigation
    navController: NavController
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

            Spacer(modifier = Modifier.height(8.dp))

            EditButton {
                navController.navigate("editActivity/$category/$activityId")
            }
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp)
        }
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

@Composable
fun EditButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        Text(text = "Endre")
    }
}
