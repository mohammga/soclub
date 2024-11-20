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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.soclub.R

/**
 * Composable function representing the Start screen.
 *
 * This screen serves as the entry point for users, providing options to sign in or sign up.
 * It includes a welcome message, buttons to continue with email sign-up, sign in, and a divider with "OR" text.
 *
 * @param navController The [NavController] used for navigating between different screens.
 */
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

/**
 * Composable function for the "OR" divider.
 *
 * Displays a horizontal line with "OR" text centered between two lines,
 * providing a visual separation between different sign-in options.
 */
@Composable
fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.or_text),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

/**
 * Composable function for the Sign-In button.
 *
 * When clicked, navigates the user to the Sign-In screen.
 *
 * @param navController The [NavController] used for navigation.
 */
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

/**
 * Composable function for the Welcome message.
 *
 * Displays a welcoming title and a clickable text for terms and conditions.
 * When the terms and conditions text is clicked, navigates the user to the Terms and Privacy screen.
 *
 * @param navController The [NavController] used for navigation.
 */
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

/**
 * Composable function for the "Continue with Email" button.
 *
 * When clicked, navigates the user to the Sign-Up screen.
 *
 * @param navController The [NavController] used for navigation.
 */
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

/**
 * Composable function for a horizontal divider line.
 *
 * Draws a simple horizontal line to be used within the UI layout.
 *
 * @param modifier The [Modifier] to be applied to the divider.
 */
@Composable
fun HorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}