package com.example.soclub.screens.editActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditActivityViewModel @Inject constructor(
    private val activityService: ActivityService
) : ViewModel() {

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess

    // Function to fetch a specific activity based on category and activityId
    fun getActivity(category: String, activityId: String) = liveData {
        val activity = activityService.getActivityById(category, activityId)
        emit(activity)
    }

    // Update activity in Firestore
    fun updateActivity(category: String, activityId: String, updatedActivity: createActivity) {
        viewModelScope.launch {
            _isUpdating.value = true
            val success = activityService.updateActivity(category, activityId, updatedActivity)
            _updateSuccess.value = success
            _isUpdating.value = false
        }
    }
}
