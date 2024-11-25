package com.example.soclub.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.models.UserInfo

/**
 * Composable for displaying the profile screen.
 *
 * This screen shows user profile information, options to edit the profile, and account settings.
 * It also allows users to log out of the application.
 *
 * @param navController The navigation controller for navigating between screens.
 * @param viewModel The ViewModel that provides user data and handles actions for this screen.
 */
@Composable
fun ProfileScreen(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    val userInfo = viewModel.userInfo
    val isLoading = viewModel.isLoading
    val isLoggingOut = viewModel.isLoggingOut

    LaunchedEffect(Unit) {
        viewModel.fetchUserInfo()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileImage(imageUrl = userInfo?.imageUrl)
                Spacer(modifier = Modifier.height(8.dp))

                ProfileName(firstname = userInfo?.firstname ?: "", lastname = userInfo?.lastname ?: "")
                Spacer(modifier = Modifier.height(8.dp))

                EditProfileButton(navController, isLoggingOut)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                AccountInfoSection(navController, userInfo, isLoggingOut)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                LogoutButton(navController, viewModel, isLoggingOut)
            }
        }
    }
}

/**
 * Displays the user's profile image.
 *
 * @param imageUrl The URL of the user's profile image.
 */
@Composable
fun ProfileImage(imageUrl: String?) {
    if (imageUrl.isNullOrEmpty()) {
        Image(
            painter = painterResource(R.drawable.user),
            contentDescription = stringResource(id = R.string.profile_picture_description),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = stringResource(id = R.string.profile_picture_description),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Displays the user's full name.
 *
 * @param firstname The user's first name.
 * @param lastname The user's last name.
 */
@Composable
fun ProfileName(firstname: String, lastname: String) {
    val fullName = if (firstname.isNotEmpty() && lastname.isNotEmpty()) {
        "$firstname $lastname"
    } else {
        stringResource(id = R.string.loading)
    }

    Text(
        text = fullName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Button for navigating to the edit profile screen.
 *
 * @param navController The navigation controller for navigating to the edit profile screen.
 * @param isLoggingOut A flag indicating whether the user is in the process of logging out.
 */
@Composable
fun EditProfileButton(navController: NavHostController, isLoggingOut: Boolean) {
    Button(
        onClick = { navController.navigate(AppScreens.EDIT_PROFILE.name) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        enabled = !isLoggingOut
    ) {
        Text(text = stringResource(id = R.string.edit_profile_button))
    }
}

/**
 * Displays a section for account information with clickable rows for different actions.
 *
 * @param navController The navigation controller for navigating to other screens.
 * @param userInfo The user's account information.
 * @param isLoggingOut A flag indicating whether the user is in the process of logging out.
 */
@Composable
fun AccountInfoSection(navController: NavHostController, userInfo: UserInfo?, isLoggingOut: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.account_info_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        val fullName = "${userInfo?.firstname} ${userInfo?.lastname}"
        ProfileInfoRow(label = stringResource(id = R.string.label_name), value = fullName)
        ProfileInfoRow(label = stringResource(id = R.string.label_age), value = (userInfo?.age ?: stringResource(id = R.string.loading)).toString())
        ProfileInfoRow(label = stringResource(id = R.string.label_email), value = userInfo?.email ?: stringResource(id = R.string.loading))
        ProfileInfoRow(label = stringResource(id = R.string.label_ads), enabled = !isLoggingOut) {
            navController.navigate("ads")
        }
        ProfileInfoRow(label = stringResource(id = R.string.label_password), enabled = !isLoggingOut) {
            navController.navigate("change_password")
        }
        ProfileInfoRow(label = stringResource(id = R.string.label_permissions), enabled = !isLoggingOut) {
            navController.navigate("edit_permission")
        }
    }
}

/**
 * Button for logging out the user.
 *
 * @param navController The navigation controller for navigating to the login screen.
 * @param viewModel The ViewModel handling logout functionality.
 * @param isLoggingOut A flag indicating whether the user is in the process of logging out.
 */
@Composable
fun LogoutButton(navController: NavHostController, viewModel: ProfileViewModel, isLoggingOut: Boolean) {
    val context = LocalContext.current

    TextButton(
        onClick = { viewModel.onSignOut(navController, context) },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isLoggingOut
    ) {
        Text(
            text = if (isLoggingOut) stringResource(id = R.string.logging_out_button) else stringResource(id = R.string.logout_button),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Displays a row of information with optional interaction.
 *
 * @param label The label of the row.
 * @param value The value displayed in the row.
 * @param enabled A flag indicating whether the row is clickable.
 * @param onClick An optional click listener for the row.
 */
@Composable
fun ProfileInfoRow(label: String, value: String = "", enabled: Boolean = true, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = onClick != null && enabled) { onClick?.invoke() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        if (value.isNotEmpty()) {
            Text(
                text = AnnotatedString(value),
                style = TextStyle(color = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.clickable(enabled = enabled) { onClick?.invoke() }
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }
    }
}
