package com.example.soclub.screens.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import androidx.lifecycle.*
import kotlinx.coroutines.delay

import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application, // Få tilgang til Application context
    private val activityService: ActivityService,
    private val fusedLocationClient: FusedLocationProviderClient
) : AndroidViewModel(application) {



    private val _userCity = MutableLiveData<String?>()
    val userCity: LiveData<String?> get() = _userCity

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> = _activities

    private val _groupedActivities = MutableLiveData<Map<String, List<Activity>>>()
    val groupedActivities: LiveData<Map<String, List<Activity>>> get() = _groupedActivities

    private val _selectedCities = MutableLiveData<List<String>>(emptyList())
    val selectedCities: LiveData<List<String>> get() = _selectedCities

    init {
        fetchAndGroupActivitiesByCities(emptyList()) // Hent aktiviteter uten filter ved oppstart
    }


    // Oppdater `userCity` når plasseringen hentes
    @SuppressLint("MissingPermission")
    private fun fetchUserLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val location: Location? = fusedLocationClient.lastLocation.await()
                location?.let {
                    _userCity.postValue(getCityFromLocation(it))
                }
            } catch (e: Exception) {
                Log.e("LocationError", "Feil ved henting av posisjon: ${e.message}")
            }
        }
    }


    // Oppdater valgte byer
    fun updateSelectedCities(city: String, isSelected: Boolean) {
        val currentCities = _selectedCities.value?.toMutableList() ?: mutableListOf()
        if (isSelected) {
            if (!currentCities.contains(city)) currentCities.add(city)
        } else {
            currentCities.remove(city)
        }
        _selectedCities.value = currentCities
    }


    fun getCategories(): LiveData<List<String>> = liveData {
        try {
            val categories = activityService.getCategories().toMutableList()
            // Sørg for at "Nærme Aktiviteter" er tilgjengelig
            if (!categories.contains("Nærme Aktiviteter")) {
                categories.add(0, "Nærme Aktiviteter")
            }
            emit(categories)
        } catch (e: Exception) {
            emit(listOf("Nærme Aktiviteter"))
        }
    }


    fun getActivities(category: String) {
        _isLoading.value = true
        viewModelScope.launch {
            delay(3000)  // 3 sekunder forsinkelse
            try {

                val activities = if (category == "Nærme Aktiviteter") {
                    val cityToFilter = _userCity.value ?: "Fredrikstad"
                    activityService.getAllActivities().filter { activity ->
                        activity.location.contains(cityToFilter, ignoreCase = true)
                    }
                } else {
                    activityService.getActivities(category)
                }
                _activities.postValue(activities)
            } catch (e: Exception) {
                _activities.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }



    // Hent alle tilgjengelige byer fra aktivitetene
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

    // Filtrer aktiviteter basert på valgte byer og kategori
    private val _filteredActivities = MutableLiveData<List<Activity>>()
    val filteredActivities: LiveData<List<Activity>> get() = _filteredActivities

    fun fetchAndGroupActivitiesByCities(selectedCities: List<String>) {
        _selectedCities.value = selectedCities.toMutableList()
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(3000)

                val allActivities = activityService.getAllActivities()
                val filteredActivities = if (selectedCities.isNotEmpty()) {
                    allActivities.filter { activity ->
                        selectedCities.any { city -> activity.location.contains(city, ignoreCase = true) }
                    }
                } else {
                    allActivities
                }

                val grouped = filteredActivities.groupBy { it.category ?: "Ukjent kategori" }
                _groupedActivities.postValue(grouped)
            } catch (e: Exception) {
                _groupedActivities.postValue(emptyMap())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }






    // Nullstill filteret
    fun resetFilter() {
        _selectedCities.value = mutableListOf() // Nullstill valgte byer
        fetchAndGroupActivitiesByCities(emptyList()) // Nullstiller filtreringen
    }



    // Hent brukerens nåværende plassering
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() = liveData(Dispatchers.IO) {
        try {
            // Hent brukerens siste kjente plassering
            val location: Location? = fusedLocationClient.lastLocation.await()

            if (location != null) {
                emit(location)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("LocationError", "Feil ved henting av posisjon: ${e.message}")
            emit(null)
        }
    }

    // Hent by basert på GPS-lokasjon
    fun getCityFromLocation(location: Location?): String? {
        location?.let {
            val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            return addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea
        }
        return null
    }

    fun resetActivities() {
        _filteredActivities.postValue(emptyList()) // Nullstill aktivitetene
    }






}

