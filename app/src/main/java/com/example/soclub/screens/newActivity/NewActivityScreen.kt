package com.example.soclub.screens.newActivity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun NewActivityScreen(navController: NavController) {
    // List of fields to display
    val fields = listOf(
        "Tittel",
        "Beskrivelse",
        "Kategori",
        "Sted",
        "Gatenummer/husnummer",
        "Postnummer",
        "Dato",
        "Tidspunkt",
        "Maks antall deltakere",
        "Aldersgrense"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Space between elements
    ) {
        // Adding Title and Description fields at the top
        item {
            TextField(
                value = "", // You can use state to manage input
                onValueChange = {},
                placeholder = { Text("Tittel") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = "", // You can use state to manage input
                onValueChange = {},
                placeholder = { Text("Beskrivelse") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5 // Allow multiple lines
            )

            Text(text = "Last opp bilder", style = MaterialTheme.typography.bodyMedium)
            // Placeholder for image upload - You can create a button or an image picker here
            Button(onClick = { /* Handle image upload */ }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Velg bilder")
            }
        }

        // Adding dynamic fields from the list
        items(fields) { field ->
            TextField(
                value = "", // You can use state to manage input
                onValueChange = {},
                placeholder = { Text(field) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = when (field) {
                        "Postnummer" -> KeyboardType.Number // For post number
                        "Dato", "Tidspunkt" -> KeyboardType.Text // You may want to use DatePicker
                        else -> KeyboardType.Text
                    }
                )
            )
        }

        // Publish button
        item {
            Button(
                onClick = { /* Handle publish */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Publiser")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewActivityScreenPreview() {
    NewActivityScreen(rememberNavController())
}
