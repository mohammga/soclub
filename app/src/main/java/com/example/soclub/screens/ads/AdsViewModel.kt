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

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _activities = MutableStateFlow<List<editActivity>>(emptyList())
    val activities: StateFlow<List<editActivity>> = _activities

    fun fetchActivitiesByCreator() {
        val creatorId = accountService.currentUserId
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

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
                        category = it.category
                    )
                }
                _activities.value = fetchedActivities
                if (fetchedActivities.isEmpty()) {
                    _errorMessage.value = "Du har ingen annonser ennå."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Det skjedde en feil. Vennligst prøv igjen senere."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
