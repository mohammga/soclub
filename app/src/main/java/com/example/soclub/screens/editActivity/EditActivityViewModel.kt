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
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
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
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",   // New field for end time
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
                    uiState.value = uiState.value.copy(
                        title = activity.title,
                        description = activity.description,
                        location = activity.location,
                        address = activity.restOfAddress,
                        postalCode = activity.location.split(", ").getOrNull(1) ?: "",
                        maxParticipants = activity.maxParticipants.toString(),
                        ageLimit = activity.ageGroup.toString(),
                        imageUrl = activity.imageUrl,
                        date = activity.date?.toDate().toString(),
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

    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue)
    }

    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue)
    }


    fun onSaveClick(navController: NavController, currentCategory: String, activityId: String) {  // activityId må sendes inn her
        if (uiState.value.title.isBlank() || uiState.value.category.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_title_required)
            return
        }

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateAndTime = "${uiState.value.date} ${uiState.value.startTime}"
        val timestampDate = try {
            val localDateTime = LocalDateTime.parse(dateAndTime, dateFormatter)
            Timestamp(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))  // Bruk Timestamp(Date)
        } catch (e: Exception) {
            Timestamp.now()  // Fallback hvis datoformatet er feil
        }

        val combinedLocation = "${uiState.value.location}, ${uiState.value.postalCode} ${uiState.value.address}"
        val creatorId = accountService.currentUserId

        if (uiState.value.imageUrl.isNotBlank()) {
            uploadImageToFirebase(
                Uri.parse(uiState.value.imageUrl),
                onSuccess = { imageUrl ->
                    handleUpdate(navController, imageUrl, combinedLocation, timestampDate, creatorId, activityId)  // Pass activityId here
                },
                onError = { error ->
                    Log.e("EditActivityViewModel", "Error uploading image: ${error.message}")
                }
            )
        } else {
            handleUpdate(navController, "", combinedLocation, timestampDate, creatorId, activityId)  // Pass activityId here
        }
    }


    private fun handleUpdate(
        navController: NavController,
        imageUrl: String,
        combinedLocation: String,
        date: Timestamp,
        creatorId: String,
        activityId: String // activityId må sendes hit
    ) {
        viewModelScope.launch {
            try {
                val updatedActivity = createActivity(
                    creatorId = creatorId,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = date,
                    startTime = uiState.value.startTime,

                )

                val oldCategory = oldCategory ?: uiState.value.category
                activityService.updateActivity(oldCategory, uiState.value.category, activityId, updatedActivity)

                navController.navigate("home")
            } catch (e: Exception) {
                Log.e("EditActivityViewModel", "Error updating activity: ${e.message}")
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
}

