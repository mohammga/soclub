package com.example.soclub.screens.changePassword

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChangePasswordScreen(navController: NavController, viewModel: ChangePasswordViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                PasswordField(label = stringResource(id = R.string.old_password_label), value = uiState.oldPassword, viewModel::onOldPasswordChange)

                PasswordField(label = stringResource(id = R.string.new_password_label), value = uiState.newPassword, viewModel::onNewPasswordChange)

                PasswordField(label = stringResource(id = R.string.confirm_new_password_label), value = uiState.confirmPassword, viewModel::onConfirmPasswordChange)

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.errorMessage != 0) {
                    Text(
                        text = stringResource(id = uiState.errorMessage),
                        color = Color.Red
                    )
                }

                ChangePasswordButton(navController, viewModel, snackbarHostState, coroutineScope)
            }
        }
    )
}

@Composable
fun PasswordField(
    label: String,
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
        onValueChange = {
            onNewValue(it)
            if (!isVisibleToggled) isVisible = it == ""
        },
        placeholder = { Text(label) },
        trailingIcon = {
            IconButton(onClick = {
                isVisible = !isVisible
                if (!isVisibleToggled) isVisibleToggled = true
            }) {
                Icon(painter = icon, contentDescription = if (isVisible) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password))
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation
    )
}

@Composable
private fun ChangePasswordButton(
    navController: NavController,
    viewModel: ChangePasswordViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    // Hent ressurser for meldinger før coroutineScope
    val errorMessageText = viewModel.uiState.value.errorMessage.takeIf { it != 0 }
        ?.let { stringResource(id = it) }
    val successMessageText = stringResource(id = R.string.password_change)

    Button(
        onClick = {
            viewModel.onChangePasswordClick()

            coroutineScope.launch {
                // Hvis det er feil, vis en feilmelding, ellers vis en suksessmelding
                if (errorMessageText != null) {
                    snackbarHostState.showSnackbar(
                        message = errorMessageText,
                        duration = SnackbarDuration.Long
                    )
                } else {
                    snackbarHostState.showSnackbar(
                        message = successMessageText,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = stringResource(id = R.string.update_password_button))
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(rememberNavController())
}
