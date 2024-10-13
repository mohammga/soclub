package com.example.soclub.screens.newActivity

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import android.util.Log  // For logging
import com.example.soclub.R
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
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
    val errorMessage: Int? = null  // For displaying error messages
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

        // Check for title and category
        if (uiState.value.title.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_title_required)
            return
        }

        if (uiState.value.category.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_category_required)
            return
        }

        // Set default date to current day if not chosen by user
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE.dd.MM.yyyy", Locale("no")))
        val dateToSend = if (uiState.value.date.isBlank()) currentDate else uiState.value.date

        val combinedLocation = "${uiState.value.location}, ${uiState.value.postalCode} ${uiState.value.address}"

        if (uiState.value.imageUrl.isNotBlank()) {
            uploadImageToFirebase(
                Uri.parse(uiState.value.imageUrl),
                onSuccess = { imageUrl ->
                    createActivityAndNavigate(navController, imageUrl, combinedLocation, dateToSend)
                },
                onError = { error ->
                    Log.e("NewActivityViewModel", "Error uploading image: ${error.message}")
                }
            )
        } else {
            createActivityAndNavigate(navController, "", combinedLocation, dateToSend)
        }
    }

    private fun createActivityAndNavigate(navController: NavController, imageUrl: String, combinedLocation: String, date: String) {
        viewModelScope.launch {
            try {
                Log.d("NewActivityViewModel", "Creating activity with imageUrl: $imageUrl and location: $combinedLocation")
                val newActivity = createActivity(
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

                navController.navigate("home")
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error creating activity: ${e.message}")
            }
        }
    }


    private fun createActivityAndNavigate(navController: NavController, imageUrl: String, combinedLocation: String) {
        viewModelScope.launch {
            try {
                Log.d("NewActivityViewModel", "Creating activity with imageUrl: $imageUrl and location: $combinedLocation")
                val newActivity = createActivity(
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = uiState.value.date
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

