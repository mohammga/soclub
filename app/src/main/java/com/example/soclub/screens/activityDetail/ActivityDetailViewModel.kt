package com.example.soclub.screens.activityDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetaillService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val accountService: AccountService,
    private val activityDetaillService: ActivityDetaillService
) : ViewModel() {


    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity


    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    private val _canRegister = MutableStateFlow(true)
    val canRegister: StateFlow<Boolean> = _canRegister

    /**
     * Fetches the details of an activity based on category and activityId.
     * Also checks if the current user is already registered for this activity.
     */

    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            val loadedActivity = activityDetaillService.getActivityById(category, activityId)
            _activity.value = loadedActivity

            // Sjekk om brukeren er registrert
            val userId = accountService.currentUserId
            val registered = activityDetaillService.isUserRegisteredForActivity(userId, activityId)
            _isRegistered.value = registered

            // Sjekk aldersgrense
            val userInfo = accountService.getUserInfo()
            if (loadedActivity != null) {
                _canRegister.value = userInfo.age >= loadedActivity.ageGroup
            }
        }
    }


    fun updateRegistrationForActivity(category: String, activityId: String, isRegistering: Boolean) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            val status = if (isRegistering) "aktiv" else "notAktiv"

            // Oppdater brukerens registreringsstatus
            val success = activityDetaillService.updateRegistrationStatus(userId, activityId, status)

            if (success) {
                _isRegistered.value = isRegistering

                // Hent nåværende aktivitet
                val currentActivity = _activity.value

                // Reduser antall deltakere hvis brukeren melder seg på, eller øk hvis brukeren melder seg av
                if (currentActivity != null && isRegistering) {
                    val updatedMaxParticipants = currentActivity.maxParticipants - 1
                    _activity.value = currentActivity.copy(maxParticipants = updatedMaxParticipants)

                    // Oppdater maxParticipants i databasen med kategori
                    activityDetaillService.updateMaxParticipants(category, activityId, updatedMaxParticipants)
                } else if (currentActivity != null && !isRegistering) {
                    val updatedMaxParticipants = currentActivity.maxParticipants + 1
                    _activity.value = currentActivity.copy(maxParticipants = updatedMaxParticipants)

                    // Oppdater maxParticipants i databasen med kategori
                    activityDetaillService.updateMaxParticipants(category, activityId, updatedMaxParticipants)
                }
            }
        }
    }

}
