package com.example.soclub.screens.resetPassword

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.soclub.R


@Composable
fun ResetPasswordScreen(navController: NavController, viewModel: ResetPasswordViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState

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

        uiState.statusMessage?.let { messageId ->
            item {
                Text(
                    text = stringResource(id = messageId),
                    color = if (messageId == R.string.password_reset_email_sent) Color.Green else Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            ResetPasswordButton(viewModel)
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
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(id = R.string.email)) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun ResetPasswordButton(viewModel: ResetPasswordViewModel) {
    Button(
        onClick = { viewModel.onForgotPasswordClick() },
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

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(rememberNavController())
}
