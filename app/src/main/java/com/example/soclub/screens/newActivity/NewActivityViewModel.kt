package com.example.soclub.screens.newActivity

import android.app.Application
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
import javax.inject.Inject


/**
 * Data class representing the UI state for creating a new activity.
 *
 * @property title The title of the activity.
 * @property description The description of the activity.
 * @property category The category of the activity.
 * @property location The location of the activity.
 * @property address The address of the activity.
 * @property postalCode The postal code of the activity.
 * @property maxParticipants The maximum number of participants.
 * @property ageLimit The age limit for the activity.
 * @property imageUrl The URL of the activity's image.
 * @property date The selected date for the activity.
 * @property startTime The start time of the activity.
 * @property errorMessage The error message resource ID, if any.
 * @property locationSuggestions Suggestions for the location field.
 * @property addressSuggestions Suggestions for the address field.
 * @property postalCodeSuggestions Suggestions for the postal code field.
 * @property locationConfirmed Flag indicating if the location is confirmed.
 * @property addressConfirmed Flag indicating if the address is confirmed.
 * @property titleError Validation error for the title field.
 * @property descriptionError Validation error for the description field.
 * @property categoryError Validation error for the category field.
 * @property locationError Validation error for the location field.
 * @property addressError Validation error for the address field.
 * @property postalCodeError Validation error for the postal code field.
 * @property maxParticipantsError Validation error for the max participants field.
 * @property ageLimitError Validation error for the age limit field.
 * @property dateError Validation error for the date field.
 * @property startTimeError Validation error for the start time field.
 * @property selectedImageUri The URI of the selected image.
 */

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
    val locationConfirmed: Boolean = false,
    val addressConfirmed: Boolean = false,
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

/**
 * ViewModel for managing the logic of creating a new activity.
 *
 * @param activityService The service for managing activities.
 * @param accountService The service for managing user accounts.
 * @param locationService The service for fetching location details.
 * @param storageService The service for uploading images.
 * @param application The application context for accessing resources.
 */
@HiltViewModel
class NewActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService,
    private val locationService: LocationService,
    private val storageService: StorageService,
    private val application: Application
) : ViewModel() {

    var isPublishing = mutableStateOf(false)
        private set


    var uiState = mutableStateOf(NewActivityState())
        private set

    private var municipalities: MutableList<String> = mutableListOf()

    init {
        loadMunicipalities()
    }

    /**
     * Loads the list of municipalities for location suggestions.
     */
    private fun loadMunicipalities() {
        viewModelScope.launch {
            locationService.fetchMunicipalities().collect { fetchedMunicipalities ->
                municipalities = fetchedMunicipalities.toMutableList()
                uiState.value = uiState.value.copy(locationSuggestions = fetchedMunicipalities)
            }
        }
    }

    /**
     * Handles updates to the title field.
     *
     * @param newValue The new value for the title.
     */
    fun onTitleChange(newValue: String) {
        val capitalizedTitle = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(title = capitalizedTitle, titleError = null)
    }

    /**
     * Handles updates to the description field.
     *
     * @param newValue The new value for the description.
     */
    fun onDescriptionChange(newValue: String) {
        val capitalizedDescription = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(description = capitalizedDescription, descriptionError = null)
    }

    /**
     * Handles selection of an image for the activity.
     *
     * @param uri The URI of the selected image.
     */
    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
    }

    /**
     * Handles updates to the category field.
     *
     * @param newValue The new value for the category.
     */
    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue, categoryError = null)
    }

    /**
     * Handles updates to the location field and triggers suggestions.
     *
     * @param newValue The new value for the location.
     */
    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(
            location = newValue,
            address = "",
            postalCode = "",
            locationConfirmed = false,
            addressConfirmed = false,
            locationError = null,
            addressSuggestions = emptyList()
        )

        if (newValue.length >= 2) {
            val suggestions = municipalities.filter { it.startsWith(newValue, ignoreCase = true) }
            uiState.value = uiState.value.copy(locationSuggestions = suggestions)
        } else {
            uiState.value = uiState.value.copy(locationSuggestions = emptyList())
        }
    }

    /**
     * Handles selection of a location suggestion.
     *
     * @param selectedLocation The selected location from suggestions.
     */
    fun onLocationSelected(selectedLocation: String) {
        uiState.value = uiState.value.copy(
            location = selectedLocation,
            locationSuggestions = emptyList(),
            locationConfirmed = true
        )
    }

    /**
     * Handles updates to the address field and triggers suggestions.
     *
     * @param newValue The new value for the address.
     */
    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(
            address = newValue,
            postalCode = "",
            addressConfirmed = false,
            addressError = null
        )

        if (newValue.isNotBlank()) {
            fetchAddressSuggestions(newValue)
        } else {
            uiState.value = uiState.value.copy(addressSuggestions = emptyList())
        }
    }

    /**
     * Handles selection of an address suggestion.
     *
     * @param selectedAddress The selected address from suggestions.
     */
    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(
            address = selectedAddress,
            addressSuggestions = emptyList(),
            addressConfirmed = true
        )
        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
    }

    /**
     * Fetches suggestions for the address based on the query.
     *
     * @param query The address query string.
     */
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

    /**
     * Extracts the street name and house number from an address query.
     *
     * @param query The address query string.
     * @return A pair containing the street name and house number.
     */
    private fun extractStreetAndHouseNumber(query: String): Pair<String, String?> {
        val regex = Regex("^(.*?)(\\d+)?$")
        val matchResult = regex.find(query.trim())
        val streetName = matchResult?.groups?.get(1)?.value?.trim() ?: query
        val houseNumber = matchResult?.groups?.get(2)?.value?.trim()
        return streetName to houseNumber
    }

    /**
     * Fetches the postal code for the given address and municipality.
     *
     * @param address The address to fetch the postal code for.
     * @param municipality The municipality of the address.
     */
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

    /**
     * Handles updates to the max participants field.
     *
     * @param newValue The new value for max participants.
     */
    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue, maxParticipantsError = null)
    }

    /**
     * Handles updates to the age limit field.
     *
     * @param newValue The new value for the age limit.
     */
    fun onAgeLimitChange(newValue: String) {
        val age = newValue.toIntOrNull()
        if (age != null && age > 100) {
            uiState.value = uiState.value.copy(
                errorMessage = R.string.error_age_limit_exceeded,
                ageLimitError = application.getString(R.string.age_limt_100)
            )
        } else {
            uiState.value = uiState.value.copy(
                ageLimit = newValue,
                errorMessage = null,
                ageLimitError = null
            )
        }
    }

    /**
     * Handles updates to the date field.
     *
     * @param newValue The new value for the date.
     */
    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue, dateError = null)
    }

    /**
     * Handles updates to the start time field.
     *
     * @param newValue The new value for the start time.
     */
    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue, startTimeError = null)
    }

    /**
     * Handles the publish action, validates inputs, and publishes the activity.
     *
     * @param navController The NavController for navigation.
     */
    fun onPublishClick(navController: NavController) {
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
            titleError = application.getString(R.string.you_must_fyll_the_titel)
            hasError = true
        }
        if (uiState.value.description.isBlank()) {
            descriptionError = application.getString(R.string.description_must_be_filled_error)
            hasError = true
        }
        if (uiState.value.category.isBlank()) {
            categoryError = application.getString(R.string.you_most_select_category)
            hasError = true
        }
        if (uiState.value.location.isBlank()) {
            locationError = application.getString(R.string.you_most_select_location)
            hasError = true
        }
        if (uiState.value.address.isBlank()) {
            addressError = application.getString(R.string.you_most_select_address)
            hasError = true
        }
        if (uiState.value.postalCode.isBlank()) {
            postalCodeError = application.getString(R.string.you_most_select_postalCode)
            hasError = true
        }
        if (uiState.value.maxParticipants.isBlank()) {
            maxParticipantsError = application.getString(R.string.maxParticipants_must_be_filled_error)
            hasError = true
        } else if (uiState.value.maxParticipants.toIntOrNull() == null) {
            maxParticipantsError = application.getString(R.string.most_ny_a_nummber)
            hasError = true
        }
        if (uiState.value.ageLimit.isBlank()) {
            ageLimitError = application.getString(R.string.ageLimit_must_be_filled_error)
            hasError = true
        } else if (uiState.value.ageLimit.toIntOrNull() == null) {
            ageLimitError = application.getString(R.string.most_ny_a_nummber)
            hasError = true
        }

        val selectedDate = uiState.value.date
        if (selectedDate == null) {
            dateError = application.getString(R.string.you_most_select_date)
            hasError = true
        } else {
            val currentTimeMillis = System.currentTimeMillis()
            val selectedDateMillis = selectedDate.toDate().time
            val diff = selectedDateMillis - currentTimeMillis
            if (diff < 24 * 60 * 60 * 1000) {
                dateError = application.getString(R.string.most_by_24_h)
                hasError = true
            }
        }

        if (uiState.value.startTime.isBlank()) {
            startTimeError = application.getString(R.string.you_most_select_start_time)
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

        isPublishing.value = true

        val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode} ${uiState.value.location}"
        val creatorId = accountService.currentUserId
        val timestampDate = uiState.value.date ?: Timestamp.now()
        val startTime = uiState.value.startTime

        if (uiState.value.imageUrl.isNotBlank()) {
            storageService.uploadImage(
                imageUri = Uri.parse(uiState.value.imageUrl),
                isActivity = true,
                category = uiState.value.category,
                onSuccess = { imageUrl ->
                    createActivityAndNavigate(navController, imageUrl, combinedLocation, timestampDate, startTime, creatorId)
                },
                onError = { error ->
                    uiState.value = uiState.value.copy(errorMessage = R.string.error_image_upload_failed)
                    Log.e("NewActivityViewModel", "Error uploading image: ${error.message}")
                    isPublishing.value = false
                }
            )
        }
        else {
            createActivityAndNavigate(navController, "", combinedLocation, timestampDate, startTime, creatorId)
        }
    }

    /**
     * Creates a new activity and navigates back to the home screen.
     *
     * @param navController The NavController for navigation.
     * @param imageUrl The URL of the uploaded image.
     * @param combinedLocation The combined location string.
     * @param date The selected date for the activity.
     * @param startTime The start time of the activity.
     * @param creatorId The ID of the user creating the activity.
     */
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
                isPublishing.value = false
                uiState.value = NewActivityState()
                navController.navigate("home")
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("NewActivityViewModel", "Error creating activity: ${e.message}")
                isPublishing.value = false
            }
        }
    }
}
