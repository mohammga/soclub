package com.example.soclub.components.navigation.navBars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    navController: NavController,
    title: String,
    showBackButton: Boolean = false,  // For HomeScreen, we don't need a back button
) {
    SmallTopAppBar(
        title = {
            Text(
                text = title,  // Use the title parameter
                textAlign = TextAlign.Start,  // Align title to the start (left)
                fontWeight = FontWeight.Bold,
            )
        },
    )
}

@Preview
@Composable
fun HomeTopBarPreview() {
    HomeTopBar(rememberNavController(), title = "SoClub")  // Preview of HomeTopBar
}
