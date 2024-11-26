package com.example.soclub.components.navigation.navBars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    title: String,
) {
    TopAppBar(title = {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
        )
    })
}

