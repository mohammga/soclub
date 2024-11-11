package com.example.soclub.screens.start

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R

@Composable
fun StartScreen(navController: NavController) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            WelcomeMessage(navController)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ContinueWithEmailButton(navController)
            Spacer(modifier = Modifier.height(16.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(16.dp))
            SignInButton(navController)
        }
    }
}

@Composable
fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.or_text), // legg til denne strengen i resources
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SignInButton(navController: NavController) {
    Button(
        onClick = {
            navController.navigate("signin")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.sign_in_button))
    }
}


@Composable
fun WelcomeMessage(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.welcome_message),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(id = R.string.terms_and_conditions))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable {
                navController.navigate("terms_privacy")
            }
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