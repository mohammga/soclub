package com.example.soclub.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat


/**
 * A utility class for managing permissions in the application.
 *
 * @param context The application context.
 * @param permissionLauncher The launcher used to request permissions.
 */
class PermissionHelper(
    private val context: Context,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>
) {
    private var isLocationPermissionGranted = false
    private var isPostNotificationsGranted = false
    private var isGalleryPermissionGranted = false

    /**
     * Initializes the class and checks the current permission statuses.
     */
    init {
        checkPermissions()
    }

    /**
     * Checks the current statuses of required permissions.
     */
    private fun checkPermissions() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isPostNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        isGalleryPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Requests the necessary permissions that are not currently granted.
     */
    fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (!isLocationPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPostNotificationsGranted) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!isGalleryPermissionGranted) {
            val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionsToRequest.add(galleryPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * Updates the statuses of permissions based on the results of a permission request.
     *
     * @param permissions A map of permissions and their grant statuses.
     */
    fun updatePermissionsStatus(permissions: Map<String, Boolean>) {
        isLocationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted

        isPostNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: isPostNotificationsGranted
        } else {
            true
        }

        isGalleryPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: isGalleryPermissionGranted
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isGalleryPermissionGranted
        }
    }
}

