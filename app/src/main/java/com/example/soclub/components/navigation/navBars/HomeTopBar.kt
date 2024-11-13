package com.example.soclub.components.navigation.navBars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

//import androidx.compose.ui.tooling.preview.Preview
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
 //import com.example.soclub.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    title: String,
) {
    TopAppBar(title = {
        Text(
            text = title,  // Use the title parameter
            textAlign = TextAlign.Start,  // Align title to the start (left)
            fontWeight = FontWeight.Bold,
        )
    })
}

