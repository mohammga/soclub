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


    fun fetchAndFilterActivitiesByCities(selectedCities: List<String>, selectedCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Hent aktiviteter for den valgte kategorien
                val activities = activityService.getActivities(selectedCategory)

                // Filtrer aktivitetene som allerede er hentet basert på de valgte byene
                val filtered = if (selectedCities.isNotEmpty()) {
                    activities.filter { activity ->
                        selectedCities.contains(activity.location)
                    }
                } else {
                    activities // Hvis ingen byer er valgt, vis alle aktivitetene i den valgte kategorien
                }

                _filteredActivities.postValue(filtered) // Oppdater LiveData med filtrerte aktiviteter
            } catch (e: Exception) {
                _filteredActivities.postValue(emptyList()) // Håndter feil ved å vise tom liste
            }
        }
    }


    fun resetFilter() {
        _filteredActivities.postValue(emptyList()) // Nullstill filtrerte aktiviteter
    }





}

