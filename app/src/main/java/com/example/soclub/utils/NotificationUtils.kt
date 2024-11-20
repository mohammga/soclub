package com.example.soclub.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.soclub.R

fun createNotificationChannel(context: Context) {
    //val channelId = "activity_reminder_channel"
    //val channelName = "Activity Reminder Notifications"
    //val channelDescription = "Notifications for activity reminders and updates"
    val channelId = context.getString(R.string.channel_id)
    val channelName = context.getString(R.string.channel_name)
    val channelDescription = context.getString(R.string.channel_description)
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(channelId, channelName, importance).apply {
        description = channelDescription
    }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}




