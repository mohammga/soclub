package com.example.soclub.screens.editPermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state of permissions (location, gallery, and notification).
 * Provides methods to check permission statuses and navigate to the app's settings.
 */
@HiltViewModel
class EditPermissionViewModel @Inject constructor() : ViewModel() {

    /**
     * Holds the state of the location permission.
     * `true` if the location permission is granted, otherwise `false`.
     */
    private val _locationPermission = MutableStateFlow(false)

    /**
     * Holds the state of the gallery permission.
     * `true` if the gallery permission is granted, otherwise `false`.
     */
    private val _galleryPermission = MutableStateFlow(false)

    /**
     * Holds the state of the notification permission.
     * `true` if the notification permission is granted, otherwise `false`.
     */
    private val _notificationPermission = MutableStateFlow(false)

    /**
     * Publicly exposes the state of the location permission as a read-only [StateFlow].
     * Observers can monitor changes to this value.
     */
    val locationPermission: StateFlow<Boolean> = _locationPermission

    /**
     * Publicly exposes the state of the gallery permission as a read-only [StateFlow].
     * Observers can monitor changes to this value.
     */
    val galleryPermission: StateFlow<Boolean> = _galleryPermission


    /**
     * Publicly exposes the state of the notification permission as a read-only [StateFlow].
     * Observers can monitor changes to this value.
     */
    val notificationPermission: StateFlow<Boolean> = _notificationPermission

    /**
     * Checks the status of location, gallery, and notification permissions.
     *
     * @param context The context used to access permission states.
     */

//Fikk hjelp ved bruk av AI på denne 
    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            _locationPermission.value = checkPermissionStatus(context, Manifest.permission.ACCESS_FINE_LOCATION)

            _galleryPermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissionStatus(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                        checkPermissionStatus(context, Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                checkPermissionStatus(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            _notificationPermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissionStatus(context, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                true
            }
        }
    }

    /**
     * Navigates the user to the app's settings page to modify permissions.
     *
     * @param context The context used to start the settings activity.
     */
    fun navigateToSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * Checks if a specific permission is granted.
     *
     * @param context The context used to access permission states.
     * @param permission The permission to check (e.g., location or gallery permission).
     * @return `true` if the permission is granted, otherwise `false`.
     */
    private fun checkPermissionStatus(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
