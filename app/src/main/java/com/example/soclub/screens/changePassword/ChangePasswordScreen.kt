package com.example.soclub.screens.changePassword

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun ChangePasswordScreen(navController: NavController) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PasswordInputField(
            label = "Gammelt passord",
            password = oldPassword,
            onPasswordChange = { oldPassword = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordInputField(
            label = "Nytt passord",
            password = newPassword,
            onPasswordChange = { newPassword = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordInputField(
            label = "Bekreft nytt passord",
            password = confirmPassword,
            onPasswordChange = { confirmPassword = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (newPassword == confirmPassword) {
                    // Her kan du legge til logikk for å oppdatere passordet
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = "Oppdater passord", color = Color.White)
        }
    }
}

@Composable
fun PasswordInputField(
    label: String,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(rememberNavController())
}
