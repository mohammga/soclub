package com.example.soclub.ui.screens.editPermission

import android.inputmethodservice.Keyboard.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

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
            text = "Plassering",  // Title above the switch
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
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = cameraPermission.value,
                onCheckedChange = {cameraPermission.value=it}
            )
        }
        Text(
            text = "Kamera",  // Title above the switch
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
                text = "Tillat tilgang til kamera",
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = locationPermission.value,
                onCheckedChange = {locationPermission.value=it}
            )
        }
        Text(
            text = "Varsling",  // Title above the switch
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
                text = "Tillat tilgang til varsling",
                style = MaterialTheme.typography.labelLarge
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
