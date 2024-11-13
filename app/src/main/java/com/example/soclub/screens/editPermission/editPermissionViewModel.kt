package com.example.soclub.screens.editPermission

import android.Manifest
import android.annotation.SuppressLint
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

@HiltViewModel
class EditPermissionViewModel @Inject constructor() : ViewModel() {

    private val _locationPermission = MutableStateFlow(false)
    private val _galleryPermission = MutableStateFlow(false) // Renamed from camera to gallery
    private val _notificationPermission = MutableStateFlow(false)

    val locationPermission: StateFlow<Boolean> = _locationPermission
    val galleryPermission: StateFlow<Boolean> = _galleryPermission // Renamed from cameraPermission
    val notificationPermission: StateFlow<Boolean> = _notificationPermission

    // Function to check permission status
    @SuppressLint("InlinedApi")
    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            _locationPermission.value = checkPermissionStatus(context, Manifest.permission.ACCESS_FINE_LOCATION)

            // Check for gallery permission based on Android version
            _galleryPermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissionStatus(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                        checkPermissionStatus(context, Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                checkPermissionStatus(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            _notificationPermission.value = checkPermissionStatus(context, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Function to open settings
    fun navigateToSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }



    private fun checkPermissionStatus(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }




}