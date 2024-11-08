package com.example.soclub.screens.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,

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

    // Hold alle aktiviteter
    private var allActivities = listOf<Activity>()

    // Listener for sanntidsoppdateringer
    private var activitiesListener: ListenerRegistration? = null

    init {
        listenForActivityUpdates()
    }

    private fun listenForActivityUpdates() {
        activitiesListener = activityService.listenForActivities { activities ->
            allActivities = activities
            applyFilters()
        }
    }

    private fun applyFilters() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentDateTime = Calendar.getInstance().time
            val nonExpiredActivities = allActivities.filter { activity ->
                val activityDateTime = combineDateAndTime(activity.date, activity.startTime)
                activityDateTime?.after(currentDateTime) ?: false
            }

            val filteredActivities = if (_selectedCities.value?.isNotEmpty() == true) {
                nonExpiredActivities.filter { activity ->
                    _selectedCities.value?.any { city ->
                        activity.location?.contains(city, ignoreCase = true) == true
                    } == true
                }
            } else {
                nonExpiredActivities
            }

            val sortedActivities = filteredActivities.sortedByDescending { activity ->
                combineDateAndTime(activity.date, activity.startTime)
            }

            val grouped = sortedActivities.groupBy { it.category ?: "Ukjent kategori" }

            _groupedActivities.postValue(grouped)
            _hasLoadedActivities.postValue(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        activitiesListener?.remove()
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

        applyFilters()
    }

    fun resetFilter() {
        _selectedCities.value = mutableListOf()
        applyFilters()
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

                val currentDateTime = Calendar.getInstance().time

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

    private fun combineDateAndTime(date: com.google.firebase.Timestamp?, timeString: String): Date? {
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
