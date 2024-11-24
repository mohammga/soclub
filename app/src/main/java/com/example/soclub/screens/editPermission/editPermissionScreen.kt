package com.example.soclub.screens.editPermission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import com.example.soclub.R
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle

/**
 * Composable function that renders the Edit Permission screen.
 *
 * @param viewModel The ViewModel responsible for managing the permission states.
 */
@Composable
fun EditPermissionScreen(
    viewModel: EditPermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationPermission by viewModel.locationPermission.collectAsState()
    val galleryPermission by viewModel.galleryPermission.collectAsState()
    val notificationPermission by viewModel.notificationPermission.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {
            Text(
                text = stringResource(id = R.string.change_location_screen_title),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.change_location_Promission_screen),
                    style = MaterialTheme.typography.labelLarge
                )
                Switch(
                    checked = locationPermission,
                    onCheckedChange = {
                        viewModel.navigateToSettings(context)
                    }
                )
            }
        }

        item {
            Text(
                text = stringResource(id = R.string.change_Gallery_screen_title),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.change_Gallery_promission_screen),
                    style = MaterialTheme.typography.labelLarge
                )
                Switch(
                    checked = galleryPermission,
                    onCheckedChange = {
                        viewModel.navigateToSettings(context)
                    }
                )
            }
        }

        item {
            Text(
                text = stringResource(id = R.string.change_notificationPermission_screen_title),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.change_notificationPermission_Promission_screen),
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
}
