package com.example.soclub.screens.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.editActivity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Bruk HiltViewModel og injiser ActivityService
@HiltViewModel
class AdsViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService // Injiserer AccountService for å få currentUserId
) : ViewModel() {

    private val _activities = MutableStateFlow<List<editActivity>>(emptyList())
    val activities: StateFlow<List<editActivity>> = _activities

fun fetchActivitiesByCreator() {
    val creatorId = accountService.currentUserId
    viewModelScope.launch {
        try {
            val fetchedActivities = activityService.getAllActivitiesByCreator(creatorId).map {
                editActivity(
                    creatorId = it.id,
                    imageUrl = it.imageUrl,
                    title = it.title,
                    description = it.description,
                    ageGroup = it.ageGroup,
                    maxParticipants = it.maxParticipants,
                    location = it.location,
                    date = it.date,
                    startTime = it.startTime,
                    category = it.category  // Inkluder kategori her
                )
            }
            _activities.value = fetchedActivities
        } catch (e: Exception) {
            e.printStackTrace()
            _activities.value = listOf()
        }
    }
}
}
