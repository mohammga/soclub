package com.example.soclub.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

@SuppressLint("ServiceCast")
fun requestExactAlarmPermissionIfNeeded(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            AlertDialog.Builder(activity)  // Pass the Activity context directly
                .setTitle("Enable Exact Alarms")
                .setMessage("To provide timely notifications, please allow exact alarms.")
                .setPositiveButton("Allow") { _, _ ->
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    activity.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
