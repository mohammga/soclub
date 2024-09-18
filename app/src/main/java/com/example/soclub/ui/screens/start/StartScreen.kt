package com.example.soclub.ui.screens.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import com.example.soclub.ui.navigation.AppScreens

@Composable
fun StartScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cat),
            contentDescription = stringResource(R.string.image_of_an_cat)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Velkommen til SoClub", fontSize = 21.sp, textAlign = TextAlign.Center,)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Velkommen til SoClub-app for sosialisering og deltakelse i ulike aktiviteter.",  textAlign = TextAlign.Center, )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(AppScreens.SIGNUP.name) },
            modifier = Modifier
        ) {
            Text(text = stringResource(R.string.see_image))
        }

    }
}

@Preview
@Composable
fun StartScreenPreview() {
    StartScreen(rememberNavController())
}
