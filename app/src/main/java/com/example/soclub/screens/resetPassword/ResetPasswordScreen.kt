package com.example.soclub.screens.resetPassword

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.res.stringResource
import com.example.soclub.R

@Composable
fun ResetPasswordScreen(viewModel: ResetPasswordViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
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
                error = uiState.emailError?.let { stringResource(id = it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Display error message in the UI if it's not a success
        uiState.statusMessage?.let { messageId ->
            if (messageId != R.string.password_reset_email_sent) {
                item {
                    Text(
                        text = stringResource(id = messageId),
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        item {
            ResetPasswordButton(viewModel, context)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            ResetPasswordInfo()
        }
    }
}

@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit, error: String?) {
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
private fun ResetPasswordButton(viewModel: ResetPasswordViewModel, context: Context) {
    Button(
        onClick = { viewModel.onForgotPasswordClick(context) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.send_email))
    }
}
@Composable
fun ResetPasswordInfo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(id = R.string.reset_password_info))
    }
}
