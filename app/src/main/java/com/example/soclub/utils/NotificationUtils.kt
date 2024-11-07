package com.example.soclub.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun scheduleNotificationForActivity(
    context: Context,
    activityTitle: String,
    activityId: String,
    startTimeMillis: Long,
    userId: String
) {
    val currentTimeMillis = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDate = dateFormat.format(Date(currentTimeMillis))
    val activityStartDate = dateFormat.format(Date(startTimeMillis))

    Log.d("NotificationUtils", "Current time: $currentDate")
    Log.d("NotificationUtils", "Activity start time: $activityStartDate")

    val delays = mapOf(
        "24h" to TimeUnit.HOURS.toMillis(24),
        "12h" to TimeUnit.HOURS.toMillis(12),
        "1h" to TimeUnit.HOURS.toMillis(1),
    )

    for ((label, delay) in delays) {
        val adjustedDelay = startTimeMillis - currentTimeMillis - delay
        if (adjustedDelay > 0) {
            Log.d("NotificationUtils", "Scheduling $label notification")
            enqueueReminderNotification(
                context,
                delay = adjustedDelay,
                message = "$activityTitle starter om $label",
                activityId = "$activityId-$label",
                userId = userId
            )
        }
    }
}

fun enqueueReminderNotification(
    context: Context,
    delay: Long,
    message: String,
    activityId: String,
    userId: String
) {
    val workData = workDataOf(
        "message" to message,
        "userId" to userId,
        "activityId" to activityId,
        "timestamp" to System.currentTimeMillis()
    )

    val tag = "$activityId-$userId"

    // Set constraints for reliability
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Can run offline
        .setRequiresBatteryNotLow(true) // Only runs if battery is not low
        .setRequiresDeviceIdle(false) // Can run even if device is not idle
        .build()

    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workData)
        .setConstraints(constraints)
        .addTag(tag)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        tag, // Ensures unique scheduling for the activity-user combination
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}

fun enqueueSignUpNotification(
    context: Context,
    activityTitle: String,
    userId: String
) {
    val activityId = "signup"
    val message = "Du er påmeldt til aktiviteten $activityTitle"
    enqueueReminderNotification(context, 0, message, activityId, userId)
}

fun enqueueUnregistrationNotification(
    context: Context,
    activityTitle: String,
    userId: String
) {
    val activityId = "unregistration" // Unique ID for unregistration notifications
    val message = "Du har meldt deg ut av aktiviteten $activityTitle"
    enqueueReminderNotification(context, 0, message, activityId, userId)
}

fun cancelNotificationForActivity(context: Context, activityId: String, userId: String) {
    Log.d("NotificationUtils", "Cancelling notifications for activity ID: $activityId and user ID: $userId")
    val workManager = WorkManager.getInstance(context)
    val tags = listOf("24h", "12h", "1h").map { "$activityId-$it-$userId" }

    for (tag in tags) {
        workManager.cancelAllWorkByTag(tag)
    }
}
