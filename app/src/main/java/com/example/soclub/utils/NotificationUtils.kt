package com.example.soclub.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.soclub.R


/**
 * Utility function to create a notification channel for devices running Android O (API level 26) or higher.
 * This ensures that notifications are properly categorized and displayed according to system guidelines.
 *
 * @param context The context from which the function is called, used to access system resources and services.
 *
 * This function retrieves the notification channel ID, name, and description from the app's string resources.
 * It then creates a notification channel with high importance, enabling audible and visual notifications.
 * If the device is running on an OS version lower than Android O, this function does nothing.
 *
 * Usage:
 * Call this function during app initialization or before sending a notification
 * to ensure the required notification channel exists.
 *
 * Example:
 * ```
 * createNotificationChannel(context)
 * ```
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}

