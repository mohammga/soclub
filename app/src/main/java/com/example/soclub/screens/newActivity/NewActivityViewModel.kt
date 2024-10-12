package com.example.soclub.screens.newActivity

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    val errorMessage: Int? = null  // For å vise eventuelle feilmeldinger
)


@HiltViewModel
class NewActivityViewModel @Inject constructor(
    private val activityService: ActivityService
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

    fun onPublishClick(navController: NavController) {
        if (uiState.value.title.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_title_required)
            return
        }

        if (uiState.value.category.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_category_required)
            return
        }

        val combinedLocation = "${uiState.value.location}, ${uiState.value.postalCode} ${uiState.value.address}"

        if (uiState.value.imageUrl.isNotBlank()) {
            uploadImageToFirebase(
                Uri.parse(uiState.value.imageUrl),
                onSuccess = { imageUrl ->
                    createActivityAndNavigate(navController, imageUrl, combinedLocation)
                },
                onError = { error ->
                    
                }
            )
        } else {
            createActivityAndNavigate(navController, "", combinedLocation)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${imageUri.lastPathSegment}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Plasser createActivityAndNavigate funksjonen her inne
    fun createActivityAndNavigate(navController: NavController, imageUrl: String, combinedLocation: String) {
        viewModelScope.launch {
            val newActivity = Activity(
                title = uiState.value.title,
                description = uiState.value.description,
                location = combinedLocation,
                maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                imageUrl = imageUrl,
                date = uiState.value.date
                // Ikke send id eller restOfAddress hvis de ikke er nødvendige
            )

            // Lagre aktivitet til databasen via ActivityService
            activityService.createActivity(uiState.value.category, newActivity)

            // Etter lagring, naviger tilbake til home-skjermen
            navController.navigate("home")
        }
    }
}
