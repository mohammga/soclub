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
import java.util.Calendar
import com.google.firebase.Timestamp
import java.util.Date



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

    private var hasLoadedNearestActivities = false

    private val _hasLoadedActivities = MutableLiveData(false)
    val hasLoadedActivities: LiveData<Boolean> get() = _hasLoadedActivities




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

    fun getCities() = liveData(Dispatchers.IO) {
        try {
            val activities = activityService.getAllActivities()
            val cities = activities.map { activity ->
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

        // Oppdater aktivitetene basert på gjeldende filter
        if (currentCities.isEmpty()) {
            // Nullstill filteret hvis ingen byer er valgt
            fetchAndGroupActivitiesByCities(emptyList())
        } else {
            fetchAndGroupActivitiesByCities(currentCities)
        }
    }

    fun fetchAndGroupActivitiesByCities(selectedCities: List<String>) {
        _selectedCities.value = selectedCities.toMutableList()
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allActivities = activityService.getAllActivities()


                val currentDateTime = Calendar.getInstance().time


                val nonExpiredActivities = allActivities.filter { activity ->
                    val activityDateTime = combineDateAndTime(activity.date, activity.startTime)
                    activityDateTime?.after(currentDateTime) ?: false
                }

                val filteredActivities = if (selectedCities.isNotEmpty()) {
                    nonExpiredActivities.filter { activity ->
                        selectedCities.any { city -> activity.location?.contains(city, ignoreCase = true) == true }
                    }
                } else {
                    nonExpiredActivities
                }

                val sortedActivities = filteredActivities.sortedByDescending { activity ->
                    combineDateAndTime(activity.date, activity.startTime)
                }

                val grouped = sortedActivities.groupBy { it.category ?: "Ukjent kategori" }
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

    private fun getCityFromLocation(location: Location?): String? {
        location?.let {
            val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            val city = addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea

            Log.d("HomeViewModel", "Geokoderesultat for posisjon (${it.latitude}, ${it.longitude}): $city")

            return city
        }
        return null
    }


    @SuppressLint("MissingPermission")
    fun getNearestActivities() {
        if (hasLoadedNearestActivities) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userLocation = fusedLocationClient.lastLocation.await() ?: return@launch
                val allActivities = activityService.getAllActivities()

                // Get current date and time
                val currentDateTime = Calendar.getInstance().time

                // Filter out expired activities
                val nonExpiredActivities = allActivities.filter { activity ->
                    val activityDateTime = combineDateAndTime(activity.date, activity.startTime)
                    activityDateTime?.after(currentDateTime) ?: false
                }

                val activitiesWithDistance = nonExpiredActivities.mapNotNull { activity ->
                    val location = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
                        .getFromLocationName(activity.location ?: "", 1)
                        ?.firstOrNull()
                        ?.let {
                            Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                        }

                    location?.let {
                        val distance = userLocation.distanceTo(location)
                        activity to distance
                    }
                }

                // Sort and take the 10 nearest activities
                val nearestActivities = activitiesWithDistance.sortedBy { it.second }.take(10).map { it.first }

                _activities.postValue(nearestActivities)
                hasLoadedNearestActivities = true
                _hasLoadedActivities.postValue(true)
            } catch (e: Exception) {
                _activities.postValue(emptyList())
                Log.e("HomeViewModel", "Error fetching nearest activities: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    private fun combineDateAndTime(date: Timestamp?, timeString: String): Date? {
        if (date == null || timeString.isEmpty()) return null

        return try {
            val calendar = Calendar.getInstance()
            calendar.time = date.toDate()

            val timeParts = timeString.split(":")
            if (timeParts.size != 2) return null

            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.time
        } catch (e: Exception) {
            null
        }
    }
}

