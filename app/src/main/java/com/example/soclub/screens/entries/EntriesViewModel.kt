package com.example.soclub.screens.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesScreenViewModel @Inject constructor(
    private val entriesService: EntriesService,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService
) : ViewModel() {

    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())
    val activeActivities: StateFlow<List<Activity>> = _activeActivities

    private val _isLoadingActive = MutableStateFlow(false)
    val isLoadingActive: StateFlow<Boolean> = _isLoadingActive

    private val _notActiveActivities = MutableStateFlow<List<Activity>>(emptyList())
    val notActiveActivities: StateFlow<List<Activity>> = _notActiveActivities

    private val _isLoadingInactive = MutableStateFlow(false)
    val isLoadingInactive: StateFlow<Boolean> = _isLoadingInactive


    private val _isLoadingExpired = MutableStateFlow(false)
    val isLoadingExpired: StateFlow<Boolean> = _isLoadingExpired


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
                    // Sorter aktiviteter etter 'createdAt' for å sikre nyeste øverst
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
                    // Sorter aktiviteter etter 'createdAt' for å sikre nyeste øverst
                    _notActiveActivities.value = activities.sortedByDescending { it.createdAt }
                    _isLoadingInactive.value = false
                }
            }
        }
    }

    fun cancelRegistration(activityId: String) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            activityDetailService.updateRegistrationStatus(userId, activityId, "notAktiv")

            // Midlertidig fjern den kansellerte aktiviteten fra listen lokalt
            _activeActivities.value = _activeActivities.value.filter { it.id != activityId }

            // La SnapshotListener håndtere oppdateringen etterpå
        }
    }



}




