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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.soclub.R
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.models.UserInfo

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    val userInfo = viewModel.userInfo

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ProfileImage(imageUrl = userInfo?.imageUrl)
            Spacer(modifier = Modifier.height(8.dp))

            ProfileName(name = userInfo?.name ?: "Laster...")
            Spacer(modifier = Modifier.height(8.dp))

            EditProfileButton(navController)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            AccountInfoSection(navController, userInfo)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            LogoutButton(navController, viewModel)
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?) {
    if (imageUrl.isNullOrEmpty()) {
        // Show placeholder image when there's no image URL
        Image(
            painter = painterResource(R.drawable.user),
            contentDescription = stringResource(id = R.string.profile_picture_description),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Load image from URL using Coil
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

@Composable
fun ProfileName(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun EditProfileButton(navController: NavHostController) {
    Button(
        onClick = { navController.navigate(AppScreens.EDIT_PROFILE.name) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text(text = stringResource(id = R.string.edit_profile_button))
    }
}

@Composable
fun AccountInfoSection(navController: NavHostController, userInfo: UserInfo?) {
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

        ProfileInfoRow(label = stringResource(id = R.string.label_name), value = userInfo?.name ?: stringResource(id = R.string.loading))
        ProfileInfoRow(label = stringResource(id = R.string.label_age), value = (userInfo?.age ?: stringResource(id = R.string.loading)).toString())
        ProfileInfoRow(label = stringResource(id = R.string.label_email), value = userInfo?.email ?: stringResource(id = R.string.loading))
        ProfileInfoRow(label = stringResource(id = R.string.label_ads), onClick = {
            navController.navigate("ads")
        })
        ProfileInfoRow(label = stringResource(id = R.string.label_password), onClick = {
            navController.navigate("change_password")
        })
        ProfileInfoRow(label = stringResource(id = R.string.label_permissions), onClick = {
            navController.navigate("edit_permission")
        })
    }
}

@Composable
fun LogoutButton(navController: NavHostController, viewModel: ProfileViewModel) {
    TextButton(
        onClick = { viewModel.onSignOut(navController) },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = stringResource(id = R.string.logout_button),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String = "", onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        if (value.isNotEmpty()) {
            Text(
                text = AnnotatedString(value),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier.clickable { onClick?.invoke() }
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}
