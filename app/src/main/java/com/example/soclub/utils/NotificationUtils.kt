// NotificationUtils.kt
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

    val delay24Hours = startTimeMillis - currentTimeMillis - TimeUnit.HOURS.toMillis(24)
    val delay12Hours = startTimeMillis - currentTimeMillis - TimeUnit.HOURS.toMillis(12)
    val delay1Hour = startTimeMillis - currentTimeMillis - TimeUnit.HOURS.toMillis(1)
    val delay2Minutes = startTimeMillis - currentTimeMillis - TimeUnit.MINUTES.toMillis(2)

    if (delay24Hours > 0) {
        Log.d("NotificationUtils", "Scheduling 24-hour notification")
        enqueueNotification(
            context,
            delay = delay24Hours,
            message = "$activityTitle starter om 24 timer",
            activityId = "$activityId-24h",
            userId = userId
        )
    }

    if (delay12Hours > 0) {
        Log.d("NotificationUtils", "Scheduling 12-hour notification")
        enqueueNotification(
            context,
            delay = delay12Hours,
            message = "$activityTitle starter om 12 timer",
            activityId = "$activityId-12h",
            userId = userId
        )
    }

    if (delay1Hour > 0) {
        Log.d("NotificationUtils", "Scheduling 1-hour notification")
        enqueueNotification(
            context,
            delay = delay1Hour,
            message = "$activityTitle starter om 1 time",
            activityId = "$activityId-1h",
            userId = userId
        )
    }

    if (delay2Minutes > 0) {
        Log.d("NotificationUtils", "Scheduling 2-minute notification")
        enqueueNotification(
            context,
            delay = delay2Minutes,
            message = "$activityTitle starter om 2 minutter",
            activityId = "$activityId-2min",
            userId = userId
        )
    }

        Log.d("NotificationUtils", "Scheduling 0-minute notification")
        enqueueNotification(
            context,
            delay = 0,
            message = "Du er påmeldt til aktiviteten $activityTitle",
            activityId = "$activityId-0min",
            userId = userId
        )
}

fun enqueueNotification(
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

    val tag = "$activityId-$userId" // Include userId in the tag

    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workData)
        .addTag(tag)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}

fun cancelNotificationForActivity(context: Context, activityId: String, userId: String) {
    Log.d("NotificationUtils", "Cancelling notifications for activity ID: $activityId and user ID: $userId")
    val workManager = WorkManager.getInstance(context)
    workManager.cancelAllWorkByTag("$activityId-24h-$userId")
    workManager.cancelAllWorkByTag("$activityId-12h-$userId")
    workManager.cancelAllWorkByTag("$activityId-1h-$userId")
    workManager.cancelAllWorkByTag("$activityId-2min-$userId")
    workManager.cancelAllWorkByTag("$activityId-0min-$userId")
}
