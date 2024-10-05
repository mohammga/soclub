package com.example.soclub.screens.editPermission

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditPermissionViewModel(context: Context) : ViewModel() {

    // SharedPreferences for å lagre tillatelsene
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("permissions_prefs", Context.MODE_PRIVATE)

    // Initialiserer tillatelser fra SharedPreferences
    private val _locationPermission = MutableStateFlow(sharedPreferences.getBoolean("location", true))
    private val _cameraPermission = MutableStateFlow(sharedPreferences.getBoolean("camera", true))
    private val _notificationPermission = MutableStateFlow(sharedPreferences.getBoolean("notification", true))

    // Eksponere immutable StateFlow for UI
    val locationPermission: StateFlow<Boolean> = _locationPermission
    val cameraPermission: StateFlow<Boolean> = _cameraPermission
    val notificationPermission: StateFlow<Boolean> = _notificationPermission

    // Oppdater tillatelser og lagre dem i SharedPreferences
    fun setLocationPermission(enabled: Boolean) {
        viewModelScope.launch {
            _locationPermission.value = enabled
            savePermission("location", enabled)
        }
    }

    fun setCameraPermission(enabled: Boolean) {
        viewModelScope.launch {
            _cameraPermission.value = enabled
            savePermission("camera", enabled)
        }
    }

    fun setNotificationPermission(enabled: Boolean) {
        viewModelScope.launch {
            _notificationPermission.value = enabled
            savePermission("notification", enabled)
        }
    }

    // Lagrer tillatelse i SharedPreferences
    private fun savePermission(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
}
