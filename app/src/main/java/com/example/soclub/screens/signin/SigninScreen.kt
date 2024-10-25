package com.example.soclub.screens.signin

import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.soclub.R


@Composable
fun SigninScreen(navController: NavController, viewModel: SigninViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

    ) {

        Text(
            text = stringResource(id = R.string.welcome_back),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(32.dp))


        EmailField(value = uiState.email, viewModel::onEmailChange)
        PasswordField(value = uiState.password, viewModel::onPasswordChange)

        if (uiState.errorMessage != 0) {
            Text(
                text = stringResource(id = uiState.errorMessage),
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ResetPasswordText(navController)

        Spacer(modifier = Modifier.height(32.dp))

        SignInButton(navController, viewModel, context)

        Spacer(modifier = Modifier.height(16.dp))

        SignUpButton(navController)

    }
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
        placeholder = { Text(stringResource(id = R.string.email)) },
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
        }               ,
        placeholder = { Text(stringResource(id = R.string.password)) },
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
private fun SignInButton(navController: NavController, viewModel: SigninViewModel, context: Context) {
    Button(
        onClick = { viewModel.onLoginClick(context, navController) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.sign_in))
    }
}

@Composable
fun SignUpButton(navController: NavController) {
    OutlinedButton(
        onClick = {
            navController.navigate("signup")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = stringResource(id = R.string.register))
    }
}

@Composable
fun ResetPasswordText(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(id = R.string.reset),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("reset_password")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SigninScreenPreview() {
    SigninScreen(rememberNavController())
}
