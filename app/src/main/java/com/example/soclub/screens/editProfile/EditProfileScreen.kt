package com.example.soclub.screens.editProfile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R

@Composable
fun EditProfileScreen(navController: NavController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ProfileTextField(
            label = stringResource(id = R.string.profile_name_label),
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) }
        )

        ProfileTextField(
            label = stringResource(id = R.string.profile_email_label),
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.errorMessage != 0) {
            Text(
                text = stringResource(id = uiState.errorMessage),
                color = Color.Red
            )
        }

        SaveButton(onClick = { viewModel.onSaveProfileClick() })
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
    ) {
        Text(text = stringResource(id = R.string.save_changes_button), color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen(rememberNavController())
}
