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
import com.example.soclub.common.ext.*
import java.util.Calendar

/**
 * Data class representing the UI state for creating a new activity.
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
    val date: Timestamp? = null,
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
     */
    fun onTitleChange(newValue: String) {
        val capitalizedTitle = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(title = capitalizedTitle, titleError = null)
    }

    /**
     * Handles updates to the description field.
     */
    fun onDescriptionChange(newValue: String) {
        val capitalizedDescription = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(description = capitalizedDescription, descriptionError = null)
    }

    /**
     * Handles selection of an image for the activity.
     */
    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
    }

    /**
     * Handles updates to the category field.
     */
    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue, categoryError = null)
    }

    /**
     * Handles updates to the location field and triggers suggestions.
     */
    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(
            location = newValue,
            locationConfirmed = false,
            address = "",
            postalCode = "",
            addressSuggestions = emptyList(),
            addressConfirmed = false
        )

        val suggestions = if (newValue.isBlank()) {
            municipalities
        } else {
            municipalities.filter { it.startsWith(newValue, ignoreCase = true) }
        }

        uiState.value = uiState.value.copy(locationSuggestions = suggestions)
    }

    fun onLocationSelected(selectedLocation: String) {
        uiState.value = uiState.value.copy(
            location = selectedLocation,
            locationSuggestions = emptyList(),
            locationConfirmed = true,
        )
    }


    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(
            address = newValue,
            postalCode = "",
            addressConfirmed = false
        )

        if (newValue.isNotBlank()) {
            fetchAddressSuggestions(newValue)
        } else {
            uiState.value = uiState.value.copy(addressSuggestions = emptyList())
        }
    }



    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(
            address = selectedAddress,
            addressSuggestions = emptyList(),
            addressConfirmed = true,
            addressError = null
        )
        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
    }

    /**
     * Fetches suggestions for the address based on the query.
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
                val filteredSuggestions = suggestions.filter {
                    it.contains(query, ignoreCase = true)
                }

                uiState.value = uiState.value.copy(addressSuggestions = filteredSuggestions)
            }
        }
    }


    /**
     * Extracts the street name and house number from an address query.
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
     */
    private fun fetchPostalCodeForAddress(address: String, municipality: String) {
        viewModelScope.launch {
            try {
                locationService.fetchPostalCodeForAddress(address, municipality).collect { postalCode ->
                    if (postalCode != null) {
                        uiState.value = uiState.value.copy(postalCode = postalCode, postalCodeError = null)
                    } else {
                        uiState.value = uiState.value.copy(postalCode = "", postalCodeError = application.getString(R.string.error_postal_code_not_found))
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_fetch_postal_code_failed)
                Log.e("EditActivityViewModel", "Error fetching postal code: ${e.message}")
            }
        }
    }

    /**
     * Handles updates to the max participants field.
     */
    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue, maxParticipantsError = null)
    }

    /**
     * Handles updates to the age limit field.
     */
    fun onAgeLimitChange(newValue: String) {
        uiState.value = uiState.value.copy(ageLimit = newValue, ageLimitError = null)
    }

    /**
     * Handles updates to the date field.
     */
    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue, dateError = null)
    }

    /**
     * Handles updates to the start time field.
     */
    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue, startTimeError = null)
    }

    private fun combineDateAndTime(date: Timestamp?, time: String?): Long? {
        if (date == null || time.isNullOrBlank()) return null

        val dateMillis = date.toDate().time
        val timeParts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (timeParts.size != 2) return null

        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, timeParts[0])
            set(Calendar.MINUTE, timeParts[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }


    private fun isValidCombinedDateTime(date: Timestamp?, time: String?): Boolean {
        val combinedMillis = combineDateAndTime(date, time) ?: return false
        val currentMillis = System.currentTimeMillis()
        return combinedMillis > currentMillis && combinedMillis - currentMillis >= 24 * 60 * 60 * 1000
    }

    /**
     * Validates all input fields before publishing.
     *
     * @return true if there are validation errors, false otherwise.
     */
    private fun validateInputs(): Boolean {
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
            titleError = application.getString(R.string.title_must_be_filled_error)
            hasError = true
        } else if (!uiState.value.title.isValidTitle()) {
            titleError = application.getString(R.string.error_title_invalid_format)
            hasError = true
        }

        if (uiState.value.description.isBlank()) {
            descriptionError = application.getString(R.string.error_description_required)
            hasError = true
        } else if (!uiState.value.description.isValidDescription()) {
            descriptionError = application.getString(R.string.error_description_required)
            hasError = true
        }

        if (uiState.value.category.isBlank()) {
            categoryError = application.getString(R.string.error_category_required)
            hasError = true
        } else if (!uiState.value.category.isValidCategory()) {
            categoryError = application.getString(R.string.error_category_invalid_format)
            hasError = true
        }

        if (uiState.value.location.isBlank()) {
            locationError = application.getString(R.string.error_location_required)
            hasError = true
        }

        if (uiState.value.address.isBlank()) {
            addressError = application.getString(R.string.error_address_required)
            hasError = true
        }

        if (uiState.value.postalCode.isBlank()) {
            postalCodeError = application.getString(R.string.error_postal_code_required)
            hasError = true
        }

        if (uiState.value.maxParticipants.isBlank()) {
            maxParticipantsError = application.getString(R.string.error_max_participants_required)
            hasError = true
        } else if (!uiState.value.maxParticipants.isValidMaxParticipants()) {
            maxParticipantsError = application.getString(R.string.error_invalid_max_participants)
            hasError = true
        }

        if (uiState.value.ageLimit.isBlank()) {
            ageLimitError = application.getString(R.string.ageLimit_must_be_filled_error)
            hasError = true
        } else if (!uiState.value.ageLimit.isValidAgeLimit()) {
            ageLimitError = application.getString(R.string.error_invalid_age_limit)
            hasError = true
        }

        val selectedDateMillis = uiState.value.date?.toDate()?.time ?: 0L
        val isDateSet = selectedDateMillis != 0L
        val isTimeSet = uiState.value.startTime.isNotBlank()

        if (!isDateSet) {
            dateError = application.getString(R.string.error_date_required)
            hasError = true
        }

        if (!isTimeSet) {
            startTimeError = application.getString(R.string.error_start_time_required)
            hasError = true
        }

        if (isDateSet && isTimeSet) {
            if (!isValidCombinedDateTime(uiState.value.date, uiState.value.startTime)) {
                val combinedErrorMsg = application.getString(R.string.invalid_combined_datetime)
                dateError = combinedErrorMsg
                startTimeError = combinedErrorMsg
                hasError = true
            }
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

        return hasError
    }


    /**
     * Handles the publish action, validates inputs, and publishes the activity.
     *
     * @param navController The NavController for navigation.
     */
    fun onPublishClick(navController: NavController) {
        if (validateInputs()) {
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
        } else {
            createActivityAndNavigate(navController, "", combinedLocation, timestampDate, startTime, creatorId)
        }
    }

    /**
     * Creates a new activity and navigates back to the home screen.
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
