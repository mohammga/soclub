package com.example.soclub.screens.signin

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.soclub.R

/**
 * Composable function representing the Sign-In screen.
 *
 * This screen allows users to input their email and password to sign in.
 * It includes fields for email and password, buttons for signing in and navigating to the sign-up screen,
 * and a link for resetting the password.
 *
 * @param navController The [NavController] used for navigation between screens.
 * @param viewModel The [SigninViewModel] managing the UI state and handling sign-in logic.
 */
@Composable
fun SigninScreen(navController: NavController, viewModel: SigninViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.welcome_back),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            EmailField(
                value = uiState.email,
                onNewValue = viewModel::onEmailChange,
                error = uiState.emailError?.let { stringResource(id = it) },
                enabled = !isLoading
            )
        }

        item {
            PasswordField(
                value = uiState.password,
                onNewValue = viewModel::onPasswordChange,
                error = uiState.passwordError?.let { stringResource(id = it) },
                enabled = !isLoading
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            ResetPasswordText(navController)
        }

        item {
            if (uiState.generalError != null) {
                Text(
                    text = stringResource(id = uiState.generalError!!),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SignInButton(navController, viewModel, context, isLoading)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SignUpButton(navController)
        }
    }
}

/**
 * Composable function for the email input field.
 *
 * @param value The current value of the email input.
 * @param onNewValue Callback invoked when the email input changes.
 * @param error Optional error message to display below the input field.
 * @param enabled Determines whether the input field is enabled for user interaction.
 */
@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.email_label)) },
        placeholder = { Text(stringResource(id = R.string.email_label)) },
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                Text(text = stringResource(id = R.string.email_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Composable function for the password input field.
 *
 * @param value The current value of the password input.
 * @param onNewValue Callback invoked when the password input changes.
 * @param error Optional error message to display below the input field.
 * @param enabled Determines whether the input field is enabled for user interaction.
 */
@Composable
fun PasswordField(
    value: String,
    onNewValue: (String) -> Unit,
    error: String?,
    enabled: Boolean
) {
    var isVisible by remember { mutableStateOf(true) }
    val icon = if (isVisible) painterResource(R.drawable.ic_visibility_on) else painterResource(R.drawable.ic_visibility_off)
    val visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        value = value,
        onValueChange = onNewValue,
        label = { Text(stringResource(id = R.string.password_label)) },
        placeholder = { Text(stringResource(id = R.string.password_label)) },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(painter = icon, contentDescription = null)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation,
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                Text(text = stringResource(id = R.string.password_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Composable function for the sign-in button.
 *
 * @param navController The [NavController] used for navigation.
 * @param viewModel The [SigninViewModel] managing the sign-in logic.
 * @param context The [Context] used for displaying Toast messages.
 * @param isLoading Indicates whether a sign-in operation is currently in progress.
 */
@Composable
private fun SignInButton(navController: NavController, viewModel: SigninViewModel, context: Context, isLoading: Boolean) {
    Button(
        onClick = { viewModel.onLoginClick(navController, context) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = !isLoading
    ) {
        Text(text = if (isLoading) stringResource(id = R.string.logging_in) else stringResource(id = R.string.sign_in))
    }
}

/**
 * Composable function for the sign-up button.
 *
 * This button navigates the user to the sign-up screen.
 *
 * @param navController The [NavController] used for navigation.
 */
@Composable
fun SignUpButton(navController: NavController) {
    OutlinedButton(
        onClick = {
            navController.navigate("signup")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.register))
    }
}

/**
 * Composable function for the reset password text link.
 *
 * This text is clickable and navigates the user to the reset password screen.
 *
 * @param navController The [NavController] used for navigation.
 */
@Composable
fun ResetPasswordText(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(id = R.string.reset),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                navController.navigate("reset_password")
            }
        )
    }
}
