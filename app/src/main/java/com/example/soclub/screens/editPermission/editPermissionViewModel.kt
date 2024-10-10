package com.example.soclub.screens.editPermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
    private val _cameraPermission = MutableStateFlow(false)
    private val _notificationPermission = MutableStateFlow(false)

    val locationPermission: StateFlow<Boolean> = _locationPermission
    val cameraPermission: StateFlow<Boolean> = _cameraPermission
    val notificationPermission: StateFlow<Boolean> = _notificationPermission

    // Funksjon til å sjekke tillatelse status
    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            _locationPermission.value = checkPermissionStatus(context, Manifest.permission.ACCESS_FINE_LOCATION)
            _cameraPermission.value = checkPermissionStatus(context, Manifest.permission.CAMERA)
            _notificationPermission.value = checkPermissionStatus(context, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Funksjon som åpner instilinger
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