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


    private val _currentParticipants = MutableStateFlow(0)
    val currentParticipants: StateFlow<Int> = _currentParticipants

    fun loadRegisteredParticipants(activityId: String) {
        viewModelScope.launch {
            val count = activityDetaillService.getRegisteredParticipantsCount(activityId)
            _currentParticipants.value = count
        }
    }



    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            val loadedActivity = activityDetaillService.getActivityById(category, activityId)
            _activity.value = loadedActivity


            val userId = accountService.currentUserId
            val registered = activityDetaillService.isUserRegisteredForActivity(userId, activityId)
            _isRegistered.value = registered


            val userInfo = accountService.getUserInfo()
            if (loadedActivity != null) {
                _canRegister.value = userInfo.age >= loadedActivity.ageGroup
            }

            loadRegisteredParticipants(activityId)

            activityDetaillService.listenToRegistrationUpdates(activityId) { count ->
                _currentParticipants.value = count
            }

        }
    }

    fun updateRegistrationForActivity(category: String, activityId: String, isRegistering: Boolean) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            val status = if (isRegistering) "aktiv" else "notAktiv"

            val success = activityDetaillService.updateRegistrationStatus(userId, activityId, status)
            if (success) {
                _isRegistered.value = isRegistering

                val currentActivity = _activity.value
                if (currentActivity != null) {

                    loadRegisteredParticipants(activityId)
                }
            }
        }
    }


}
