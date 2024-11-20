package com.example.soclub.screens.changePassword

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soclub.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChangePasswordScreen(viewModel: ChangePasswordViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val isProcessing by viewModel.isProcessing
    val context = LocalContext.current

    Scaffold(
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
                        supportingText = stringResource(id = R.string.old_password_supporting_text),
                        enabled = !isProcessing
                    )
                }

                item {
                    PasswordField(
                        label = stringResource(id = R.string.new_password_label),
                        value = uiState.newPassword,
                        onNewValue = viewModel::onNewPasswordChange,
                        error = uiState.newPasswordError?.let { stringResource(id = it) },
                        supportingText = stringResource(id = R.string.new_password_supporting_text),
                        enabled = !isProcessing
                    )
                }

                item {
                    PasswordField(
                        label = stringResource(id = R.string.confirm_new_password_label),
                        value = uiState.confirmPassword,
                        onNewValue = viewModel::onConfirmPasswordChange,
                        error = uiState.confirmPasswordError?.let { stringResource(id = it) },
                        supportingText = stringResource(id = R.string.confirm_new_password_supporting_text),
                        enabled = !isProcessing
                    )
                }

                uiState.generalError?.let { errorId ->
                    item {
                        Text(
                            text = stringResource(id = errorId),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))


                    ChangePasswordButton(
                        viewModel = viewModel,
                        context = context,
                        enabled = !isProcessing,
                        isProcessing = isProcessing
                    )
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
    supportingText: String? = null,
    enabled: Boolean = true  // Legg til denne linjen
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
            IconButton(
                onClick = {
                    isVisible = !isVisible
                    if (!isVisibleToggled) isVisibleToggled = true
                },
                enabled = enabled  // Deaktiver ikonet når feltet er deaktivert
            ) {
                Icon(
                    painter = icon,
                    contentDescription = if (isVisible) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation,
        isError = error != null,
        supportingText = {
            if (error == null) {
                supportingText?.let { Text(text = it) }
            } else {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        },
        enabled = enabled  // Sett enabled på tekstfeltet
    )
}


@Composable
private fun ChangePasswordButton(
    viewModel: ChangePasswordViewModel,
    context: android.content.Context,
    enabled: Boolean,
    isProcessing: Boolean
) {
    val buttonText = if (isProcessing) {
        stringResource(id = R.string.updating_password_button)  // "Oppdaterer passord..."
    } else {
        stringResource(id = R.string.update_password_button)  // "Oppdater passord"
    }

    Button(
        onClick = {
            viewModel.onChangePasswordClick(context)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled
    ) {
        Text(text = buttonText)
    }
}

