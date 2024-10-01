package com.example.soclub.screens.LoginSelection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.components.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSelectionScreen(navController: NavController, viewModel: LoginSelectionViewModel = hiltViewModel()){

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.choose_sign_in_method),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 200.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(2f))

            Button(
                onClick = {
                    navController.navigate(AppScreens.LOGIN.name)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = stringResource(R.string.sign_in_with_email_and_password))
            }

            Button(
                onClick = {
                    navController.navigate(AppScreens.REGISTRATION.name)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = stringResource(R.string.register_new_user))
            }

            Button(
                onClick = {
                    viewModel.signInAnonymously(context, navController)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(Color.LightGray)
            ) {
                Text(text = stringResource(R.string.sign_in_anonymously))
            }

            Spacer(modifier = Modifier.weight(0.3f))

        }
    }
}