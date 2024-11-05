package com.example.soclub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionHelper(private val context: Context, private val permissionLauncher: ActivityResultLauncher<Array<String>>) {

    var isLocationPermissionGranted = false
        private set
    var isGalleryPermissionGranted = false
        private set
    var isPostNotificationsGranted = false
        private set

    init {
        checkPermissions()
    }

    private fun checkPermissions() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        isGalleryPermissionGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        isPostNotificationsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (!isLocationPermissionGranted) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!isGalleryPermissionGranted) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (!isPostNotificationsGranted) permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    fun updatePermissionsStatus(permissions: Map<String, Boolean>) {
        isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
        isPostNotificationsGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: isPostNotificationsGranted
        isGalleryPermissionGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: isGalleryPermissionGranted
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isGalleryPermissionGranted
        }
    }
}
