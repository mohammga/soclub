package com.example.soclub.screens.editActivity

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
import com.example.soclub.utils.cancelNotificationForActivity
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.soclub.common.ext.*
import java.util.Calendar

/**
 * Data class representing the UI state for editing an activity.
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
    val selectedImageUri: Uri? = null,
)

@HiltViewModel
class EditActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService,
    private val locationService: LocationService,
    private val storageService: StorageService,
    private val application: Application
) : ViewModel() {

    var isSaving = mutableStateOf(false)
        private set

    var isLoading = mutableStateOf(true)
        private set

    var uiState = mutableStateOf(EditActivityState())
        private set

    private var oldCategory: String? = null
    private var municipalities: MutableList<String> = mutableListOf()

    init {
        loadMunicipalities()
    }

    /**
     * Loads the list of municipalities and updates the UI state with location suggestions.
     */
    private fun loadMunicipalities() {
        viewModelScope.launch {
            try {
                locationService.fetchMunicipalities().collect { fetchedMunicipalities ->
                    municipalities = fetchedMunicipalities.toMutableList()
                    uiState.value = uiState.value.copy(locationSuggestions = fetchedMunicipalities)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_fetch_municipalities_failed)
                Log.e("EditActivityViewModel", "Error fetching municipalities: ${e.message}")
            }
        }
    }

    /**
     * Loads an activity for editing.
     *
     * @param category The category of the activity.
     * @param activityId The unique ID of the activity.
     */
    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            isLoading.value = true
            val startTime = System.currentTimeMillis()
            try {
                val activity = activityService.getActivityById(category, activityId)
                if (activity != null) {
                    val location = activity.location
                    val restOfAddress = activity.restOfAddress
                    val addressParts = restOfAddress.split(", ")
                    val address = addressParts.getOrNull(0)?.trim() ?: ""
                    val postalCode = addressParts.getOrNull(1)?.trim() ?: ""
                    val isLocationConfirmed = location.isNotBlank()
                    val isAddressConfirmed = address.isNotBlank()


                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingTime = 1000L - elapsedTime
                    if (remainingTime > 0) {
                        delay(remainingTime)
                    }

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
                        locationConfirmed = isLocationConfirmed,
                        addressConfirmed = isAddressConfirmed
                    )
                    oldCategory = category
                } else {
                    uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error loading activity: ${e.message}")

            }finally {
                isLoading.value = false
            }
        }
    }
    /**
     * Handles updates to the title field.
     *
     * @param newValue The new value for the title.
     */
    fun onTitleChange(newValue: String) {
        val capitalizedTitle = newValue.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        uiState.value = uiState.value.copy(title = capitalizedTitle, titleError = null)
    }

    /**
     * Handles updates to the description field.
     *
     * @param newValue The new value for the description.
     */
    fun onDescriptionChange(newValue: String) {
        val capitalizedDescription = newValue.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        uiState.value = uiState.value.copy(description = capitalizedDescription, descriptionError = null)
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

        val suggestions = if (newValue.isBlank()) {
            municipalities
        } else {
            municipalities.filter { it.startsWith(newValue, ignoreCase = true) }
        }

        uiState.value = uiState.value.copy(locationSuggestions = suggestions)
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
            try {
                locationService.fetchAddressSuggestions(
                    streetName = streetName,
                    houseNumber = houseNumber,
                    municipality = uiState.value.location
                ).collect { suggestions ->
                    uiState.value = uiState.value.copy(addressSuggestions = suggestions)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_fetch_address_suggestions_failed)
                Log.e("EditActivityViewModel", "Error fetching address suggestions: ${e.message}")
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
        uiState.value = uiState.value.copy(ageLimit = newValue, ageLimitError = null)
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
     * Handles selection of an image for the activity.
     *
     * @param uri The URI of the selected image.
     */
    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
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
        val twentyFourHoursMillis = 24 * 60 * 60 * 1000
        return combinedMillis > currentMillis && combinedMillis - currentMillis >= twentyFourHoursMillis
    }


    /**
     * Validates all input fields before saving.
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
            startTimeError = startTimeError,
        )

        return hasError
    }

    /**
     * Handles the save action, validates inputs, and updates the activity.
     *
     * @param navController The NavController for navigation.
     * @param activityId The unique ID of the activity being edited.
     * @param currentCategory The current category of the activity.
     */
    fun onSaveClick(navController: NavController, activityId: String, currentCategory: String) {
        if (validateInputs()) {
            return
        }

        isSaving.value = true

        val creatorId = accountService.currentUserId
        val timestampDate = uiState.value.date ?: Timestamp.now()
        val startTime = uiState.value.startTime

        if (uiState.value.imageUrl.isNotBlank() && uiState.value.selectedImageUri != null) {
            if (uiState.value.selectedImageUri.toString().startsWith("content://")) {
                storageService.uploadImage(
                    imageUri = uiState.value.selectedImageUri!!,
                    isActivity = true,
                    category = uiState.value.category,
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
                        isSaving.value = false
                    }
                )
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

    /**
     * Updates the activity and navigates back to the home screen.
     *
     * @param navController The NavController for navigation.
     * @param imageUrl The URL of the uploaded image.
     * @param date The selected date for the activity.
     * @param startTime The start time of the activity.
     * @param creatorId The ID of the user creating the activity.
     * @param activityId The unique ID of the activity.
     * @param currentCategory The current category of the activity.
     */
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
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = date,
                    startTime = startTime,
                )

                activityService.updateActivity(oldCategoryValue, uiState.value.category, activityId, updatedActivity)
                isSaving.value = false

                uiState.value = uiState.value.copy()

                navController.navigate("home") {
                    popUpTo("editActivity") { inclusive = true }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error updating activity: ${e.message}")
                isSaving.value = false
            }
        }
    }

    /**
     * Deletes the activity and navigates back to the home screen.
     *
     * @param navController Navigation controller for navigating to other screens.
     * @param category The category of the activity.
     * @param activityId The unique ID of the activity.
     */
    fun onDeleteClick(navController: NavController, category: String, activityId: String) {
        viewModelScope.launch {
            try {
                val registeredUsers = activityService.getRegisteredUsersForActivity(activityId)
                registeredUsers.forEach { _ ->
                    cancelNotificationForActivity(application, activityId)
                }

                activityService.deleteActivity(category, activityId)

                navController.navigate("home") {
                    popUpTo("editActivity") { inclusive = true }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error deleting activity: ${e.message}")
            }
        }
    }
}
