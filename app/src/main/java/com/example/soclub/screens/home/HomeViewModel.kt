package com.example.soclub.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val activityService: ActivityService
) : ViewModel() {

    // Henter aktiviteter fra en kategori
    fun getActivities(category: String) = liveData(Dispatchers.IO) {
        val activities = activityService.getActivities(category)
        emit(activities)  // Sender aktivitetene videre til UI
    }

    // Oppretter en ny aktivitet
    suspend fun createActivity(category: String, activity: Activity) {
        activityService.createActivity(category, activity)
    }

    // Oppdaterer en eksisterende aktivitet
    suspend fun updateActivity(category: String, documentId: String, activity: Activity) {
        activityService.updateActivity(category, documentId, activity)
    }

    // Sletter en aktivitet
    suspend fun deleteActivity(category: String, documentId: String) {
        activityService.deleteActivity(category, documentId)
    }
}