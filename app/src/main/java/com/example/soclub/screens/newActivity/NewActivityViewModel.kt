package com.example.soclub.screens.newActivity

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import android.util.Log
import androidx.annotation.StringRes
import com.example.soclub.R
import com.example.soclub.common.ext.isAgeValid
import com.example.soclub.common.ext.isValidParticipants
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    val date: String = "",
    @StringRes val errorMessage: Int = 0
)

@HiltViewModel
class NewActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService
) : ViewModel() {

    var uiState = mutableStateOf(NewActivityState())
        private set

    fun onTitleChange(newValue: String) {
        uiState.value = uiState.value.copy(title = newValue)
    }

    fun onDescriptionChange(newValue: String) {
        uiState.value = uiState.value.copy(description = newValue)
    }

    fun onImageSelected(imagePath: String) {
        uiState.value = uiState.value.copy(imageUrl = imagePath)
    }

    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue)
    }

    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(location = newValue)
    }

    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(address = newValue)
    }

    fun onPostalCodeChange(newValue: String) {
        uiState.value = uiState.value.copy(postalCode = newValue)
    }

    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue)
    }

    fun onAgeLimitChange(newValue: String) {
        uiState.value = uiState.value.copy(ageLimit = newValue)
    }

    fun onDateChange(newValue: String) {
        uiState.value = uiState.value.copy(date = newValue)
    }

    private fun uploadImageToFirebase(imageUri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${imageUri.lastPathSegment}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Log.d("NewActivityViewModel", "Image uploaded successfully")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("NewActivityViewModel", "Image URL fetched: $uri")
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NewActivityViewModel", "Error uploading image: ${exception.message}")
                onError(exception)
            }
    }

    fun onPublishClick(navController: NavController) {
        Log.d("NewActivityViewModel", "Publish button clicked")

        // Validering for tittel
        if (uiState.value.title.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_title_required)
            return
        }

        // Validering for bilde
        if (uiState.value.imageUrl.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_image_required)
            return
        }

        // Validering for beskrivelse
        if (uiState.value.description.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_description_required)
            return
        }

        // Validering for kategori
        if (uiState.value.category.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_category_required)
            return
        }

        // Validering for sted
        if (uiState.value.location.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_location_required)
            return
        }

        // Validering for adresse
        if (uiState.value.address.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_address_required)
            return
        }

        // Validering for postnummer
        if (uiState.value.postalCode.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_postal_code_required)
            return
        }

        // Validering for antall deltakere
        if (uiState.value.maxParticipants.isBlank() || uiState.value.maxParticipants.toIntOrNull() == null) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_max_participants_required)
            return
        }

        // Validering for aldersgrense (hvis den er spesifisert)
        if (uiState.value.ageLimit.isNotBlank() && uiState.value.ageLimit.toIntOrNull() == null) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_age_limit)
            return
        }


        // Validering av maks deltakere
        if (!uiState.value.maxParticipants.isValidParticipants()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_max_participants)
            return
        }

        // Validering av aldersgrense
        if (!uiState.value.ageLimit.isAgeValid()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_age_limit)
            return
        }


        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE.dd.MM.yyyy", Locale("no")))
        val dateToSend = uiState.value.date.ifBlank { currentDate }
        val combinedLocation = "${uiState.value.location}, ${uiState.value.postalCode} ${uiState.value.address}"
        val creatorId = accountService.currentUserId

        uploadImageToFirebase(
            Uri.parse(uiState.value.imageUrl),
            onSuccess = { imageUrl ->
                createActivityAndNavigate(navController, imageUrl, combinedLocation, dateToSend, creatorId)
            },
            onError = { error ->
                Log.e("NewActivityViewModel", "Error uploading image: ${error.message}")
            }
        )
    }

    private fun createActivityAndNavigate(navController: NavController, imageUrl: String, combinedLocation: String, date: String, creatorId: String) {
        viewModelScope.launch {
            try {
                val newActivity = createActivity(
                    creatorId = creatorId,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = date
                )

                activityService.createActivity(uiState.value.category, newActivity)
                Log.d("NewActivityViewModel", "Activity created successfully")

                // Navigate to home after successfully creating the activity
                navController.navigate("home")
                Log.d("NewActivityViewModel", "Navigation to home successful")
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error creating activity: ${e.message}")
            }
        }
    }
}