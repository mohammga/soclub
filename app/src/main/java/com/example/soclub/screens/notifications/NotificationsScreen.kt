package com.example.soclub.screens.notifications


import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soclub.R
import com.example.soclub.models.Notification

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current // Get the context

    LaunchedEffect(Unit) {
        viewModel.loadNotifications(context)
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
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_notifications),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(notifications.size) { index ->
                            NotificationItem(
                                notification = notifications[index],
                                onDelete = { notification ->
                                    viewModel.deleteNotification(notification, context) // Pass context here
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



/*fun getTimeAgo(timestamp: Long): String {
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
}*/
fun getTimeAgo(timestamp: Long, context: Context): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> context.getString(R.string.just_now)
        minutes < 60 -> {
            val minuteText = if (minutes == 1L) context.getString(R.string.minute) else context.getString(R.string.minutes)
            context.getString(R.string.minutes_ago, minutes, minuteText)
        }
        hours < 24 -> {
            val hourText = if (hours == 1L) context.getString(R.string.hour) else context.getString(R.string.hours)
            context.getString(R.string.hours_ago, hours, hourText)
        }
        else -> {
            val dayText = if (days == 1L) context.getString(R.string.day) else context.getString(R.string.days)
            context.getString(R.string.days_ago, days, dayText)
        }
    }
}


@Composable
fun NotificationItem(
    notification: Notification,
    onDelete: (Notification) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(
                text = getTimeAgo(notification.timestamp, context),
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
        Box(
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { onDelete(notification) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.delete_Notification),
                )
            }
        }
    }
}


