package com.example.soclub.screens.activityDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val activityService: ActivityService,    // Injected service to handle activity data
    private val accountService: AccountService       // Injected service to handle account data
) : ViewModel() {

    // Holds the state of the current activity being viewed
    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity    // Publicly exposed state for observing activity data

    // Holds the registration status of the current user for the activity
    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered  // Publicly exposed state for observing registration status

    /**
     * Fetches the details of an activity based on category and activityId.
     * Also checks if the current user is already registered for this activity.
     */
    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            try {
                val fetchedActivity = activityService.getActivityById(category, activityId)
                _activity.value = fetchedActivity

                // Check if the user is registered
                val userId = accountService.currentUserId
                val registrationExists = activityService.isUserRegisteredForActivity(userId, activityId)
                _isRegistered.value = registrationExists
            } catch (e: Exception) {
                // Handle the error, e.g., show a toast or update UI with error message
                println("Error loading activity: ${e.message}")
            }
        }
    }

    /**
     * Registers the current user for the specified activity.
     */
    fun registerForActivity(activityId: String) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            // Update the user's registration status to "aktiv" (active)
            val success = activityService.updateRegistrationStatus(userId, activityId, "aktiv")
            if (success) {
                _isRegistered.value = true  // Update the state to reflect successful registration
            }
        }
    }

    /**
     * Unregisters the current user from the specified activity.
     */
    fun unregisterFromActivity(activityId: String) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            // Update the user's registration status to "notAktiv" (inactive)
            val success = activityService.updateRegistrationStatus(userId, activityId, "notAktiv")
            if (success) {
                _isRegistered.value = false  // Update the state to reflect successful unregistration
            }
        }
    }
}
