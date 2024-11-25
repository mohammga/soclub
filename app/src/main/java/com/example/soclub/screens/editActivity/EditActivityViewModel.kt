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

/**
 * Data class representing the UI state for editing an activity.
 *
 * @property title The title of the activity.
 * @property description The description of the activity.
 * @property category The category of the activity.
 * @property location The location of the activity.
 * @property address The address of the activity.
 * @property postalCode The postal code of the activity.
 * @property maxParticipants The maximum number of participants allowed.
 * @property ageLimit The age limit for the activity.
 * @property imageUrl The URL of the activity's image.
 * @property date The date of the activity.
 * @property startTime The start time of the activity.
 * @property errorMessage The error message to be displayed, if any.
 * @property locationSuggestions List of location suggestions.
 * @property addressSuggestions List of address suggestions.
 * @property postalCodeSuggestions List of postal code suggestions.
 * @property locationConfirmed Indicates if the location is confirmed.
 * @property addressConfirmed Indicates if the address is confirmed.
 * @property titleError Error message for the title field.
 * @property descriptionError Error message for the description field.
 * @property categoryError Error message for the category field.
 * @property locationError Error message for the location field.
 * @property addressError Error message for the address field.
 * @property postalCodeError Error message for the postal code field.
 * @property maxParticipantsError Error message for the max participants field.
 * @property ageLimitError Error message for the age limit field.
 * @property dateError Error message for the date field.
 * @property startTimeError Error message for the start time field.
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
    val selectedImageUri: Uri? = null
)

/**
 * ViewModel responsible for managing the state and logic of the Edit Activity screen.
 *
 * @param activityService Service for managing activities.
 * @param accountService Service for managing user accounts.
 * @param locationService Service for fetching location data.
 * @param storageService Service for managing storage operations.
 * @param application The application context.
 */
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
            locationService.fetchMunicipalities().collect { fetchedMunicipalities ->
                municipalities = fetchedMunicipalities.toMutableList()
                uiState.value = uiState.value.copy(locationSuggestions = fetchedMunicipalities)
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
     * Updates the title field in the UI state.
     *
     * @param newValue The new value for the title.
     */
    fun onTitleChange(newValue: String) {
        uiState.value = uiState.value.copy(title = newValue, titleError = null)
        val capitalizedTitle = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(title = capitalizedTitle, titleError = null)
    }


    /**
     * Updates the description field in the UI state.
     *
     * @param newValue The new value for the description.
     */
    fun onDescriptionChange(newValue: String) {
        uiState.value = uiState.value.copy(description = newValue, descriptionError = null)
        val capitalizedDescription = newValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        uiState.value = uiState.value.copy(description = capitalizedDescription, descriptionError = null)
    }

    /**
     * Updates the selected image URI in the UI state.
     *
     * @param uri The URI of the selected image.
     */
    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
    }

    /**
     * Updates the category field in the UI state.
     *
     * @param newValue The new value for the category.
     */
    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue, categoryError = null)
    }

    /**
     * Updates the location field in the UI state and fetches location suggestions.
     *
     * @param newValue The new value for the location.
     */
    fun onLocationChange(newValue: String) {
        if (newValue.isBlank()) {
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
                uiState.value = uiState.value.copy(address = "", postalCode = "")
            } else if (newValue.length >= 2) {
                val suggestions = municipalities.filter { it.startsWith(newValue, ignoreCase = true) }
                uiState.value = uiState.value.copy(locationSuggestions = suggestions)
            } else {
                uiState.value = uiState.value.copy(locationSuggestions = emptyList())
            }
        }
    }

    /**
     * Updates the UI state when a location is selected.
     *
     * @param selectedLocation The selected location.
     */
    fun onLocationSelected(selectedLocation: String) {
        uiState.value = uiState.value.copy(
            location = selectedLocation,
            locationSuggestions = emptyList(),
            locationConfirmed = true,
            address = "",
            postalCode = "",
            addressConfirmed = false,
            addressSuggestions = emptyList()
        )
    }

    /**
     * Updates the address field in the UI state and fetches address suggestions.
     *
     * @param newValue The new value for the address.
     */
    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(
            address = newValue,
            addressConfirmed = false,
            postalCode = ""
        )

        if (newValue.isNotBlank() && uiState.value.locationConfirmed) {
            fetchAddressSuggestions(newValue)
        } else {
            uiState.value = uiState.value.copy(addressSuggestions = emptyList())
        }
    }

    /**
     * Updates the UI state when an address is selected and fetches the postal code.
     *
     * @param selectedAddress The selected address.
     */
    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(
            address = selectedAddress,
            addressConfirmed = true,
            addressSuggestions = emptyList()
        )

        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
    }

    /**
     * Fetches address suggestions based on the provided query.
     *
     * @param query The input string for which address suggestions are to be fetched.
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
     * Extracts the street name and house number from the input query.
     *
     * @param query The input string containing a street name and optionally a house number.
     * @return A pair containing the street name and the house number (if available).
     */
    private fun extractStreetAndHouseNumber(query: String): Pair<String, String?> {
        val regex = Regex("^(.*?)(\\d+)?$")
        val matchResult = regex.find(query.trim())
        val streetName = matchResult?.groups?.get(1)?.value?.trim() ?: query
        val houseNumber = matchResult?.groups?.get(2)?.value?.trim()
        return streetName to houseNumber
    }

    /**
     * Fetches the postal code for the specified address and municipality.
     *
     * @param address The address for which the postal code is to be fetched.
     * @param municipality The municipality of the address.
     */
    private fun fetchPostalCodeForAddress(address: String, municipality: String) {
        viewModelScope.launch {
            locationService.fetchPostalCodeForAddress(address, municipality).collect { postalCode ->
                if (postalCode != null) {
                    uiState.value = uiState.value.copy(postalCode = postalCode, postalCodeError = null)
                } else {
                    //uiState.value = uiState.value.copy(postalCode = "", postalCodeError = "Fant ikke postnummer")
                    uiState.value = uiState.value.copy(
                        postalCode = "",
                        postalCodeError = application.getString(R.string.error_postal_code_not_found))
                }
            }
        }
    }

    /**
     * Updates the max participants field in the UI state.
     *
     * @param newValue The new value for the max participants.
     */
    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue, maxParticipantsError = null)
    }

    /**
     * Updates the age limit field in the UI state.
     *
     * @param newValue The new value for the age limit.
     */
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

    /**
     * Updates the date field in the UI state.
     *
     * @param newValue The new value for the date.
     */
    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue, dateError = null)
    }

    /**
     * Updates the start time field in the UI state.
     *
     * @param newValue The new value for the start time.
     */
    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue, startTimeError = null)
    }

    /**
     * Validates the input fields and saves the updated activity.
     *
     * @param navController Navigation controller for navigating to other screens.
     * @param activityId The unique ID of the activity being edited.
     * @param currentCategory The current category of the activity.
     */
    fun onSaveClick(navController: NavController, activityId: String, currentCategory: String) {
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
            if (diff < 24 * 60 * 60 * 1000) { // 48 hours in milliseconds
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
                registeredUsers.forEach {
                    cancelNotificationForActivity(navController.context, activityId)
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


    /**
     * Updates the activity and navigates to the home screen.
     *
     * @param navController Navigation controller for navigating to other screens.
     * @param imageUrl The URL of the activity's image.
     * @param date The date of the activity.
     * @param startTime The start time of the activity.
     * @param creatorId The ID of the activity creator.
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

                navController.navigate("home")
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_creating_activity)
                Log.e("EditActivityViewModel", "Error updating activity: ${e.message}")
                isSaving.value = false
            }
        }
    }
}
