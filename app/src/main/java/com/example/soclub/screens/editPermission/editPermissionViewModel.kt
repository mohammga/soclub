package com.example.soclub.screens.editPermission

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditPermissionViewModel(context: Context) : ViewModel() {


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("permissions_prefs", Context.MODE_PRIVATE)


    private val _locationPermission = MutableStateFlow(sharedPreferences.getBoolean("location", true))
    private val _cameraPermission = MutableStateFlow(sharedPreferences.getBoolean("camera", true))
    private val _notificationPermission = MutableStateFlow(sharedPreferences.getBoolean("notification", true))

    val locationPermission: StateFlow<Boolean> = _locationPermission
    val cameraPermission: StateFlow<Boolean> = _cameraPermission
    val notificationPermission: StateFlow<Boolean> = _notificationPermission


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


    private fun savePermission(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
}
