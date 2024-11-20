package com.example.soclub.screens.ads

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
import com.example.soclub.models.EditActivity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityService: ActivityService,
    private val accountService: AccountService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _activities = MutableStateFlow<List<EditActivity>>(emptyList())
    val activities: StateFlow<List<EditActivity>> = _activities

    fun fetchActivitiesByCreator() {
        val creatorId = accountService.currentUserId
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val fetchedActivities = activityService.getAllActivitiesByCreator(creatorId).map {
                    EditActivity(
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
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = context.getString(R.string.error_message)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
