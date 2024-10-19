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

//    fun getCities() = liveData(Dispatchers.IO) {
//        try {
//            val activities = activityService.getAllActivities() // Hent alle aktiviteter
//            val cities = activities.mapNotNull { activity ->
//                val fullLocation = activity.location ?: "Ukjent"
//                fullLocation.substringAfterLast(" ") // Trekk ut byen
//            }.distinct() // Fjern duplikater
//            emit(cities)
//        } catch (e: Exception) {
//            emit(emptyList<String>())
//        }
//    }

    

}