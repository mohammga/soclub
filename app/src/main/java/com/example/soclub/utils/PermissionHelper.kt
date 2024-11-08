package com.example.soclub.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionHelper(private val context: Context, private val permissionLauncher: ActivityResultLauncher<Array<String>>) {

    private var isLocationPermissionGranted = false
    private var isPostNotificationsGranted = false

    init {
        checkPermissions()
    }

    @SuppressLint("InlinedApi")
    private fun checkPermissions() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        isPostNotificationsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (!isLocationPermissionGranted) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!isPostNotificationsGranted) permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    @SuppressLint("InlinedApi")
    fun updatePermissionsStatus(permissions: Map<String, Boolean>) {
        isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
        isPostNotificationsGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: isPostNotificationsGranted
    }
}
