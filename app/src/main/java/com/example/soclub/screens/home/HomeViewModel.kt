package com.example.soclub.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityService: ActivityService
) : ViewModel() {

    // Hent alle kategorier fra Firestore
    fun getCategories() = liveData(Dispatchers.IO) {
        val categories = activityService.getCategories()
        emit(categories)
    }

    // Hent aktiviteter for en valgt kategori fra Firestore, returnerer LiveData
    fun getActivities(category: String) = liveData(Dispatchers.IO) {
        val activities = activityService.getActivities(category)
        emit(activities)
    }
}
