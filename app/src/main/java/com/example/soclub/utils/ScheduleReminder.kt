package com.example.soclub.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.soclub.R
import com.example.soclub.models.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun scheduleReminder(
    context: Context,
    reminderTime: Long,
    activityTitle: String,
    activityId: String,
    userId: String,
    sendNow: Boolean = false,
    isCancellation: Boolean = false,
    isRegistration: Boolean = false,
    saveToDatabase: Boolean = true
) {
    createNotificationChannel(context)

    val hasNotificationPermission =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    if (!hasNotificationPermission) {
        Log.e("Permission Error", "Notification permission not granted.")
        return
    }

    val message = when {
        isRegistration -> context.getString(R.string.notification_registration, activityTitle)
        isCancellation -> context.getString(R.string.notification_cancellation, activityTitle)
        reminderTime <= System.currentTimeMillis() + (2 * 60 * 1000) -> context.getString(R.string.notification_2_minutes, activityTitle)
        reminderTime <= System.currentTimeMillis() + (60 * 60 * 1000) -> context.getString(R.string.notification_1_hour, activityTitle)
        reminderTime <= System.currentTimeMillis() + (12 * 60 * 60 * 1000) -> context.getString(R.string.notification_12_hours, activityTitle)
        reminderTime <= System.currentTimeMillis() + (24 * 60 * 60 * 1000) -> context.getString(R.string.notification_24_hours, activityTitle)
        else -> context.getString(R.string.notification_default, activityTitle)
    }

    if (saveToDatabase) {
        saveNotificationToDatabase(context, userId, activityId, message)
    }

    val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
        putExtra("activityTitle", activityTitle)
        putExtra("activityId", activityId)
        putExtra("message", message)
        putExtra("userId", userId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        activityId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (sendNow) {
        val notification = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }
}



fun cancelNotificationForActivity(context: Context, activityId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val reminderIds = listOf("${activityId}_24hr", "${activityId}_12hr", "${activityId}_1hr")

    reminderIds.forEach { id ->
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}


fun saveNotificationToDatabase(context: Context, userId: String, activityId: String, message: String) {
    val notification = Notification(
        userId = userId,
        activityId = activityId,
        message = message,
        timestamp = System.currentTimeMillis()
    )

    val notificationService = ServiceLocator.provideNotificationService(context)
    CoroutineScope(Dispatchers.IO).launch {
        notificationService.saveNotification(notification)
    }
}
