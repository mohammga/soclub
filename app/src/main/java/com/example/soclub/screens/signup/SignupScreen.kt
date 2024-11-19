package com.example.soclub.screens.signup

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soclub.R

/**
 * Composable function representing the Sign-Up screen.
 *
 * This screen allows new users to input their first name, last name, age, email, and password to create an account.
 * It includes input fields for each of these details, a button to submit the registration, and a button to navigate back to the Sign-In screen.
 *
 * @param navController The [NavController] used for navigation between screens.
 * @param viewModel The [SignupViewModel] managing the UI state and handling sign-up logic.
 */
@Composable
fun SignupScreen(navController: NavController, viewModel: SignupViewModel = hiltViewModel()) {
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
                text = stringResource(id = R.string.join_us),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            FirstNameField(
                value = uiState.firstname,
                onNewValue = viewModel::onFirstNameChange,
                error = uiState.firstNameError?.let { stringResource(id = it) },
                enabled = !isLoading
            )
        }

        item {
            LastNameField(
                value = uiState.lastname,
                onNewValue = viewModel::onLastNameChange,
                error = uiState.lastNameError?.let { stringResource(id = it) },
                enabled = !isLoading
            )
        }

        item {
            AgeField(
                value = uiState.age,
                onNewValue = viewModel::onAgeChange,
                error = uiState.ageError?.let { stringResource(id = it) },
                enabled = !isLoading
            )
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
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            if (uiState.generalError != null) {
                Text(
                    text = stringResource(id = uiState.generalError!!),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        item {
            SignUpButton(navController, viewModel, context, isLoading)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SignInButton(navController)
        }
    }
}

/**
 * Composable function for the first name input field.
 *
 * @param value The current value of the first name input.
 * @param onNewValue Callback invoked when the first name input changes.
 * @param error Optional error message to display below the input field.
 * @param enabled Determines whether the input field is enabled for user interaction.
 */
@Composable
fun FirstNameField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.firstname_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_firstname)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                Text(text = stringResource(id = R.string.firstname_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Composable function for the last name input field.
 *
 * @param value The current value of the last name input.
 * @param onNewValue Callback invoked when the last name input changes.
 * @param error Optional error message to display below the input field.
 * @param enabled Determines whether the input field is enabled for user interaction.
 */
@Composable
fun LastNameField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.lastname_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_lastname)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                Text(text = stringResource(id = R.string.lastname_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Composable function for the age input field.
 *
 * @param value The current value of the age input.
 * @param onNewValue Callback invoked when the age input changes.
 * @param error Optional error message to display below the input field.
 * @param enabled Determines whether the input field is enabled for user interaction.
 */
@Composable
fun AgeField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.age_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_age)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        supportingText = {
            if (error == null) {
                Text(text = stringResource(id = R.string.age_supporting_text))
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
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
        placeholder = { Text(stringResource(id = R.string.placeholder_email)) },
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
        value = value,
        onValueChange = onNewValue,
        label = { Text(stringResource(id = R.string.password_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_password)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        visualTransformation = visualTransformation,
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(painter = icon, contentDescription = null)
            }
        },
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
 * Composable function for the sign-up button.
 *
 * This button initiates the sign-up process by invoking the [SignupViewModel]'s [SignupViewModel.onSignUpClick] method.
 *
 * @param navController The [NavController] used for navigation after successful sign-up.
 * @param viewModel The [SignupViewModel] managing the sign-up logic.
 * @param context The [Context] used for displaying Toast messages.
 * @param isLoading Indicates whether a sign-up operation is currently in progress.
 */
@Composable
private fun SignUpButton(navController: NavController, viewModel: SignupViewModel, context: Context, isLoading: Boolean) {
    Button(
        onClick = { viewModel.onSignUpClick(navController, context) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = !isLoading
    ) {
        Text(text = if (isLoading) stringResource(id = R.string.registering) else stringResource(id = R.string.register))
    }
}

/**
 * Composable function for the sign-in button.
 *
 * This button navigates the user back to the Sign-In screen.
 *
 * @param navController The [NavController] used for navigation.
 */
@Composable
fun SignInButton(navController: NavController) {
    OutlinedButton(
        onClick = {
            navController.navigate("signin")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.sign_in))
    }
}
