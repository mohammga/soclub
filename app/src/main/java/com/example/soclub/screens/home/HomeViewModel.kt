package com.example.soclub.screens.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val activityService: ActivityService,
    private val fusedLocationClient: FusedLocationProviderClient,
) : AndroidViewModel(application) {


    private val _userCity = MutableLiveData<String?>()
    val userCity: LiveData<String?> get() = _userCity

    // LiveData for å holde styr på GPS-tillatelsen
    private val _hasLocationPermission = MutableLiveData(false)
    val hasLocationPermission: LiveData<Boolean> get() = _hasLocationPermission

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

    private var allActivities = listOf<Activity>()
    private var activitiesListener: ListenerRegistration? = null
    private var nearestActivitiesListener: ListenerRegistration? = null



    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val location: Location? = fusedLocationClient.lastLocation.await()
                _hasLocationPermission.postValue(location != null) // Setter tillatelse basert på lokasjonsdata
                location?.let {
                    val city = getCityFromLocation(it)
                    _userCity.postValue(city)
                }
            } catch (e: SecurityException) {
                Log.e("HomeViewModel", "Location permission not granted: ${e.message}")
                _hasLocationPermission.postValue(false) // Setter tillatelse til false hvis tilgang ikke er gitt
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching location: ${e.message}")
                _hasLocationPermission.postValue(false)
            }
        }
    }


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
                        activity.location.contains(city, ignoreCase = true)
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
        nearestActivitiesListener?.remove()
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
                activity.location.substringAfterLast(" ")
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

    private suspend fun getCityFromLocation(location: Location?): String? {
        location ?: return null

        val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For API level 33 and above, use GeocodeListener
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            cont.resume(addresses.firstOrNull()?.locality ?: addresses.firstOrNull()?.subAdminArea)
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(null)
                        }
                    }
                )
            }
        } else {
            // For lower API levels, use deprecated getFromLocation in IO dispatcher
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea
                } catch (e: IOException) {
                    // Handle IOException, such as network issues
                    null
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun getNearestActivities() {
        if (hasLoadedNearestActivities) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userLocation = fusedLocationClient.lastLocation.await() ?: return@launch

                nearestActivitiesListener = activityService.listenForActivities { allActivities ->
                    viewModelScope.launch {
                        val currentDateTime = Calendar.getInstance().time

                        val nonExpiredActivities = allActivities.filter { activity ->
                            val activityDateTime = combineDateAndTime(activity.date, activity.startTime)
                            activityDateTime?.after(currentDateTime) ?: false
                        }

                        val activitiesWithDistance = nonExpiredActivities.mapNotNull { activity ->
                            val location = getLocationFromAddress(activity.location) // Call inside coroutine
                            location?.let {
                                val distance = userLocation.distanceTo(location)
                                activity to distance
                            }
                        }

                        val nearestActivities = activitiesWithDistance.sortedBy { it.second }.take(10).map { it.first }
                        _activities.postValue(nearestActivities)
                        _hasLoadedActivities.postValue(true)
                    }
                }

                hasLoadedNearestActivities = true
            } catch (e: Exception) {
                _activities.postValue(emptyList())
                Log.e("HomeViewModel", "Error fetching nearest activities: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun getLocationFromAddress(address: String): Location? {
        val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val firstAddress = addresses.firstOrNull()
                        val location = firstAddress?.let {
                            Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                        }
                        cont.resume(location)
                    }

                    override fun onError(errorMessage: String?) {
                        cont.resume(null)
                    }
                })
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(address, 1)
                    addresses?.firstOrNull()?.let {
                        Location("").apply {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    }
                } catch (e: IOException) {
                    Log.e("HomeViewModel", "Geocoder I/O error: ${e.message}")
                    null
                }
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
            calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        } catch (e: Exception) {
            null
        }
    }
}
