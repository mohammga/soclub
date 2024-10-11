package com.example.soclub.screens.activityDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetaillService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService,
    private val activityDetaillService: ActivityDetaillService
) : ViewModel() {

    // Holds the state of the current activity being viewed
    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity

    // Holds the registration status of the current user for the activity
    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    /**
     * Fetches the details of an activity based on category and activityId.
     * Also checks if the current user is already registered for this activity.
     */
    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            try {
                val fetchedActivity = activityDetaillService.getActivityById(category, activityId)
                _activity.value = fetchedActivity

                // Check if the user is registered
                val userId = accountService.currentUserId
                val registrationExists = activityDetaillService.isUserRegisteredForActivity(userId, activityId)
                _isRegistered.value = registrationExists
            } catch (e: Exception) {

                println("Error loading activity: ${e.message}")
            }
        }
    }


    fun updateRegistrationForActivity(activityId: String, isRegistering: Boolean) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            val status = if (isRegistering) "aktiv" else "notAktiv"

            // Oppdater brukerens registreringsstatus basert på isRegistering
            val success = activityDetaillService.updateRegistrationStatus(userId, activityId, status)
            if (success) {
                _isRegistered.value = isRegistering  // Oppdaterer til riktig status basert på handlingen
            }
        }
    }
}
