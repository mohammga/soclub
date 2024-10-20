package com.example.soclub.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityService: ActivityService
) : ViewModel() {

    // Hent alle kategorier fra Firestore
    fun getCategories() = liveData(Dispatchers.IO) {
        try {
            val categories = activityService.getCategories()
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList<String>())
        }
    }

    // Hent aktiviteter for en valgt kategori fra Firestore, returnerer LiveData
    fun getActivities(category: String) = liveData(Dispatchers.IO) {
        try {
            val activities = activityService.getActivities(category)
            println("Henter aktiviteter for kategori: $category")
            println("Antall aktiviteter hentet: ${activities.size}")
            emit(activities)
        } catch (e: Exception) {
            emit(emptyList<Activity>())
        }
    }

    fun getCities() = liveData(Dispatchers.IO) {
        try {
            val activities = activityService.getAllActivities() // Hent alle aktiviteter
            val cities = activities.mapNotNull { activity ->
                val fullLocation = activity.location ?: "Ukjent"
                fullLocation.substringAfterLast(" ") // Trekk ut byen
            }.distinct() // Fjern duplikater
            emit(cities)
        } catch (e: Exception) {
            emit(emptyList<String>())
        }
    }

    private val _filteredActivities = MutableLiveData<List<Activity>>()
    val filteredActivities: LiveData<List<Activity>> get() = _filteredActivities


    fun filterActivitiesByCities(selectedCities: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val activities = activityService.getAllActivities() // Hent alle aktiviteter
                val filtered = activities.filter { activity ->
                    selectedCities.contains(activity.location) // Filtrer basert på by
                }
                _filteredActivities.postValue(filtered) // Oppdater LiveData med filtrerte aktiviteter
            } catch (e: Exception) {
                _filteredActivities.postValue(emptyList()) // I tilfelle feil
            }
        }
    }

    fun fetchAndFilterActivitiesByCities(selectedCities: List<String>, categories: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filteredActivities = mutableListOf<Activity>()
                // Hent aktiviteter for hver kategori og filtrer etter valgte byer
                for (category in categories) {
                    val activities = activityService.getActivities(category)
                    val filtered = activities.filter { activity ->
                        selectedCities.contains(activity.location) // Sjekk om aktiviteten er i en valgt by
                    }
                    filteredActivities.addAll(filtered)
                }
                _filteredActivities.postValue(filteredActivities) // Oppdater filtrerte aktiviteter
            } catch (e: Exception) {
                _filteredActivities.postValue(emptyList()) // Håndter feil
            }
        }
    }




    fun resetFilter() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allActivities = activityService.getAllActivities() // Hent alle aktiviteter på nytt
                _filteredActivities.postValue(allActivities) // Oppdater visningen
            } catch (e: Exception) {
                _filteredActivities.postValue(emptyList())
            }
        }
    }







}

