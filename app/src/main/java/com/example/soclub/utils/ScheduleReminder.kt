package com.example.soclub.utils

import android.Manifest
import android.annotation.SuppressLint
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

@SuppressLint("ScheduleExactAlarm")
fun scheduleReminder(
    context: Context,
    reminderTime: Long,
    activityTitle: String,
    activityId: String,
    userId: String,
    sendNow: Boolean = false,
    isCancellation: Boolean = false,  // Indicate if this is a cancellation notification
    isRegistration: Boolean = false,  // Indicate if this is a registration notification
    saveToDatabase: Boolean = true
) {
    createNotificationChannel(context)  // Ensure the notification channel exists

    // Check notification permission for Android 13 and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        Log.e("Permission Error", "Notification permission not granted.")
        return
    }

    // Determine the message based on the type of notification (registration, cancellation, or reminder)
    val message = when {
        isRegistration -> "Du er påmeldt til aktiviteten: $activityTitle"
        isCancellation -> "Du har avmeldt deg fra aktiviteten: $activityTitle"
        reminderTime <= System.currentTimeMillis() + (2 * 60 * 1000) -> "Aktiviteten starter om 2 minutter: $activityTitle"
        reminderTime <= System.currentTimeMillis() + (60 * 60 * 1000) -> "Aktiviteten starter om 1 time: $activityTitle"
        reminderTime <= System.currentTimeMillis() + (12 * 60 * 60 * 1000) -> "Aktiviteten starter om 12 timer: $activityTitle"
        reminderTime <= System.currentTimeMillis() + (24 * 60 * 60 * 1000) -> "Aktiviteten starter om 24 timer: $activityTitle"
        else -> "Påminnelse om din aktivitet: $activityTitle"
    }

    // Only save the notification to Firestore if specified
    if (saveToDatabase) {
        saveNotificationToDatabase(userId, activityId, message)
    }

    if (sendNow || reminderTime > System.currentTimeMillis()) {
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
            // Immediate notification
            val notification = NotificationCompat.Builder(context, "activity_reminder_channel")
                .setContentTitle("Aktivitetspåminnelse")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_name) // Use your relevant icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        }
    }
}


fun cancelNotificationForActivity(context: Context, activityId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Lag unike IDs for hver påminnelse: 24 timer, 12 timer og 1 time før aktiviteten
    val reminderIds = listOf("${activityId}_24hr", "${activityId}_12hr", "${activityId}_1hr")

    // Gå gjennom hver reminder ID og kanseller den tilhørende PendingIntent
    reminderIds.forEach { id ->
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),  // Unik ID basert på påminnelsestype
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Kanseller planlagt alarm og PendingIntent
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}


// Helper function to save notification in the database
fun saveNotificationToDatabase(userId: String, activityId: String, message: String) {
    val notification = Notification(
        userId = userId,
        activityId = activityId,
        message = message,
        timestamp = System.currentTimeMillis()
    )

    val notificationService = ServiceLocator.provideNotificationService()
    CoroutineScope(Dispatchers.IO).launch {
        notificationService.saveNotification(notification)
    }
}
