package com.example.soclub.screens.editProfile

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.*
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(navController: NavController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Bruk LaunchedEffect for å hente profilinformasjonen når skjermen vises
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = stringResource(id = R.string.change_profile_picture),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.change_profile_picture),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {},
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClickableText(
                        text = AnnotatedString(stringResource(id = R.string.upload_new_picture)),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray,
                            fontSize = 14.sp
                        ),
                        onClick = {}
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.upload_new_picture),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brukerens navn og e-post vises nå automatisk i input-feltene
                ProfileTextField(
                    label = stringResource(id = R.string.profile_name_label),
                    value = uiState.firstname,
                    onValueChange = { viewModel.onNameChange(it) }
                )

                ProfileTextField(
                    label = stringResource(id = R.string.profile_lastname_label),
                    value = uiState.lastname,
                    onValueChange = { viewModel.onEmailChange(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.errorMessage != 0) {
                    Text(
                        text = stringResource(id = uiState.errorMessage),
                        color = Color.Red
                    )
                }

                SaveButton(onClick = {
                    viewModel.onSaveProfileClick(navController)
                    coroutineScope.launch {
                        // Henter strengen fra strings.xml og viser snackbar
                        snackbarHostState.showSnackbar(
                            message = "Personlig info er endret"
                        )
                    }
                })
            }
        }
    )
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
        placeholder = { Text(label) },
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
