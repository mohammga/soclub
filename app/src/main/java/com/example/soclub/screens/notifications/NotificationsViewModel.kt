package com.example.soclub.screens.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Notification
import com.example.soclub.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val notificationCount: StateFlow<Int> = _notifications.map { it.size }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        initialValue = 0
    )

    init {
        viewModelScope.launch {
            notificationService.getNotificationsStream().collect { notifications ->
                _notifications.value = notifications
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val notificationsFromDb = notificationService.getAllNotifications()
                _notifications.value = notificationsFromDb
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Det skjedde en feil. Vennligst prøv igjen senere."
                _isLoading.value = false
            }
        }
    }

    fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            try {
                notificationService.deleteNotification(notification)
                _notifications.value = _notifications.value.filter { it != notification }
            } catch (e: Exception) {
                _errorMessage.value = "Feil ved sletting av varsling. Vennligst prøv igjen senere."
            }
        }
    }
}
