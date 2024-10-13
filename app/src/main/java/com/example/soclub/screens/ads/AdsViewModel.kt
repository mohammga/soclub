package com.example.soclub.screens.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.createActivity
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

    private val _activities = MutableStateFlow<List<createActivity>>(emptyList())
    val activities: StateFlow<List<createActivity>> = _activities

    fun fetchActivitiesByCreator() {
        val creatorId = accountService.currentUserId  // Bruker innloggede brukers ID
        viewModelScope.launch {
            try {
                val fetchedActivities = activityService.getAllActivitiesByCreator(creatorId).map {
                    createActivity(
                        creatorId = it.id,
                        imageUrl = it.imageUrl,
                        title = it.title,
                        description = it.description,
                        ageGroup = it.ageGroup,
                        maxParticipants = it.maxParticipants,
                        location = it.location,
                        date = it.date,
                        time = it.time
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
