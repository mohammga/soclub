import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soclub.screens.editActivity.EditActivityViewModel

@Composable
fun EditActivityScreen(
    navController: NavController,
    viewModel: EditActivityViewModel = hiltViewModel(),
    category: String,
    activityId: String
) {
    // Debugging: Log category and activityId
    LaunchedEffect(Unit) {
        Log.d("EditActivityScreen", "Category: $category, ActivityId: $activityId")
        println("Category: $category, ActivityId: $activityId")  // Alternativt print
    }

    // State to store activity details
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var ageGroup by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    // Observe activity data from ViewModel using observeAsState()
    val activity = viewModel.getActivity(category, activityId).observeAsState()

    // Populate the input fields when the activity data is fetched
    LaunchedEffect(activity.value) {
        activity.value?.let {
            title = it.title ?: ""
            description = it.description ?: ""
            maxParticipants = it.maxParticipants.toString()
            ageGroup = it.ageGroup.toString()
            location = it.location ?: ""
            date = it.date ?: ""
            time = it.time ?: ""
        }
    }

    // Build the UI with TextFields populated with activity data
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Tittel") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Beskrivelse") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                placeholder = { Text("Maks antall deltakere") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = ageGroup,
                onValueChange = { ageGroup = it },
                placeholder = { Text("Aldersgruppe") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Sted") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = date,
                onValueChange = { date = it },
                placeholder = { Text("Dato") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = time,
                onValueChange = { time = it },
                placeholder = { Text("Tidspunkt") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Navigate back if the update is successful
    if (viewModel.updateSuccess.collectAsState().value == true) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}
