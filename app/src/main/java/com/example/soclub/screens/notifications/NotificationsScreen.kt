package com.example.soclub.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class Notification(val timeAgo: String, val message: String)

@Composable
fun NotificationsScreen(navController: NavController) {
    // Sample notifications data
    val notifications = listOf(
        Notification("2 minutter siden", "Du har fått en ny melding fra Cathrine."),
        Notification("20 minutter siden", "Cathrine har meldt seg på arrangementet."),
        Notification("1 time siden", "Vennen din skal også delta på arrangementet."),
        Notification("I går", "Det er opprettet et nytt arrangement av vennen din.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Space between notifications
    ) {
        items(notifications.size) { index ->
            NotificationItem(notification = notifications[index])
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        BasicText(text = notification.timeAgo, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = notification.message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen(rememberNavController())
}
