package com.example.soclub.screens.editPermission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val cameraPermission = remember { mutableStateOf(true)}
    val notificationPermission = remember { mutableStateOf(true)}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
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


        ){
            Text(
                text = "Tillat tilgang til plassering",
                style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
            )
            Switch(
                checked = cameraPermission.value,
                onCheckedChange = {cameraPermission.value=it}
            )
        }
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

        ){
            Text(
                text = "Tillat tilgang til kamera og galleri",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Switch(
                checked = locationPermission.value,
                onCheckedChange = {locationPermission.value=it}
            )
        }
        Text(
            text = "Varsler",
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

        ){
            Text(
                text = "Tillat tilgang til varsler",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Switch(
                checked = notificationPermission.value,
                onCheckedChange = {notificationPermission.value=it}
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun EditPermissionScreenPreview() {
    EditPermissionScreen(rememberNavController())
}
