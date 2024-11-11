package com.example.soclub.screens.signup

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

@Composable
fun SignupScreen(navController: NavController, viewModel: SignupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val isLoading by viewModel.isLoading

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
            SignUpButton(navController, viewModel, isLoading)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SignInButton(navController)
        }
    }
}

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

@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.email_label)) },
        placeholder = { Text(stringResource(id = R.string.placeholder_email)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
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

@Composable
fun PasswordField(value: String, onNewValue: (String) -> Unit, error: String?, enabled: Boolean) {
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

@Composable
private fun SignUpButton(navController: NavController, viewModel: SignupViewModel, isLoading: Boolean) {
    val context = LocalContext.current
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