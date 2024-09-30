package com.example.soclub.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import com.example.soclub.components.navigation.AppScreens

@Composable
fun ProfileScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile image
        Image(
            painter = painterResource(R.drawable.user), // Replace with your image resource
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Profile Name
        Text(
            text = "Sarah Nordmann",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Edit Profile Button
        Button(
            onClick = { navController.navigate(AppScreens.EDIT_PROFILE.name)},
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(text = "Rediger profil")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Account Information Section
        Text(
            text = "Min konto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info rows (Name, Email, etc.)
        ProfileInfoRow(label = "Navn", value = "Sarah Nordmann")
        ProfileInfoRow(label = "E-post", value = "sarah-nordmann@gmail.com")
        ProfileInfoRow(label = "Passord", onClick = {
            navController.navigate("change_password")
        })
        ProfileInfoRow(label = "Tillatelser", onClick = {
            navController.navigate("edit_permission")
        })

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = {
                navController.navigate("signin")
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Logg ut",
                style = MaterialTheme.typography.labelLarge
            )
        }
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
            ClickableText(
                text = AnnotatedString(value),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.secondary,
                ),
                onClick = { onClick?.invoke() }
            )
        } else {
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}
