package com.example.soclub.screens.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesScreenViewModel @Inject constructor(
    private val entriesService: EntriesService,
    private val accountService: AccountService
) : ViewModel() {

    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())
    val activeActivities: StateFlow<List<Activity>> = _activeActivities

    init {
        loadActiveActivities()
    }

    private fun loadActiveActivities() {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            if (userId.isNotEmpty()) {
                val activities = entriesService.getActiveActivitiesForUser(userId)
                _activeActivities.value = activities
            }
        }
    }
}
