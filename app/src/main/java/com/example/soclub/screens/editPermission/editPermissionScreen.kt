package com.example.soclub.screens.editPermission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun EditPermissionScreen(navController: NavController) {
    val locationPermission = remember { mutableStateOf(true) }
    val cameraPermission = remember { mutableStateOf(true) }
    val notificationPermission = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        PermissionTitle(title = "Plassering")
        PermissionSwitchRow(
            permissionText = "Tillat tilgang til plassering",
            permissionState = locationPermission.value,
            onPermissionChange = { locationPermission.value = it }
        )

        PermissionTitle(title = "Kamera")
        PermissionSwitchRow(
            permissionText = "Tillat tilgang til kamera og galleri",
            permissionState = cameraPermission.value,
            onPermissionChange = { cameraPermission.value = it }
        )

        PermissionTitle(title = "Varsler")
        PermissionSwitchRow(
            permissionText = "Tillat tilgang til varsler",
            permissionState = notificationPermission.value,
            onPermissionChange = { notificationPermission.value = it }
        )
    }
}

@Composable
fun PermissionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(vertical = 5.dp)
    )
}

@Composable
fun PermissionSwitchRow(
    permissionText: String,
    permissionState: Boolean,
    onPermissionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = permissionText,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Switch(
            checked = permissionState,
            onCheckedChange = onPermissionChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditPermissionScreenPreview() {
    EditPermissionScreen(rememberNavController())
}
