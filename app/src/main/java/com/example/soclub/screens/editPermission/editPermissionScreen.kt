package com.example.soclub.screens.editPermission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

@Composable
fun EditPermissionScreen(
    navController: NavController,
    viewModel: EditPermissionViewModel = viewModel(factory = EditPermissionViewModelFactory(LocalContext.current))
) {
    // Hente tillatelsestilstandene fra ViewModel ved hjelp av StateFlow
    val locationPermission by viewModel.locationPermission.collectAsState()
    val cameraPermission by viewModel.cameraPermission.collectAsState()
    val notificationPermission by viewModel.notificationPermission.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        // UI for plasseringstillatelse
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
                onCheckedChange = { viewModel.setLocationPermission(it) }  // Oppdatere ViewModel
            )
        }

        // UI for kamera
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
                onCheckedChange = { viewModel.setCameraPermission(it) }  // Oppdatere ViewModel
            )
        }

        // UI for varslinger
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
                onCheckedChange = { viewModel.setNotificationPermission(it) }  // Oppdatere ViewModel
            )
        }

        // Lagre-knapp for å lagre endringer
        Button(
            onClick = {
                navController.popBackStack()  // Gå tilbake etter lagring
            },
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Lagre")
        }

        // Tilbakestill-knapp for å sette alle tillatelser tilbake til standard
        Button(
            onClick = {
                viewModel.setLocationPermission(true)
                viewModel.setCameraPermission(true)
                viewModel.setNotificationPermission(true)
            },
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Tilbakestill til standard")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPermissionScreenPreview() {
    EditPermissionScreen(rememberNavController())
}
