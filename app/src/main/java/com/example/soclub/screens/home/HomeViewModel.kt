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
        fetchAndGroupActivitiesByCities(emptyList())  // Hent aktiviteter uten filter ved oppstart
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("HomeViewModel", "Henter brukerens posisjon...")
                val location: Location? = fusedLocationClient.lastLocation.await()

                if (location != null) {
                    Log.d("HomeViewModel", "Posisjon hentet: ${location.latitude}, ${location.longitude}")
                    val city = getCityFromLocation(location)
                    _userCity.postValue(city)
                    Log.d("HomeViewModel", "Brukerens by: $city")
                } else {
                    Log.w("HomeViewModel", "Posisjon er null")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Feil ved henting av posisjon: ${e.message}")
            }
        }
    }

    fun getCategories(): LiveData<List<String>> = liveData {
        try {
            val categories = activityService.getCategories().toMutableList()
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
            try {
                val activities = if (category == "Nærme Aktiviteter") {
                    val cityToFilter = _userCity.value ?: "Fredrikstad"
                    Log.d("HomeViewModel", "Filtrerer aktiviteter for by: $cityToFilter")
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

    fun getCities() = liveData(Dispatchers.IO) {
        try {
            val activities = activityService.getAllActivities()
            val cities = activities.mapNotNull { activity ->
                val fullLocation = activity.location ?: "Ukjent"
                fullLocation.substringAfterLast(" ")
            }.distinct()
            emit(cities)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun updateSelectedCities(city: String, isSelected: Boolean) {
        val currentCities = _selectedCities.value?.toMutableList() ?: mutableListOf()
        if (isSelected) {
            if (!currentCities.contains(city)) currentCities.add(city)
        } else {
            currentCities.remove(city)
        }
        _selectedCities.value = currentCities
    }

    fun fetchAndGroupActivitiesByCities(selectedCities: List<String>) {
        _selectedCities.value = selectedCities.toMutableList()
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
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

    fun resetFilter() {
        _selectedCities.value = mutableListOf()
        fetchAndGroupActivitiesByCities(emptyList())
    }

    fun getCityFromLocation(location: Location?): String? {
        location?.let {
            val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            val city = addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea

            Log.d("HomeViewModel", "Geokoderesultat for posisjon (${it.latitude}, ${it.longitude}): $city")

            return city
        }
        return null
    }

    fun resetActivities() {
        _activities.postValue(emptyList())
    }

    @SuppressLint("MissingPermission")
    fun getNearestActivities() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Hent brukerens posisjon
                val userLocation = fusedLocationClient.lastLocation.await() ?: return@launch

                // Hent alle aktiviteter og kalkuler avstanden fra brukerens posisjon
                val activitiesWithDistance = activityService.getAllActivities().mapNotNull { activity ->
                    val location = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
                        .getFromLocationName(activity.location, 1)
                        ?.firstOrNull()
                        ?.let {
                            Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                        }
                    location?.let {
                        val distance = userLocation.distanceTo(location) // Avstand i meter
                        activity to distance
                    }
                }

                // Sorter aktiviteter etter avstand
                val nearestActivities = activitiesWithDistance.sortedBy { it.second }.map { it.first }

                // Oppdater aktiviteter med de nærmeste først
                _activities.postValue(nearestActivities)
            } catch (e: Exception) {
                _activities.postValue(emptyList())
                Log.e("HomeViewModel", "Feil ved henting av nærmeste aktiviteter: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }






}

