package com.example.soclub.screens.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetaillService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesScreenViewModel @Inject constructor(
    private val entriesService: EntriesService,
    private val accountService: AccountService,
    private val activityDetaillService: ActivityDetaillService
) : ViewModel() {

    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())
    val activeActivities: StateFlow<List<Activity>> = _activeActivities

    private val _isLoadingActive = MutableStateFlow(false)
    val isLoadingActive: StateFlow<Boolean> = _isLoadingActive

    private val _notActiveActivities = MutableStateFlow<List<Activity>>(emptyList())
    val notActiveActivities: StateFlow<List<Activity>> = _notActiveActivities

    private val _isLoadingInactive = MutableStateFlow(false)
    val isLoadingInactive: StateFlow<Boolean> = _isLoadingInactive

    private val _expiredActivities = MutableStateFlow<List<Activity>>(emptyList())
    val expiredActivities: StateFlow<List<Activity>> = _expiredActivities
    // I EntriesScreenViewModel

    private val _isLoadingExpired = MutableStateFlow(false)
    val isLoadingExpired: StateFlow<Boolean> = _isLoadingExpired


    init {
        listenForActivityUpdates()
        listenForNotActiveActivityUpdates()
        listenForExpiredActivityUpdates()
    }

    private fun listenForActivityUpdates() {
        val userId = accountService.currentUserId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                _isLoadingActive.value = true
                entriesService.getActiveActivitiesForUser(userId) { activities ->
                    _activeActivities.value = activities // Disse aktivitetene inkluderer nå kategori og ID
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
                    _isLoadingInactive.value = false
                }
            }
        }
    }

    fun cancelRegistration(activityId: String) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            activityDetaillService.updateRegistrationStatus(userId, activityId, "notAktiv")
        }
    }

    private fun listenForExpiredActivityUpdates() {
        val userId = accountService.currentUserId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                _isLoadingExpired.value = true  // Setter loading til true før lasting
                entriesService.getExpiredActivitiesForUser(userId) { activities ->
                    _expiredActivities.value = activities
                    _isLoadingExpired.value = false  // Setter loading til false etter lasting
                }
            }
        }
    }
}





