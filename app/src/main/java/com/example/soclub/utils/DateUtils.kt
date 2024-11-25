package com.example.soclub.utils

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


