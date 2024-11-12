package com.example.soclub.screens.editActivity

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.models.CreateActivity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import com.example.soclub.service.LocationService
import com.example.soclub.service.StorageService
import com.example.soclub.utils.cancelNotificationForActivity
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class EditActivityState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val address: String = "",
    val postalCode: String = "",
    val maxParticipants: String = "",
    val ageLimit: String = "",
    val imageUrl: String = "",
    val date: Timestamp? = Timestamp.now(),
    val startTime: String = "",
    val errorMessage: Int? = null,
    val locationSuggestions: List<String> = emptyList(),
    val addressSuggestions: List<String> = emptyList(),
    val postalCodeSuggestions: List<String> = emptyList(),
    val locationConfirmed: Boolean = false,
    val addressConfirmed: Boolean = false,
    // Feilmeldinger
    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val locationError: String? = null,
    val addressError: String? = null,
    val postalCodeError: String? = null,
    val maxParticipantsError: String? = null,
    val ageLimitError: String? = null,
    val dateError: String? = null,
    val startTimeError: String? = null,
    val selectedImageUri: Uri? = null
)

@HiltViewModel
class EditActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService,
    private val locationService: LocationService,
    private val storageService: StorageService
) : ViewModel() {

    var uiState = mutableStateOf(EditActivityState())
        private set

    private var oldCategory: String? = null
    private var municipalities: MutableList<String> = mutableListOf()

    init {
        loadMunicipalities()
    }

    // Funksjon for å laste inn kommuner
    private fun loadMunicipalities() {
        viewModelScope.launch {
            locationService.fetchMunicipalities().collect { fetchedMunicipalities ->
                municipalities = fetchedMunicipalities.toMutableList()
                uiState.value = uiState.value.copy(locationSuggestions = fetchedMunicipalities)
            }
        }
    }

    // Funksjon for å laste inn aktivitet
    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            try {
                val activity = activityService.getActivityById(category, activityId)
                if (activity != null) {
                    val location = activity.location ?: ""
                    val restOfAddress = activity.restOfAddress ?: ""
                    val addressParts = restOfAddress.split(", ")
                    val address = addressParts.getOrNull(0)?.trim() ?: ""
                    val postalCode = addressParts.getOrNull(1)?.trim() ?: ""

                    // Sikre at `locationConfirmed` er satt basert på datatilgjengelighet
                    val isLocationConfirmed = location.isNotBlank()
                    val isAddressConfirmed = address.isNotBlank()

                    uiState.value = uiState.value.copy(
                        title = activity.title,
                        description = activity.description,
                        address = address,
                        postalCode = postalCode,
                        location = location,
                        maxParticipants = activity.maxParticipants.toString(),
                        ageLimit = activity.ageGroup.toString(),
                        imageUrl = activity.imageUrl,
                        date = activity.date,
                        startTime = activity.startTime,
                        category = category,
                        selectedImageUri = if (activity.imageUrl.isNotEmpty()) Uri.parse(activity.imageUrl) else null,
                        locationConfirmed = isLocationConfirmed, // Bekreft sted
                        addressConfirmed = isAddressConfirmed // Bekreft adresse
                    )
                    oldCategory = category
                } else {
                    uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error loading activity: ${e.message}")
            }
        }
    }
    
    // Function to handle title change with first letter capitalized
    fun onTitleChange(newValue: String) {
        uiState.value = uiState.value.copy(title = newValue, titleError = null)
        val capitalizedTitle = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(title = capitalizedTitle, titleError = null)
    }

    // Function to handle description change with first letter capitalized
    fun onDescriptionChange(newValue: String) {
        uiState.value = uiState.value.copy(description = newValue, descriptionError = null)
        val capitalizedDescription = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(description = capitalizedDescription, descriptionError = null)
    }

    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
    }

    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue, categoryError = null)
    }

    fun onLocationChange(newValue: String) {
        if (newValue.isBlank()) {
            // Tilbakestill feltene hvis sted er tømt
            uiState.value = uiState.value.copy(
                location = newValue,
                address = "",
                postalCode = "",
                locationConfirmed = false,
                addressConfirmed = false,
                locationSuggestions = emptyList()
            )
        } else {
            val matchesSuggestion = uiState.value.locationSuggestions.contains(newValue)
            uiState.value = uiState.value.copy(
                location = newValue,
                locationConfirmed = matchesSuggestion,
                address = if (matchesSuggestion) "" else uiState.value.address,
                postalCode = if (matchesSuggestion) "" else uiState.value.postalCode,
                addressConfirmed = false
            )

            if (matchesSuggestion) {
                // Når stedet matcher, tilbakestill avhengige felter
                uiState.value = uiState.value.copy(address = "", postalCode = "")
            } else if (newValue.length >= 2) {
                val suggestions = municipalities.filter { it.startsWith(newValue, ignoreCase = true) }
                uiState.value = uiState.value.copy(locationSuggestions = suggestions)
            } else {
                uiState.value = uiState.value.copy(locationSuggestions = emptyList())
            }
        }
    }

    fun onLocationSelected(selectedLocation: String) {
        uiState.value = uiState.value.copy(
            location = selectedLocation,
            locationSuggestions = emptyList(),
            locationConfirmed = true,
            // Tilbakestill adresse og postnummer når et nytt sted er valgt
            address = "",
            postalCode = "",
            addressConfirmed = false,
            addressSuggestions = emptyList()
        )
    }

    fun onAddressChange(newValue: String) {
        // Tilbakestill postnummer og adressebekreftelse hvis brukeren skriver manuelt
        uiState.value = uiState.value.copy(
            address = newValue,
            addressConfirmed = false,
            postalCode = ""
        )

        // Vis adresseforslag hvis sted er bekreftet
        if (newValue.isNotBlank() && uiState.value.locationConfirmed) {
            fetchAddressSuggestions(newValue)
        } else {
            uiState.value = uiState.value.copy(addressSuggestions = emptyList())
        }
    }

    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(
            address = selectedAddress,
            addressConfirmed = true,
            addressSuggestions = emptyList()
        )

        // Hent postnummer kun når en adresse er valgt fra listen
        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
    }

    private fun fetchAddressSuggestions(query: String) {
        val (streetName, houseNumber) = extractStreetAndHouseNumber(query)

        if (!uiState.value.locationConfirmed) {
            return
        }

        viewModelScope.launch {
            locationService.fetchAddressSuggestions(
                streetName = streetName,
                houseNumber = houseNumber,
                municipality = uiState.value.location
            ).collect { suggestions ->
                uiState.value = uiState.value.copy(addressSuggestions = suggestions)
            }
        }
    }

    private fun extractStreetAndHouseNumber(query: String): Pair<String, String?> {
        val regex = Regex("^(.*?)(\\d+)?$")
        val matchResult = regex.find(query.trim())
        val streetName = matchResult?.groups?.get(1)?.value?.trim() ?: query
        val houseNumber = matchResult?.groups?.get(2)?.value?.trim()
        return streetName to houseNumber
    }

    private fun fetchPostalCodeForAddress(address: String, municipality: String) {
        viewModelScope.launch {
            locationService.fetchPostalCodeForAddress(address, municipality).collect { postalCode ->
                if (postalCode != null) {
                    uiState.value = uiState.value.copy(postalCode = postalCode, postalCodeError = null)
                } else {
                    uiState.value = uiState.value.copy(postalCode = "", postalCodeError = "Fant ikke postnummer")
                }
            }
        }
    }

    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue, maxParticipantsError = null)
    }

    fun onAgeLimitChange(newValue: String) {
        val age = newValue.toIntOrNull()
        if (age != null && age > 100) {
            uiState.value = uiState.value.copy(
                errorMessage = R.string.error_age_limit_exceeded,
                ageLimitError = "Aldersgrensen kan ikke overstige 100"
            )
        } else {
            uiState.value = uiState.value.copy(
                ageLimit = newValue,
                errorMessage = null,
                ageLimitError = null
            )
        }
    }

    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue, dateError = null)
    }

    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue, startTimeError = null)
    }

    fun onSaveClick(navController: NavController, activityId: String, currentCategory: String) {
        // Validering
        var hasError = false
        var titleError: String? = null
        var descriptionError: String? = null
        var categoryError: String? = null
        var locationError: String? = null
        var addressError: String? = null
        var postalCodeError: String? = null
        var maxParticipantsError: String? = null
        var ageLimitError: String? = null
        var dateError: String? = null
        var startTimeError: String? = null

        if (uiState.value.title.isBlank()) {
            titleError = "Du må fylle inn tittel"
            hasError = true
        }
        if (uiState.value.description.isBlank()) {
            descriptionError = "Du må fylle inn beskrivelse"
            hasError = true
        }
        if (uiState.value.category.isBlank()) {
            categoryError = "Du må velge kategori"
            hasError = true
        }
        if (uiState.value.location.isBlank()) {
            locationError = "Du må velge sted"
            hasError = true
        }
        if (uiState.value.address.isBlank()) {
            addressError = "Du må velge adresse"
            hasError = true
        }
        if (uiState.value.postalCode.isBlank()) {
            postalCodeError = "Postnummer er påkrevd"
            hasError = true
        }
        if (uiState.value.maxParticipants.isBlank()) {
            maxParticipantsError = "Du må fylle inn maks antall deltakere"
            hasError = true
        } else if (uiState.value.maxParticipants.toIntOrNull() == null) {
            maxParticipantsError = "Må være et tall"
            hasError = true
        }
        if (uiState.value.ageLimit.isBlank()) {
            ageLimitError = "Du må fylle inn aldersgrense"
            hasError = true
        } else if (uiState.value.ageLimit.toIntOrNull() == null) {
            ageLimitError = "Må være et tall"
            hasError = true
        }

        val selectedDate = uiState.value.date
        if (selectedDate == null) {
            dateError = "Du må velge dato"
            hasError = true
        } else {
            val currentTimeMillis = System.currentTimeMillis()
            val selectedDateMillis = selectedDate.toDate().time
            val diff = selectedDateMillis - currentTimeMillis
            if (diff < 48 * 60 * 60 * 1000) { // 48 hours in milliseconds
                dateError = "Datoen må være minst 48 timer fra nå"
                hasError = true
            }
        }

        if (uiState.value.startTime.isBlank()) {
            startTimeError = "Du må velge starttidspunkt"
            hasError = true
        }

        // Oppdater UI-state med feilmeldinger
        uiState.value = uiState.value.copy(
            titleError = titleError,
            descriptionError = descriptionError,
            categoryError = categoryError,
            locationError = locationError,
            addressError = addressError,
            postalCodeError = postalCodeError,
            maxParticipantsError = maxParticipantsError,
            ageLimitError = ageLimitError,
            dateError = dateError,
            startTimeError = startTimeError
        )

        if (hasError) {
            return
        }

        val creatorId = accountService.currentUserId
        val timestampDate = uiState.value.date ?: Timestamp.now()
        val startTime = uiState.value.startTime

        if (uiState.value.imageUrl.isNotBlank() && uiState.value.selectedImageUri != null) {
            // Sjekk om URI er en lokal content URI
            if (uiState.value.selectedImageUri.toString().startsWith("content://")) {
                storageService.uploadImage(
                    imageUri = uiState.value.selectedImageUri!!,
                    isActivity = true,  // Sett til true for aktivitetsbilder
                    category = uiState.value.category, // Passér aktivitetskategorien
                    onSuccess = { imageUrl ->
                        updateActivityAndNavigate(
                            navController,
                            imageUrl,
                            timestampDate,
                            startTime,
                            creatorId,
                            activityId,
                            currentCategory
                        )
                    },
                    onError = { error ->
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_image_upload_failed)
                        Log.e("EditActivityViewModel", "Error uploading image: ${error.message}")
                    }
                )
            } else {
                // Hopp over opplasting og bruk eksisterende URL hvis det er en ekstern URL
                updateActivityAndNavigate(
                    navController,
                    uiState.value.imageUrl,
                    timestampDate,
                    startTime,
                    creatorId,
                    activityId,
                    currentCategory
                )
            }
        } else {
            updateActivityAndNavigate(
                navController,
                uiState.value.imageUrl,
                timestampDate,
                startTime,
                creatorId,
                activityId,
                currentCategory
            )
        }
    }


    fun onDeleteClick(navController: NavController, category: String, activityId: String) {
        viewModelScope.launch {
            try {
                // Før sletting, hent alle påmeldte brukere og kanseller deres varsler
                val registeredUsers = activityService.getRegisteredUsersForActivity(activityId)
                registeredUsers.forEach { userId ->
                    cancelNotificationForActivity(navController.context, userId, activityId)
                }

                // Slett deretter selve aktiviteten fra databasen
                activityService.deleteActivity(category, activityId)

                // Naviger tilbake til hjem eller vis en bekreftelse
                navController.navigate("home") {
                    popUpTo("editActivity") { inclusive = true }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error deleting activity: ${e.message}")
            }
        }
    }



    private fun updateActivityAndNavigate(
        navController: NavController,
        imageUrl: String,
        date: Timestamp,
        startTime: String,
        creatorId: String,
        activityId: String,
        currentCategory: String
    ) {
        val oldCategoryValue = oldCategory ?: currentCategory

        viewModelScope.launch {
            try {
                val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode} ${uiState.value.location}".trim()
                val updatedActivity = CreateActivity(
                    createdAt = Timestamp.now(),
                    lastUpdated = Timestamp.now(),
                    creatorId = creatorId,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation, // Kombinert adresse
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = date,
                    startTime = startTime,
                )

                activityService.updateActivity(oldCategoryValue, uiState.value.category, activityId, updatedActivity)

                navController.navigate("home")
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error updating activity: ${e.message}")
            }
        }
    }
}
