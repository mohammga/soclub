package com.example.soclub.screens.entries

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetailService
import com.example.soclub.utils.scheduleReminder
import com.example.soclub.utils.cancelNotificationForActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesScreenViewModel @Inject constructor(
    private val entriesService: EntriesService,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())
    val activeActivities: StateFlow<List<Activity>> = _activeActivities

    private val _isLoadingActive = MutableStateFlow(false)
    val isLoadingActive: StateFlow<Boolean> = _isLoadingActive

    private val _notActiveActivities = MutableStateFlow<List<Activity>>(emptyList())
    val notActiveActivities: StateFlow<List<Activity>> = _notActiveActivities

    private val _isLoadingInactive = MutableStateFlow(false)
    val isLoadingInactive: StateFlow<Boolean> = _isLoadingInactive

    private val _isProcessingCancellation = MutableStateFlow<String?>(null)
    val isProcessingCancellation: StateFlow<String?> = _isProcessingCancellation


    init {
        listenForActivityUpdates()
        listenForNotActiveActivityUpdates()
    }

    private fun listenForActivityUpdates() {
        val userId = accountService.currentUserId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                _isLoadingActive.value = true
                entriesService.getActiveActivitiesForUser(userId) { activities ->
                    _activeActivities.value = activities.sortedByDescending { it.createdAt }
                    _isLoadingActive.value = false
                }
            }
        }
    }

    private fun listenForNotActiveActivityUpdates() {
        val userId = accountService.currentUserId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                _isLoadingInactive.value = true
                entriesService.getNotActiveActivitiesForUser(userId) { activities ->
                    _notActiveActivities.value = activities.sortedByDescending { it.createdAt }
                    _isLoadingInactive.value = false
                }
            }
        }
    }

    fun cancelRegistration(activityId: String) {
        viewModelScope.launch {
            _isProcessingCancellation.value = activityId // Start kanselleringsprosessen

            val userId = accountService.currentUserId
            val activityTitle = _activeActivities.value.find { it.id == activityId }?.title ?: "Aktivitet"

            val success = activityDetailService.updateRegistrationStatus(userId, activityId, "notAktiv")

            if (success) {
                // Update the active activities list to remove the cancelled activity
                _activeActivities.value = _activeActivities.value.filter { it.id != activityId }

                // Send a cancellation notification
                scheduleReminder(
                    context = context,
                    reminderTime = System.currentTimeMillis(),  // Send immediately
                    activityTitle = activityTitle,
                    activityId = activityId,
                    userId = userId,
                    sendNow = true,
                    isCancellation = true  // Specify cancellation message
                )

                // Optionally, cancel any pre-scheduled notifications for this activity
                cancelNotificationForActivity(context, activityId)

                // Display a Toast message for user feedback
                Toast.makeText(context, "Aktivitet kansellert", Toast.LENGTH_LONG).show()
            } else {
                // Håndter feil (du kan legge til feilhåndtering her hvis ønskelig)
            }

            _isProcessingCancellation.value = null // Fullfør kanselleringsprosessen
        }
    }
}

