package com.example.soclub.components.navigation.navBars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    title: String,  // New parameter to accept a title
    showBackButton: Boolean = true,  // Parameter to show or hide the back button
    route: String? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,  // Use the title parameter
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = {
                    if (route != null) {
                        navController.navigate(route)
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar(rememberNavController(), title = "Preview Title", showBackButton = true)  // Preview with back button
}

@Preview
@Composable
fun TopBarTextOnlyPreview() {
    TopBar(rememberNavController(), title = "Preview Title", showBackButton = false)  // Preview without back button
}
