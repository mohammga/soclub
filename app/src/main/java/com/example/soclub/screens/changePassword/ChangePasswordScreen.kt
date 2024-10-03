package com.example.soclub.screens.changePassword

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R

@Composable
fun ChangePasswordScreen(navController: NavController, viewModel: ChangePasswordViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PasswordInputField(
            label = stringResource(id = R.string.old_password_label),
            password = uiState.oldPassword,
            onPasswordChange = { viewModel.onOldPasswordChange(it) }
        )

        PasswordInputField(
            label = stringResource(id = R.string.new_password_label),
            password = uiState.newPassword,
            onPasswordChange = { viewModel.onNewPasswordChange(it) }
        )

        PasswordInputField(
            label = stringResource(id = R.string.confirm_new_password_label),
            password = uiState.confirmPassword,
            onPasswordChange = { viewModel.onConfirmPasswordChange(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.errorMessage != 0) {
            Text(
                text = stringResource(id = uiState.errorMessage),
                color = Color.Red
            )
        }

        Button(
            onClick = { viewModel.onChangePasswordClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.update_password_button))
        }
    }
}

@Composable
fun PasswordInputField(
    label: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation()
    )
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(rememberNavController())
}