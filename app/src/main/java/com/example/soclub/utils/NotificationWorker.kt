package com.example.soclub.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
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
        setForegroundAsync(createForegroundInfo("Sender varsling..."))

        val message = inputData.getString("message") ?: return Result.failure()
        val userId = inputData.getString("userId") ?: return Result.failure()
        val activityId = inputData.getString("activityId") ?: return Result.failure()
        val timestamp = inputData.getLong("timestamp", System.currentTimeMillis())

        val channelId = getChannelId(activityId)
        showNotification(message, channelId)

        // Save notification to the database
        notificationService.saveNotification(
            Notification(userId = userId, activityId = activityId, timestamp = timestamp, message = message)
        )

        return Result.success()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showNotification(message: String, channelId: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelName = when (channelId) {
            "CANCEL_CHANNEL" -> "Kansellerte varsler"
            "SIGNUP_CHANNEL" -> "Deltakelsesvarsler"
            else -> "Påminnelsesvarsler"
        }

        val channelDescription = when (channelId) {
            "CANCEL_CHANNEL" -> "Varsler når aktiviteter er kansellert eller når brukere melder seg av."
            "SIGNUP_CHANNEL" -> "Varsler for deltakelse i aktiviteter."
            else -> "Varsler for påminnelser om aktiviteter."
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationTitle = when (channelId) {
            "CANCEL_CHANNEL" -> "Avbestilling"
            "SIGNUP_CHANNEL" -> "Deltakelse"
            else -> "Påminnelse"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getChannelId(activityId: String): String {
        return when {
            activityId.contains("unregistration") -> "CANCEL_CHANNEL"
            activityId.contains("signup") -> "SIGNUP_CHANNEL"
            else -> "REMINDER_CHANNEL"
        }
    }

    private fun createForegroundInfo(notificationText: String): ForegroundInfo {
        val channelId = "PROCESSING_CHANNEL"
        val channel = NotificationChannel(
            channelId,
            "Processing",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Processing Notification")
            .setTicker(notificationText)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        return ForegroundInfo(System.currentTimeMillis().toInt(), notification)
    }
}
