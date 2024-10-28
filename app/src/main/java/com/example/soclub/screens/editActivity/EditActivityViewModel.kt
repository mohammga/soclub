package com.example.soclub.screens.editActivity

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    val date: Timestamp? = null,
    val startTime: String = "",
    val errorMessage: Int? = null
)

@HiltViewModel
class EditActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService
) : ViewModel() {

    var uiState = mutableStateOf(EditActivityState())
        private set


    private var oldCategory: String? = null


    fun loadActivity(category: String, activityId: String) {
        viewModelScope.launch {
            try {
                val activity = activityService.getActivityById(category, activityId)
                if (activity != null) {
                    // Fyll inn UI-staten med eksisterende data
                    uiState.value = uiState.value.copy(
                        title = activity.title,
                        description = activity.description,
                        //SPLITE LOCATION TIL Å KUN HENTE BY
                        location = activity.location.split(",").last().trim(),
                        address = activity.location.split(",").first().trim(),
                        postalCode = activity.location.split(", ").getOrNull(1) ?: "",
                        maxParticipants = activity.maxParticipants.toString(),
                        ageLimit = activity.ageGroup.toString(),
                        imageUrl = activity.imageUrl,
                        date = activity.date,  // Bruker Timestamp for dato
                        startTime = activity.startTime,
                        category = category
                    )
                    oldCategory = category
                }
            } catch (e: Exception) {
                Log.e("EditActivityViewModel", "Error loading activity: ${e.message}")
            }
        }
    }

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

    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue)
    }

    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue)
    }

    private fun uploadImageToFirebase(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${imageUri.lastPathSegment}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Log.d("EditActivityViewModel", "Image uploaded successfully")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("EditActivityViewModel", "Image URL fetched: $uri")
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditActivityViewModel", "Error uploading image: ${exception.message}")
                onError(exception)
            }
    }

    fun onDeleteClick(navController: NavController, category: String, activityId: String) {
        viewModelScope.launch {
            try {
                activityService.deleteActivity(category, activityId)
                navController.navigate("adsScreen") // Naviger til AdsScreen etter sletting
            } catch (e: Exception) {
                Log.e("EditActivityViewModel", "Error deleting activity: ${e.message}")
            }
        }
    }


    fun onSaveClick(navController: NavController, currentCategory: String, activityId: String) {
        Log.d("EditActivityViewModel", "Save button clicked")

        if (uiState.value.title.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_title_required)
            return
        }

        if (uiState.value.category.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_category_required)
            return
        }

        val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode} ${uiState.value.location}"
        val creatorId = accountService.currentUserId

        if (uiState.value.imageUrl.isNotBlank()) {
            uploadImageToFirebase(
                Uri.parse(uiState.value.imageUrl),
                onSuccess = { imageUrl ->
                    handleUpdate(navController, imageUrl, combinedLocation, currentCategory, activityId, creatorId)
                },
                onError = { error ->
                    Log.e("EditActivityViewModel", "Error uploading image: ${error.message}")
                }
            )
        } else {
            handleUpdate(navController, "", combinedLocation, currentCategory, activityId, creatorId)
        }
    }

    private fun handleUpdate(
        navController: NavController,
        imageUrl: String,
        combinedLocation: String,
        currentCategory: String,
        activityId: String,
        creatorId: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("EditActivityViewModel", "Updating activity with imageUrl: $imageUrl")

                val updatedActivity = createActivity(
                    createdAt = Timestamp.now(),  // Assuming updated creation time
                    lastUpdated = Timestamp.now(),  // Timestamp for last update
                    creatorId = creatorId,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = uiState.value.date ?: Timestamp.now(),  // Bruker Timestamp for dato
                    startTime = uiState.value.startTime
                )

                // Oppdater aktiviteten i Firebase
                val oldCategory = oldCategory ?: currentCategory
                activityService.updateActivity(oldCategory, uiState.value.category, activityId, updatedActivity)

                Log.d("EditActivityViewModel", "Activity updated successfully")
                navController.navigate("home")
            } catch (e: Exception) {
                Log.e("EditActivityViewModel", "Error updating activity: ${e.message}")
            }
        }
    }
}



private fun uploadImageToFirebase(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${imageUri.lastPathSegment}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            }
            .addOnFailureListener { onError(it) }
    }


