package com.example.soclub.screens.start

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R

@Composable
fun StartScreen(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        WelcomeMessage()

        Spacer(modifier = Modifier.height(16.dp))

        ContinueWithEmailButton(navController)
    }
}

@Composable
fun WelcomeMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.welcome_message),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.terms_and_conditions),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
    }
}

@Composable
fun ContinueWithEmailButton(navController: NavController) {
    Button(
        onClick = {
            navController.navigate("signup")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.continue_with_email_button))
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(rememberNavController())
}
