package com.example.soclub.screens.resetPassword

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soclub.R

/**
 * Composable function representing the Reset Password screen.
 *
 * This screen allows users to input their email address to receive a password reset link.
 * It includes an input field for the email, a button to submit the request, and informational text.
 *
 * @param viewModel The [ResetPasswordViewModel] managing the UI state and handling password reset logic.
 */
@Composable
fun ResetPasswordScreen(viewModel: ResetPasswordViewModel = hiltViewModel()) {
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
                text = stringResource(id = R.string.reset_password_text),
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.statusMessage?.let { messageId ->
            if (messageId != R.string.password_reset_email_sent) {
                item {
                    Text(
                        text = stringResource(id = messageId),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        item {
            ResetPasswordButton(viewModel, context, isLoading)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            ResetPasswordInfo()
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
        value = value,
        onValueChange = { onNewValue(it) },
        label = { Text(stringResource(id = R.string.email_label)) },
        placeholder = { Text(stringResource(id = R.string.email_placeholder)) },
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

/**
 * Composable function for the reset password button.
 *
 * This button initiates the password reset process by invoking the [ResetPasswordViewModel]'s [ResetPasswordViewModel.onForgotPasswordClick] method.
 *
 * @param viewModel The [ResetPasswordViewModel] managing the password reset logic.
 * @param context The [Context] used for displaying Toast messages.
 * @param isLoading Indicates whether a password reset operation is currently in progress.
 */
@Composable
private fun ResetPasswordButton(viewModel: ResetPasswordViewModel, context: Context, isLoading: Boolean) {
    Button(
        onClick = { viewModel.onForgotPasswordClick(context) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = !isLoading
    ) {
        Text(text = if (isLoading) stringResource(id = R.string.sending_email) else stringResource(id = R.string.send_email))
    }
}

/**
 * Composable function for displaying informational text about the password reset process.
 *
 * Provides users with additional information or instructions regarding resetting their password.
 */
@Composable
fun ResetPasswordInfo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(id = R.string.reset_password_info))
    }
}
