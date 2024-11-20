package com.example.soclub.utils
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Opens Google Maps with the given URI.
 *
 * @param context The application or activity context.
 * @param gmmIntentUri The URI for Google Maps navigation.
 */
fun openGoogleMaps(context: Context, gmmIntentUri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri))
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

/**
 * Displays a Toast message for registration or unregistration success.
 *
 * @param context The application context.
 * @param isRegistering True if registering, false if unregistering.
 * @param activity The activity object.
 * @param currentParticipants The current number of participants.
 */
fun showToast(context: Context, isRegistering: Boolean, activity: Activity?, currentParticipants: Int) {
    val maxParticipants = activity?.maxParticipants ?: 0
    val remainingSlots = maxParticipants - currentParticipants

    val message = if (isRegistering) {
        if (remainingSlots > 0) {
            context.getString(R.string.registration_successful)
        } else {
            context.getString(R.string.registration_successful_filled)
        }
    } else {
        context.getString(R.string.unregistered_successful)
    }

    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * Formats a Timestamp to a string with date and time.
 *
 * @param date The Firebase Timestamp.
 * @return Formatted date and time string.
 */
fun formatDate(date: Timestamp): String {
    val sdf = SimpleDateFormat("d. MMM yyyy, HH:mm", Locale("no", "NO"))
    return sdf.format(date.toDate())
}

/**
 * Formats a Timestamp to a string with date only.
 *
 * @param date The Firebase Timestamp.
 * @return Formatted date string.
 */
fun formatDateWithoutTime(date: Timestamp): String {
    val sdf = SimpleDateFormat("d. MMM yyyy", Locale("no", "NO"))
    return sdf.format(date.toDate())
}

/**
 * Splits a description into chunks for better readability.
 *
 * @param description The full description string.
 * @param linesPerChunk Number of sentences per chunk.
 * @return Formatted string with natural line breaks.
 */
fun splitDescriptionWithNaturalFlow(description: String, linesPerChunk: Int = 1): String {
    val sentences = description.split(Regex("(?<=\\.)\\s+"))
    val result = StringBuilder()
    var currentLines = 0

    for (sentence in sentences) {
        result.append(sentence.trim()).append(" ")
        currentLines++

        if (currentLines % linesPerChunk == 0) {
            result.append("\n\n")
        }
    }

    return result.toString().trim()
}