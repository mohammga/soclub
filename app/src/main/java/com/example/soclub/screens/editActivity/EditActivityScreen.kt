import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soclub.models.createActivity
import com.example.soclub.screens.editActivity.EditActivityViewModel

@Composable
fun EditActivityScreen(
    navController: NavController,
    viewModel: EditActivityViewModel = hiltViewModel(),
    category: String,
    activityId: String
) {
    LaunchedEffect(Unit) {
        Log.d("EditActivityScreen", "Category: $category, ActivityId: $activityId")
        println("Category: $category, ActivityId: $activityId")
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var ageGroup by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var creatorId by remember { mutableStateOf("") }

    val activity = viewModel.getActivity(category, activityId).observeAsState()

    LaunchedEffect(activity.value) {
        activity.value?.let {
            title = it.title ?: ""
            description = it.description ?: ""
            maxParticipants = it.maxParticipants.toString()
            ageGroup = it.ageGroup.toString()
            location = it.location + " " + it.restOfAddress
            date = it.date ?: ""
            time = it.time ?: ""
        }
    }

    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Beskrivelse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                maxLines = 5
            )

            TextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                placeholder = { Text("Maks antall deltakere") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TextField(
                value = ageGroup,
                onValueChange = { ageGroup = it },
                placeholder = { Text("Aldersgruppe") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Adresse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TextField(
                value = date,
                onValueChange = { date = it },
                placeholder = { Text("Dato") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TextField(
                value = time,
                onValueChange = { time = it },
                placeholder = { Text("Tidspunkt") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    viewModel.updateActivity(
                        category = category,
                        activityId = activityId,
                        updatedActivity = createActivity(
                            title = title,
                            description = description,
                            maxParticipants = maxParticipants.toInt(),
                            ageGroup = ageGroup.toInt(),
                            location = location,
                            date = date,
                            time = time,
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !isUpdating
            ) {
                Text(text = if (isUpdating) "Lagrer..." else "Lagre endringer")
            }
        }
    }

    if (updateSuccess == true) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}
