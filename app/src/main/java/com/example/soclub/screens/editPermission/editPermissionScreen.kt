package com.example.soclub.screens.editPermission

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soclub.R
import kotlinx.coroutines.delay

@Composable
fun EditPermissionScreen(
    navController: NavController,
    viewModel: EditPermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Collect permission states from ViewModel
    val locationPermission by viewModel.locationPermission.collectAsState()
    val cameraPermission by viewModel.cameraPermission.collectAsState()
    val notificationPermission by viewModel.notificationPermission.collectAsState()

    // Update permissions when the screen becomes visible again
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
}