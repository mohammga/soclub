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

    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())
    val activeActivities: StateFlow<List<Activity>> = _activeActivities

    private val _notActiveActivities = MutableStateFlow<List<Activity>>(emptyList())
    val notActiveActivities: StateFlow<List<Activity>> = _notActiveActivities

    private val _isLoadingActive = MutableStateFlow(false)
    val isLoadingActive: StateFlow<Boolean> = _isLoadingActive

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
                    _activeActivities.value = activities
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
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
                    _notActiveActivities.value = activities
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                    _isLoadingInactive.value = false
                }
            }
        }
    }

    fun cancelRegistration(activityId: String) {
        viewModelScope.launch {
            _isProcessingCancellation.value = activityId
            val userId = accountService.currentUserId
            val defaultActivityTitle = context.getString(R.string.default_activity)
            val activity = _activeActivities.value.find { it.id == activityId }
            val activityTitle = activity?.title ?: defaultActivityTitle

            val statusNotActive = context.getString(R.string.status_not_active)
            val category = activity?.category

            if (category != null) {
                val success = activityDetailService.updateRegistrationStatus(userId, activityId, category, statusNotActive)
                if (success) {
                val cancelledActivity = _activeActivities.value.find { it.id == activityId }
                _activeActivities.value = _activeActivities.value.filter { it.id != activityId }

                if (cancelledActivity != null) {
                    _notActiveActivities.value = (_notActiveActivities.value + cancelledActivity)
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                }

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

                Toast.makeText(context, context.getString(R.string.activity_cancelled), Toast.LENGTH_LONG).show()
            }

            _isProcessingCancellation.value = null
        }
    }
}
}

