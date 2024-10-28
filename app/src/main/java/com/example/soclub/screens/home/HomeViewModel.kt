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

    init {
        // Start med å hente brukerens plassering med en gang ViewModel opprettes
        fetchUserLocation()
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


    // Hent alle kategorier fra Firestore
    fun getCategories() = liveData(Dispatchers.IO) {
        try {
            val categories = activityService.getCategories().toMutableList()

            // Legg til den statiske kategorien "Test 22" først i listen
            categories.add(0, "Nærme Aktiviteter")

            emit(categories)
        } catch (e: Exception) {
            emit(listOf("Nærme Aktiviteter")) // Returner bare "Forslag" hvis noe går galt
        }
    }

    fun getActivities(category: String) = liveData(Dispatchers.IO) {
        try {
            val activities = if (category == "Nærme Aktiviteter") {
                // Bruk GPS-byen eller fallback-byen (f.eks. "Fredrikstad")
                val cityToFilter = _userCity.value ?: "Fredrikstad"
                // Filtrer aktiviteter basert på brukerens by
                activityService.getAllActivities().filter { activity ->
                    activity.location.contains(cityToFilter, ignoreCase = true)
                }
            } else {
                activityService.getActivities(category)
            }
            emit(activities)
        } catch (e: Exception) {
            emit(emptyList<Activity>())
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

    fun fetchAndFilterActivitiesByCities(selectedCities: List<String>, selectedCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Hent aktiviteter for den valgte kategorien
                val activities = activityService.getActivities(selectedCategory)

                // Filtrer aktivitetene basert på de valgte byene
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


