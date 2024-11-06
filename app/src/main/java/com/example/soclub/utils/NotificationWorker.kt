package com.example.soclub.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.soclub.R
import com.example.soclub.models.Notification
import com.example.soclub.service.NotificationService

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationService: NotificationService by lazy {
        ServiceLocator.provideNotificationService()
    }

    override suspend fun doWork(): Result {
        val message = inputData.getString("message") ?: return Result.failure()
        val userId = inputData.getString("userId") ?: return Result.failure()
        val activityId = inputData.getString("activityId") ?: return Result.failure()
        val timestamp = inputData.getLong("timestamp", System.currentTimeMillis())

        showNotification(message)

        // Save notification to the database
        notificationService.saveNotification(
            Notification(userId = userId, activityId = activityId, timestamp = timestamp, message = message)
        )

        return Result.success()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showNotification(message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SOCIAL_CHANNEL",
                "Påminnelser",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Påminnelser til aktiviteter"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "SOCIAL_CHANNEL")
            .setSmallIcon(R.drawable.user)
            .setContentTitle("Påminnelse")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
