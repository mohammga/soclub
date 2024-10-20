package com.example.soclub.screens.editPermission


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.soclub.R
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle


@Composable
fun EditPermissionScreen(
    navController: NavController,
    viewModel: EditPermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationPermission by viewModel.locationPermission.collectAsState()
    val cameraPermission by viewModel.cameraPermission.collectAsState()
    val notificationPermission by viewModel.notificationPermission.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    // Bruk DisposableEffect for å oppdatere tillatelser når brukeren kommer tilbake til skjermen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Oppdater tillatelser hver gang appen går tilbake til app
                viewModel.checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        // Opprinnelig sjekk av tillatelser når skjermen vises første gang
        viewModel.checkPermissions(context)
    }

    // Resten av UI-koden
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        // Plasseringstillatelse Switch
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

        // Kamera tillatelse Switch
        Text(
            text = stringResource(id = R.string.change_Camera_screen_title),
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
                text = stringResource(id = R.string.change_Camera_promission_screen),
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = cameraPermission,
                onCheckedChange = {
                    viewModel.navigateToSettings(context)
                }
            )
        }

        // Varseltillatelse Switch
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
