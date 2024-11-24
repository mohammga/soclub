package com.example.soclub.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.soclub.R

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        createNotificationChannel(context)

        val hasNotificationPermission =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (hasNotificationPermission) {
            val message = intent?.getStringExtra("message")
                ?: context.getString(R.string.default_notification_message)
            val activityId = intent?.getStringExtra("activityId") ?: return
            val userId = intent.getStringExtra("userId") ?: return

            val notification = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
            saveNotificationToDatabase(context, userId, activityId, message)
        } else {
            Log.e("Permission Error", "Notification permission not granted.")
        }
    }
}
