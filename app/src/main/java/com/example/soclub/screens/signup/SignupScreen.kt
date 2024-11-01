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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R

@Composable
fun SignupScreen(navController: NavController, viewModel: SignupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }

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
            NameField(value = uiState.name, viewModel::onNameChange)
        }

        item {
            AgeField(value = uiState.age, viewModel::onAgeChange)
        }

        item {
            EmailField(value = uiState.email, viewModel::onEmailChange)
        }

        item {
            PasswordField(value = uiState.password, viewModel::onPasswordChange)
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.errorMessage != 0) {
            item {
                Text(
                    text = stringResource(id = uiState.errorMessage),
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            SignUpButton(navController, viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SignInButton(navController)
        }
    }
}

@Composable
fun NameField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_name)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun AgeField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_age)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit) {
    OutlinedTextField(
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.placeholder_email)) },
    )
}

@Composable
fun PasswordField(
    value: String,
    onNewValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var isVisibleToggled by remember { mutableStateOf(false) }

    val icon = if (isVisible) painterResource(R.drawable.ic_visibility_on) else painterResource(R.drawable.ic_visibility_off)
    val visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        value = value,
        onValueChange ={
            onNewValue(it)
            if (!isVisibleToggled) isVisible = it == ""
        },
        placeholder = { Text(stringResource(id = R.string.placeholder_password)) },
        trailingIcon = {
            IconButton(onClick = {
                isVisible = !isVisible
                if (!isVisibleToggled) isVisibleToggled = true
            }) {
                Icon(painter = icon, contentDescription = if (isVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password))
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation
    )
}

@Composable
private fun SignUpButton(navController: NavController, viewModel: SignupViewModel) {
    Button(
        onClick = { viewModel.onSignUpClick(navController) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.register))
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


@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen(rememberNavController())
}
