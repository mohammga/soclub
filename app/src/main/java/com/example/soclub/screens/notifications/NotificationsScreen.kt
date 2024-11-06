package com.example.soclub.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soclub.models.Notification

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Optionally, remove this if you're handling loading in the init block
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(notifications.size) { index ->
                        NotificationItem(
                            notification = notifications[index],
                            onDelete = { notification ->
                                viewModel.deleteNotification(notification)
                            }
                        )
                    }
                }
            }
        }
    }
}


fun getTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Akkurat nå"
        minutes < 60 -> {
            val minuteText = if (minutes == 1L) "minutt" else "minutter"
            "$minutes $minuteText siden"
        }
        hours < 24 -> {
            val hourText = if (hours == 1L) "time" else "timer"
            "$hours $hourText siden"
        }
        else -> {
            val dayText = if (days == 1L) "dag" else "dager"
            "$days $dayText siden"
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onDelete: (Notification) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getTimeAgo(notification.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        // Center the IconButton vertically by wrapping it in a Box
        Box(
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { onDelete(notification) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Notification",
                )
            }
        }
    }
}

