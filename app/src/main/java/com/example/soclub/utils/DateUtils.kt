package com.example.soclub.utils


import java.util.Calendar
import java.util.Date
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration



/**
 * Determines if the device is in landscape orientation.
 *
 * @return True if the orientation is landscape, false otherwise.
 */
@Composable
fun isLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}

//HomeViewModel Date Utils
/**
 * Combines a Firebase Timestamp and a time string into a Date object.
 *
 * @param date The Firebase Timestamp representing the date.
 * @param timeString The time string in the format "HH:mm".
 * @return A Date object representing the combined date and time, or null if the input is invalid.
 */

fun combineDateAndTime(date: com.google.firebase.Timestamp?, timeString: String): Date? {
    if (date == null || timeString.isEmpty()) return null
    return try {
        val calendar = Calendar.getInstance()
        calendar.time = date.toDate()
        val timeParts = timeString.split(":")
        if (timeParts.size != 2) return null
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    } catch (e: Exception) {
        null
    }
}



