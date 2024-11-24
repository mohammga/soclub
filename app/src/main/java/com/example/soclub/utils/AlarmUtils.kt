package com.example.soclub.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.soclub.R

/**
 * Utility function to request the "Exact Alarm Permission" if it is required
 * on devices running Android 12 (API level 31) or higher.
 *
 * This function checks whether the app has the necessary permission to schedule
 * exact alarms using the `AlarmManager`. If not, it shows an `AlertDialog` prompting
 * the user to grant the permission via the system settings.
 *
 * @param activity The [Activity] context used to display the dialog and start the intent.
 *
 * Usage:
 * ```
 * requestExactAlarmPermissionIfNeeded(this)
 * ```
 *
 * Requirements:
 * - The strings `R.string.exact_alarm_permission_title`, `R.string.exact_alarm_permission_message`,
 *   `R.string.allow`, and `R.string.cancel_eng` must be defined in the app's `strings.xml`.
 * - The app should target Android 12 or higher for this permission to be relevant.
 */
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
        // No action needed for Android versions below API 31
    }
}

