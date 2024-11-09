package com.example.soclub.screens.newActivity

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
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class NewActivityState(
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
    val locationConfirmed: Boolean = false, // Flag to confirm location selection
    val addressConfirmed: Boolean = false,  // Flag to confirm address selection
    // Error messages
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
class NewActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService,
    private val locationService: LocationService,
    private val storageService: StorageService
) : ViewModel() {

    var uiState = mutableStateOf(NewActivityState())
        private set

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

    // Funksjon for å håndtere endring av tittel
    fun onTitleChange(newValue: String) {
        uiState.value = uiState.value.copy(title = newValue, titleError = null)
    }

    // Funksjon for å håndtere endring av beskrivelse
    fun onDescriptionChange(newValue: String) {
        uiState.value = uiState.value.copy(description = newValue, descriptionError = null)
    }

    // Funksjon for å håndtere bildevalg
    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
    }

    // Funksjon for å håndtere kategoriendring
    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue, categoryError = null)
    }

    // Funksjon for å håndtere endring av sted og vise forslag
    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(
            location = newValue,
            address = "",          // Tilbakestill adresse når sted endres
            postalCode = "",      // Tilbakestill postnummer når sted endres
            locationConfirmed = false, // Tilbakestill bekreftelsesflagget
            addressConfirmed = false,  // Tilbakestill adressebekreftelsesflagget
            locationError = null,
            addressSuggestions = emptyList()
        )

        // Vis forslag bare når to eller flere tegn er angitt
        if (newValue.length >= 2) {
            val suggestions = municipalities.filter { it.startsWith(newValue, ignoreCase = true) }
            uiState.value = uiState.value.copy(locationSuggestions = suggestions)
        } else {
            uiState.value = uiState.value.copy(locationSuggestions = emptyList())
        }
    }

    // Funksjon for å bekrefte stedsvalg fra dropdown
    fun onLocationSelected(selectedLocation: String) {
        uiState.value = uiState.value.copy(
            location = selectedLocation,
            locationSuggestions = emptyList(),
            locationConfirmed = true  // Marker sted som bekreftet
        )
    }

    // Funksjon for å håndtere endring av adresse og vise forslag
    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(
            address = newValue,
            postalCode = "",          // Tilbakestill postnummer når adresse endres
            addressConfirmed = false, // Tilbakestill bekreftelsesflagget
            addressError = null
        )

        if (newValue.isNotBlank()) {
            fetchAddressSuggestions(newValue)
        } else {
            uiState.value = uiState.value.copy(addressSuggestions = emptyList())
        }
    }

    // Funksjon for å bekrefte adressevalg fra dropdown
    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(
            address = selectedAddress,
            addressSuggestions = emptyList(),
            addressConfirmed = true  // Marker adresse som bekreftet
        )
        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
    }

    // Hent adresseforslag, håndterer husnummeruttrekking
    private fun fetchAddressSuggestions(query: String) {
        val (streetName, houseNumber) = extractStreetAndHouseNumber(query)

        // Hent forslag bare hvis sted er bekreftet
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

    // Hjelpefunksjon for å trekke ut gatenavn og husnummer
    private fun extractStreetAndHouseNumber(query: String): Pair<String, String?> {
        val regex = Regex("^(.*?)(\\d+)?$")
        val matchResult = regex.find(query.trim())
        val streetName = matchResult?.groups?.get(1)?.value?.trim() ?: query
        val houseNumber = matchResult?.groups?.get(2)?.value?.trim()
        return streetName to houseNumber
    }

    // Hent postnummer for den valgte adressen
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

    // Funksjon for å håndtere endring av maks antall deltakere
    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue, maxParticipantsError = null)
    }

    // Funksjon for å håndtere aldersgrenseendring og validere
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

    // Funksjon for å håndtere datoendring
    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue, dateError = null)
    }

    // Funksjon for å håndtere starttidspunktendring
    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue, startTimeError = null)
    }

    // Funksjon for å håndtere publiseringshandling
    fun onPublishClick(navController: NavController) {
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
            if (diff < 48 * 60 * 60 * 1000) {
                dateError = "Datoen må være minst 48 timer fra nå"
                hasError = true
            }
        }

        if (uiState.value.startTime.isBlank()) {
            startTimeError = "Du må velge starttidspunkt"
            hasError = true
        }

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

        val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode} ${uiState.value.location}"
        val creatorId = accountService.currentUserId
        val timestampDate = uiState.value.date ?: Timestamp.now()
        val startTime = uiState.value.startTime

        if (uiState.value.imageUrl.isNotBlank()) {
            storageService.uploadImage(
                imageUri = Uri.parse(uiState.value.imageUrl),
                isActivity = true,  // Set to true for activity images
                category = uiState.value.category, // Pass the activity category
                onSuccess = { imageUrl ->
                    createActivityAndNavigate(navController, imageUrl, combinedLocation, timestampDate, startTime, creatorId)
                },
                onError = { error ->
                    uiState.value = uiState.value.copy(errorMessage = R.string.error_image_upload_failed)
                    Log.e("NewActivityViewModel", "Error uploading image: ${error.message}")
                }
            )
        } else {
            createActivityAndNavigate(navController, "", combinedLocation, timestampDate, startTime, creatorId)
        }
    }

    private fun createActivityAndNavigate(
        navController: NavController,
        imageUrl: String,
        combinedLocation: String,
        date: Timestamp,
        startTime: String,
        creatorId: String
    ) {
        viewModelScope.launch {
            try {
                val newActivity = CreateActivity(
                    createdAt = Timestamp.now(),
                    lastUpdated = Timestamp.now(),
                    creatorId = creatorId,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = date,
                    startTime = startTime,
                )
                activityService.createActivity(uiState.value.category, newActivity)
                navController.navigate("home")
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("NewActivityViewModel", "Error creating activity: ${e.message}")
            }
        }
    }
}
