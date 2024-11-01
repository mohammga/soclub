package com.example.soclub.screens.changePassword

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PasswordField(
                        label = stringResource(id = R.string.old_password_label),
                        value = uiState.oldPassword,
                        onNewValue = viewModel::onOldPasswordChange,
                        error = uiState.oldPasswordError?.let { stringResource(id = it) },
                        supportingText = stringResource(id = R.string.old_password_supporting_text)
                    )
                }

                item {
                    PasswordField(
                        label = stringResource(id = R.string.new_password_label),
                        value = uiState.newPassword,
                        onNewValue = viewModel::onNewPasswordChange,
                        error = uiState.newPasswordError?.let { stringResource(id = it) },
                        supportingText = stringResource(id = R.string.new_password_supporting_text)
                    )
                }

                item {
                    PasswordField(
                        label = stringResource(id = R.string.confirm_new_password_label),
                        value = uiState.confirmPassword,
                        onNewValue = viewModel::onConfirmPasswordChange,
                        error = uiState.confirmPasswordError?.let { stringResource(id = it) },
                        supportingText = stringResource(id = R.string.confirm_new_password_supporting_text)
                    )
                }

                uiState.generalError?.let { errorId ->
                    item {
                        Text(
                            text = stringResource(id = errorId),
                            color = Color.Red
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))

                    ChangePasswordButton(navController, viewModel, snackbarHostState, coroutineScope)
                }
            }
        }
    )
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onNewValue: (String) -> Unit,
    error: String? = null,
    supportingText: String? = null
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
        label = { Text(label) },
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
        visualTransformation = visualTransformation,
        isError = error != null,
        supportingText = {
            supportingText?.let { Text(text = it) }
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun ChangePasswordButton(
    navController: NavController,
    viewModel: ChangePasswordViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val successMessageText = stringResource(id = R.string.password_change)

    Button(
        onClick = {
            viewModel.onChangePasswordClick()
            coroutineScope.launch {
                if (viewModel.uiState.value.generalError == null) {
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
