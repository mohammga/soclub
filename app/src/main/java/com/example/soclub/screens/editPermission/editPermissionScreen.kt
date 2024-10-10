package com.example.soclub.screens.editPermission

<<<<<<< Updated upstream
import android.content.Context
=======


>>>>>>> Stashed changes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
<<<<<<< Updated upstream
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soclub.R
import kotlinx.coroutines.delay
=======
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.soclub.R

>>>>>>> Stashed changes

@Composable
fun EditPermissionScreen(
    navController: NavController,
<<<<<<< Updated upstream
    viewModel: EditPermissionViewModel = hiltViewModel()
=======
    viewModel: EditPermissionViewModel = viewModel()
>>>>>>> Stashed changes
) {
    val context = LocalContext.current
    val locationPermission by viewModel.locationPermission.collectAsState()
    val cameraPermission by viewModel.cameraPermission.collectAsState()
    val notificationPermission by viewModel.notificationPermission.collectAsState()

<<<<<<< Updated upstream
    // Collect permission states from ViewModel
    val locationPermission by viewModel.locationPermission.collectAsState()
    val cameraPermission by viewModel.cameraPermission.collectAsState()
    val notificationPermission by viewModel.notificationPermission.collectAsState()

    // Update permissions when the screen becomes visible again
=======

>>>>>>> Stashed changes
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        // UI for plasseringstillatelse med Switch
        Text(
            text = stringResource(R.string.change_location_screen_title),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 5.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.change_location_Promission_screen),
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = locationPermission,
                onCheckedChange = {
                    viewModel.navigateToSettings(context)
                }
            )
        }

        // UI for kameratillatelse med Switch
        Text(
            text = stringResource(R.string.change_Camera_screen_title),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 5.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.change_Camera_promission_screen),
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = cameraPermission,
                onCheckedChange = {
                    viewModel.navigateToSettings(context)
                }
            )
        }

        // UI for varsel tillatelse med Switch
        Text(
            text = stringResource(R.string.change_notificationPermission_screen_title),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 5.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.change_notificationPermission_Promission_screen),
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = notificationPermission,
                onCheckedChange = {
                    viewModel.navigateToSettings(context)
                }
            )
        }
    }
<<<<<<< Updated upstream
}
=======
}


>>>>>>> Stashed changes
