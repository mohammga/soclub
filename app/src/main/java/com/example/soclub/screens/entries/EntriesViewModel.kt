package com.example.soclub.screens.entries

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
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

/**
 * ViewModel for managing the entries screen.
 * Handles active and inactive activities for the user and provides methods for interaction.
 *
 * @property entriesService Service for fetching activities.
 * @property accountService Service for managing user accounts.
 * @property activityDetailService Service for handling activity details.
 * @property context The application context for notifications and Toast messages.
 */
@HiltViewModel
class EntriesScreenViewModel @Inject constructor(
    private val entriesService: EntriesService,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService,
    @ApplicationContext private val context: Context,
) : ViewModel() {


    /**
     * Flow to store the list of active activities.
     */
    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())

    /**
     * Flow to indicate loading status of active activities.
     */
    private val _isLoadingActive = MutableStateFlow(false)

    /**
     * Flow to store the list of inactive (cancelled) activities.
     */
    private val _notActiveActivities = MutableStateFlow<List<Activity>>(emptyList())

    /**
     * Flow to indicate loading status of inactive activities.
     */
    private val _isLoadingInactive = MutableStateFlow(false)


    /**
     * Flow to indicate the ID of an activity being processed for cancellation.
     */
    private val _isProcessingCancellation = MutableStateFlow<String?>(null)


    /**
     * Public state for observing active activities.
     */
    val activeActivities: StateFlow<List<Activity>> = _activeActivities


    /**
     * Public state for observing loading status of active activities.
     */
    val isLoadingActive: StateFlow<Boolean> = _isLoadingActive

    /**
     * Public state for observing inactive activities.
     */
    val notActiveActivities: StateFlow<List<Activity>> = _notActiveActivities

    /**
     * Public state for observing loading status of inactive activities.
     */
    val isLoadingInactive: StateFlow<Boolean> = _isLoadingInactive

    /**
     * Public state for observing the cancellation process status.
     */
    val isProcessingCancellation: StateFlow<String?> = _isProcessingCancellation


    /**
     * Initializes the ViewModel by setting up listeners for activity updates.
     */
    init {
        listenForActivityUpdates()
        listenForNotActiveActivityUpdates()
    }

    /**
     * Listens for updates to the user's active activities.
     * Fetches activities for the current user and updates the active activities state.
     */
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

    /**
     * Listens for updates to the user's inactive (cancelled) activities.
     * Fetches activities for the current user and updates the inactive activities state.
     */
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

    /**
     * Cancels the user's registration for a specific activity.
     *
     * @param activityId The ID of the activity to cancel registration for.
     */
    fun cancelRegistration(activityId: String) {
        viewModelScope.launch {
            _isProcessingCancellation.value = activityId

            val userId = accountService.currentUserId
            val activityTitle = _activeActivities.value.find { it.id == activityId }?.title ?: "Aktivitet"

            val success = activityDetailService.updateRegistrationStatus(userId, activityId, "notAktiv")

            if (success) {
                _activeActivities.value = _activeActivities.value.filter { it.id != activityId }
                scheduleReminder(
                    context = context,
                    reminderTime = System.currentTimeMillis(),
                    activityTitle = activityTitle,
                    activityId = activityId,
                    userId = userId,
                    sendNow = true,
                    isCancellation = true
                )
                cancelNotificationForActivity(context, activityId)
                //Toast.makeText(context, "Aktivitet kansellert", Toast.LENGTH_LONG).show()
                // Display a Toast message for user feedback
                //Toast.makeText(context, "Aktivitet kansellert", Toast.LENGTH_LONG).show()
                Toast.makeText(context, context.getString(R.string.activity_cancelled), Toast.LENGTH_LONG).show()
            }
            _isProcessingCancellation.value = null
        }
    }
}

