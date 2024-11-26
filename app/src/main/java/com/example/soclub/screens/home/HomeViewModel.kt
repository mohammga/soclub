package com.example.soclub.screens.home

import android.app.Application
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.soclub.R
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
import kotlinx.coroutines.delay

/**
 * ViewModel for handling activities and user location data in the home screen.
 *
 * @param application The application context required for certain operations, such as geocoding.
 * @param activityService The service responsible for fetching and managing activity data.
 * @param fusedLocationClient The client for accessing the user's location.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val activityService: ActivityService,
    private val fusedLocationClient: FusedLocationProviderClient,
) : AndroidViewModel(application) {

    /**
     * Private state variables to manage internal ViewModel data.
     *
     * - `_userCity`: Stores the name of the city where the user is currently located.
     * - `_hasLocationPermission`: Indicates whether the app has permission to access the user's location.
     * - `_isLoading`: Indicates whether the app is currently loading data.
     * - `_activities`: Holds the list of all available activities fetched from the service.
     * - `_groupedActivities`: Contains activities grouped by category for display purposes.
     * - `_selectedCities`: Tracks the list of cities selected by the user for filtering activities.
     */
    private val _userCity = MutableLiveData<String?>()
    private val _hasLocationPermission = MutableLiveData(false)
    private val _isLoading = MutableLiveData(false)
    private val _activities = MutableLiveData<List<Activity>>()
    private val _groupedActivities = MutableLiveData<Map<String, List<Activity>>>()
    private val _selectedCities = MutableLiveData<List<String>>(emptyList())

    /**
     * Internal variables for handling ViewModel logic and maintaining state.
     *
     * - `hasLoadedNearestActivities`: Flags whether nearest activities have been loaded to avoid redundant operations.
     * - `_hasLoadedActivities`: Indicates whether activities have been successfully loaded into the ViewModel.
     * - `allActivities`: Stores all activities fetched from the service for further processing.
     * - `activitiesListener`: Listener for real-time activity updates to keep the UI synchronized.
     * - `nearestActivitiesListener`: Listener for updates to nearest activities based on the user's location.
     */
    private var hasLoadedNearestActivities = false
    private val _hasLoadedActivities = MutableLiveData(false)
    private var allActivities = listOf<Activity>()
    private var activitiesListener: ListenerRegistration? = null
    private var nearestActivitiesListener: ListenerRegistration? = null

    /**
     * Public LiveData variables for exposing data to observers (e.g., UI components).
     *
     * - `hasLocationPermission`: Observes whether the app has location permission.
     * - `isLoading`: Indicates whether the ViewModel is currently loading data.
     * - `activities`: Provides the list of all available activities.
     * - `groupedActivities`: Exposes activities grouped by category for UI consumption.
     * - `selectedCities`: Contains the list of cities selected by the user for filtering.
     */
    val hasLocationPermission: LiveData<Boolean> get() = _hasLocationPermission
    val isLoading: LiveData<Boolean> get() = _isLoading
    val activities: LiveData<List<Activity>> get() = _activities
    val groupedActivities: LiveData<Map<String, List<Activity>>> get() = _groupedActivities
    val selectedCities: LiveData<List<String>> get() = _selectedCities


    /**
     * Initializes the ViewModel by starting to listen for activity updates.
     *
     * This block is executed immediately when the ViewModel is created.
     */
    init {
        listenForActivityUpdates()
    }

    /**
     * Cleans up resources when the ViewModel is cleared.
     *
     * Removes activity and nearest activities listeners to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        activitiesListener?.remove()
        nearestActivitiesListener?.remove()
    }

    /**
     * Fetches the user's current location and updates the city and location permission status.
     *
     * This function checks for location permissions and retrieves the user's current city
     * based on their geographical location.
     */
    fun fetchUserLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasLocationPermission) {
                    Log.e("HomeViewModel", "Location permission not granted")
                    _hasLocationPermission.postValue(false)
                    return@launch
                }

                val location: Location? = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: SecurityException) {
                    Log.e("HomeViewModel", "Location permission denied during execution: ${e.message}")
                    _hasLocationPermission.postValue(false)
                    return@launch
                }
                _hasLocationPermission.postValue(location != null)
                location?.let {
                    val city = getCityFromLocation(it)
                    _userCity.postValue(city)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching location: ${e.message}")
                _hasLocationPermission.postValue(false)
            }
        }
    }

    /**
     * Starts listening for updates to activities from the service.
     *
     * Updates the `allActivities` list and applies filters when new activities are received.
     */
    private fun listenForActivityUpdates() {
        activitiesListener = activityService.listenForActivities { activities ->
            allActivities = activities
            applyFilters()
        }
    }

    /**
     * Retrieves a list of activity categories from the service.
     *
     * The "Nærme Aktiviteter" category is added to the top of the list if it's not already present.
     *
     * @return A LiveData object containing the list of categories.
     */
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


    /**
     * Retrieves a list of unique city names from the activities.
     *
     * Extracts the city name from each activity's location field.
     *
     * @return A LiveData object containing the list of cities.
     */
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


    /**
     * Combines a Firebase Timestamp and a time string into a Date object.
     *
     * @param date The Firebase Timestamp representing the date.
     * @param timeString The time string in the format "HH:mm".
     * @return A Date object representing the combined date and time, or null if the input is invalid.
     */
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

    /**
     * Retrieves the city name from a given location.
     *
     * @param location The Location object containing latitude and longitude.
     * @return The city name or sub-administrative area based on the location, or null if unavailable.
     */
    @Suppress("DEPRECATION")
    private suspend fun getCityFromLocation(location: Location?): String? {
        if (location == null) return null

        val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val city = addresses.firstOrNull()?.locality ?: addresses.firstOrNull()?.subAdminArea
                                cont.resume(city)
                            }

                            override fun onError(errorMessage: String?) {
                                Log.e("HomeViewModel", "Geocoding error: $errorMessage")
                                cont.resume(null)
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error in GeocodeListener: ${e.message}")
                    cont.resume(null)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea
                } catch (e: IOException) {
                    Log.e("HomeViewModel", "Geocoding I/O error: ${e.message}")
                    null
                } catch (e: IllegalArgumentException) {
                    Log.e("HomeViewModel", "Invalid latitude or longitude: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Unexpected error during geocoding: ${e.message}")
                    null
                }
            }
        }
    }


    /**
     * Applies filters to the list of activities based on selected cities and date constraints.
     *
     * Filters out expired activities and those not matching the selected cities.
     * Groups the activities by category and updates the LiveData for filtered activities.
     */
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

            val grouped = sortedActivities.groupBy { it.category ?:  getApplication<Application>().getString(
                R.string.unknown_category) }
            _groupedActivities.postValue(grouped)
            _hasLoadedActivities.postValue(true)
        }
    }


    /**
     * Updates the list of selected cities and reapplies activity filters.
     *
     * @param city The name of the city to add or remove.
     * @param isSelected True to add the city to the filter, false to remove it.
     */
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

    /**
     * Resets the selected cities filter and reapplies activity filters.
     *
     * Clears all selected cities and updates the filtered activities accordingly.
     */
    fun resetFilter() {
        _selectedCities.value = mutableListOf()
        applyFilters()
    }

    /**
     * Fetches and updates the nearest activities based on the user's location.
     *
     * Filters out expired activities and sorts them by proximity to the user's current location.
     * Limits the result to the top 12 nearest activities.
     */
    fun getNearestActivities() {
        if (hasLoadedNearestActivities) return

        _isLoading.value = true
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasLocationPermission) {
                    Log.e("HomeViewModel", "Location permission not granted")
                    _isLoading.postValue(false)
                    return@launch
                }

                val userLocation = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: SecurityException) {
                    Log.e("HomeViewModel", "SecurityException: Location permission denied during execution")
                    _isLoading.postValue(false)
                    return@launch
                }

                if (userLocation == null) {
                    Log.e("HomeViewModel", "User location is null")
                    _isLoading.postValue(false)
                    return@launch
                }

                nearestActivitiesListener = activityService.listenForActivities { allActivities ->
                    viewModelScope.launch {
                        val currentDateTime = Calendar.getInstance().time

                        val nonExpiredActivities = allActivities.filter { activity ->
                            val activityDateTime = combineDateAndTime(activity.date, activity.startTime)
                            activityDateTime?.after(currentDateTime) ?: false
                        }

                        val activitiesWithDistance = nonExpiredActivities.mapNotNull { activity ->
                            val location = getLocationFromAddress(activity.location)
                            location?.let {
                                val distance = userLocation.distanceTo(location)
                                activity to distance
                            }
                        }

                        val nearestActivities = activitiesWithDistance.sortedBy { it.second }.take(12).map { it.first }
                        _activities.postValue(nearestActivities)
                        _hasLoadedActivities.postValue(true)

                        val elapsedTime = System.currentTimeMillis() - startTime
                        val delayTime = 2000L - elapsedTime
                        if (delayTime > 0) {
                            delay(delayTime)
                        }

                        _isLoading.postValue(false)
                    }
                }

                hasLoadedNearestActivities = true
            } catch (e: SecurityException) {
                Log.e("HomeViewModel", "SecurityException: ${e.message}")
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _activities.postValue(emptyList())
                Log.e("HomeViewModel", "Error fetching nearest activities: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }



    /**
     * Retrieves the geographical location (latitude and longitude) from a given address.
     *
     * @param address The address string to geocode.
     * @return A Location object with latitude and longitude, or null if the address cannot be resolved.
     */
    @Suppress("DEPRECATION")
    private suspend fun getLocationFromAddress(address: String): Location? {
        val geocoder = Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
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
                            Log.e("HomeViewModel", "Geocoding error: $errorMessage")
                            cont.resume(null)
                        }
                    })
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error in GeocodeListener: ${e.message}")
                    cont.resume(null)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocationName(address, 1)
                    addresses?.firstOrNull()?.let {
                        Location("").apply {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    }
                } catch (e: IOException) {
                    Log.e("HomeViewModel", "Geocoding I/O error: ${e.message}")
                    null
                } catch (e: IllegalArgumentException) {
                    Log.e("HomeViewModel", "Invalid address input: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Unexpected error during geocoding: ${e.message}")
                    null
                }
            }
        }
    }


}
