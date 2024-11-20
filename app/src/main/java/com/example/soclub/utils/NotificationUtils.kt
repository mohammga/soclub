package com.example.soclub.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(context: Context) {
    val channelId = "activity_reminder_channel"
    val channelName = "Activity Reminder Notifications"
    val channelDescription = "Notifications for activity reminders and updates"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(channelId, channelName, importance).apply {
        description = channelDescription
    }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}




