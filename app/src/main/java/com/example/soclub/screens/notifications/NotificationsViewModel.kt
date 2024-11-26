package com.example.soclub.screens.notifications

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
import com.example.soclub.models.Notification
import com.example.soclub.service.NotificationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing notifications.
 *
 * This ViewModel provides data streams and functions to handle the following:
 * - Fetching and observing notifications from the `NotificationService`.
 * - Loading notifications on demand.
 * - Deleting notifications.
 * - Observing authentication state changes to manage notifications accordingly.
 *
 * @property notificationService The service used to interact with the notification data source.
 * @property firebaseAuth Firebase authentication instance for observing auth state changes.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    /**
     * StateFlow containing the list of notifications.
     */
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    /**
     * StateFlow indicating whether notifications are being loaded.
     */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * StateFlow holding an error message if an error occurs during operations.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * StateFlow representing the current count of notifications.
     */
    val notificationCount: StateFlow<Int> = _notifications.map { it.size }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = 0
    )

    /**
     * Job reference for managing the notifications coroutine.
     */
    private var notificationsJob: Job? = null

    /**
     * AuthStateListener to observe authentication state changes.
     */
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    /**
     * Initializes the ViewModel by setting up the AuthStateListener.
     */
    init {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                startListeningToNotifications()
            } else {
                stopListeningToNotifications()
                _notifications.value = emptyList()
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener!!)

        if (firebaseAuth.currentUser != null) {
            startListeningToNotifications()
        } else {
            _isLoading.value = false
        }
    }

    /**
     * Starts listening to the notifications stream.
     */
    private fun startListeningToNotifications() {
        if (notificationsJob == null || notificationsJob?.isCancelled == true) {
            notificationsJob = viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null

                try {
                    notificationService.getNotificationsStream().collect { notifications ->
                        _notifications.value = notifications.sortedByDescending { it.timestamp }
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error loading notifications"
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Stops listening to the notifications stream.
     */
    private fun stopListeningToNotifications() {
        notificationsJob?.cancel()
        notificationsJob = null
        _isLoading.value = false
    }

    /**
     * Cleans up resources when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { firebaseAuth.removeAuthStateListener(it) }
        stopListeningToNotifications()
    }

    /**
     * Loads notifications from the data source.
     *
     * This function sets the loading state, attempts to fetch notifications,
     * and handles any errors by updating the error message.
     *
     * @param context The context used to fetch localized error messages.
     */
    fun loadNotifications(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val notificationsFromDb = notificationService.getAllNotifications()
                _notifications.value = notificationsFromDb
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.error_message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a notification from the data source and updates the local state.
     *
     * If the deletion is successful, the notification is removed from the list
     * and a success message is displayed. If an error occurs, the error message is updated.
     *
     * @param notification The notification to be deleted.
     * @param context The context used to fetch localized messages and display Toasts.
     */
    fun deleteNotification(notification: Notification, context: Context) {
        viewModelScope.launch {
            try {
                notificationService.deleteNotification(notification)
                _notifications.value = _notifications.value.filter { it != notification }
                Toast.makeText(context, context.getString(R.string.notification_deleted), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.notification_deleted_error)
            }
        }
    }
}
