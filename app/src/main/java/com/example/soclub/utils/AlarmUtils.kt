package com.example.soclub.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.soclub.R

fun requestExactAlarmPermissionIfNeeded(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.exact_alarm_permission_title))
                .setMessage(activity.getString(R.string.exact_alarm_permission_message))
                .setPositiveButton(R.string.allow) { _, _ ->
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    activity.startActivity(intent)
                }
                .setNegativeButton(R.string.cancel_eng, null)
                .show()
        }
    } else {

    }
}
