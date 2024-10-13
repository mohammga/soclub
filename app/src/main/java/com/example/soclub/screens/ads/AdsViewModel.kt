package com.example.soclub.screens.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Bruk HiltViewModel og injiser ActivityService
@HiltViewModel
class AdsViewModel @Inject constructor(
    private val activityService: ActivityService
) : ViewModel() {

    // StateFlow som holder listen av createActivity
    private val _activities = MutableStateFlow<List<createActivity>>(emptyList())
    val activities: StateFlow<List<createActivity>> = _activities

    // Funksjon for å hente aktiviteter for en spesifikk bruker basert på creatorId
    fun fetchActivitiesByCreator(creatorId: String) {
        viewModelScope.launch {
            try {
                val fetchedActivities = activityService.getAllActivitiesByCreator(creatorId).map {
                    createActivity(
                        creatorId = it.id,  // Kartlegger Activity.id til createActivity.creatorId
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
                _activities.value = fetchedActivities // Oppdaterer aktivitetene
            } catch (e: Exception) {
                // Håndter eventuelle feil som oppstår under henting av data
                e.printStackTrace()
                _activities.value = listOf()
            }
        }
    }
}
