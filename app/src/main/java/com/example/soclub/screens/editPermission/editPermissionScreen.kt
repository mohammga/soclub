package com.example.soclub.screens.editPermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun EditPermissionScreen(
    navController: NavController,
    viewModel: EditPermissionViewModel = viewModel(factory = EditPermissionViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current

    // Mutable states to observe permission changes
    var locationPermission by remember { mutableStateOf(checkPermissionStatus(context, Manifest.permission.ACCESS_FINE_LOCATION)) }
    var cameraPermission by remember { mutableStateOf(checkPermissionStatus(context, Manifest.permission.CAMERA)) }
    var notificationPermission by remember { mutableStateOf(checkPermissionStatus(context, Manifest.permission.POST_NOTIFICATIONS)) }

    // Launch effect to keep the permission states updated
    LaunchedEffect(context) {
        while (true) {
            locationPermission = checkPermissionStatus(context, Manifest.permission.ACCESS_FINE_LOCATION)
            cameraPermission = checkPermissionStatus(context, Manifest.permission.CAMERA)
            notificationPermission = checkPermissionStatus(context, Manifest.permission.POST_NOTIFICATIONS)
            delay(1000) // Check permissions every second to keep the UI updated
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        // UI for plasseringstillatelse med Switch
        Text(
            text = "Plassering",
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
                text = "Tillat tilgang til plassering",
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = locationPermission,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        // Brukeren vil aktivere tillatelse blir sendt til innstillinger
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        // Brukeren blir sent til innstillinger for å gjøre dette
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        // UI for kameratillatelse med Switch
        Text(
            text = "Kamera",
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
                text = "Tillat tilgang til kamera",
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = cameraPermission,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        // Brukeren vil aktivere tillatelse, gå til innstillinger
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        // Brukeren blir sent til innstillinger for å gjøre dette
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        // UI for varsel tillatelse med Switch
        Text(
            text = "Varsling",
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
                text = "Tillat tilgang til varsling",
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = notificationPermission,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        // Brukeren vil aktivere tillatelse, gå til innstillinger
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        // Brukeren blir sent til innstillinger for å gjøre dette
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}

fun checkPermissionStatus(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}