package com.example.soclub.screens.editActivity

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.delay


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

    private val _isLoading = mutableStateOf(false)
    val isLoading: MutableState<Boolean> get() = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: MutableState<String?> get() = _errorMessage

    private var oldCategory: String? = null


    fun loadActivity(category: String, activityId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            delay(1000)
            try {
                val activity = activityService.getActivityById(category, activityId)
                if (activity != null) {
                    val addressParts = activity.location.split(", ")
                    uiState.value = uiState.value.copy(
                        title = activity.title,
                        description = activity.description,
                        address = addressParts.getOrNull(0)?.trim() ?: "",
                        postalCode = addressParts.getOrNull(1)?.split(" ")?.getOrNull(0) ?: "",
                        location = addressParts.getOrNull(1)?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                        maxParticipants = activity.maxParticipants.toString(),
                        ageLimit = activity.ageGroup.toString(),
                        imageUrl = activity.imageUrl,
                        date = activity.date,
                        startTime = activity.startTime,
                        category = category
                    )
                    oldCategory = category
                } else {
                    _errorMessage.value = "Fant ikke aktiviteten."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Feil ved lasting av aktivitet: ${e.message}"
            } finally {
                _isLoading.value = false
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

//    private fun uploadImageToFirebase(
//        imageUri: Uri,
//        onSuccess: (String) -> Unit,
//        onError: (Exception) -> Unit
//    ) {
//        val storageRef = FirebaseStorage.getInstance().reference.child("images/${imageUri.lastPathSegment}")
//        storageRef.putFile(imageUri)
//            .addOnSuccessListener {
//                Log.d("EditActivityViewModel", "Image uploaded successfully")
//                storageRef.downloadUrl.addOnSuccessListener { uri ->
//                    Log.d("EditActivityViewModel", "Image URL fetched: $uri")
//                    onSuccess(uri.toString())
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("EditActivityViewModel", "Error uploading image: ${exception.message}")
//                onError(exception)
//            }
//    }
//
fun onDeleteClick(navController: NavController, category: String, activityId: String) {
    _isLoading.value = true
    _errorMessage.value = null
    viewModelScope.launch {
        try {
            activityService.deleteActivity(category, activityId)
            navController.navigate("adsScreen") // Naviger til com.example.soclub.screens.ads.AdsScreen etter sletting
        } catch (e: Exception) {
            _errorMessage.value = "Feil ved sletting av aktivitet: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}

//}

// Funksjon for å lagre endringer med lastestatus og feilmelding
fun onSaveClick(navController: NavController, currentCategory: String, activityId: String) {
    if (uiState.value.title.isBlank()) {
        _errorMessage.value = "Tittel er påkrevd."
        return
    }
    if (uiState.value.category.isBlank()) {
        _errorMessage.value = "Kategori er påkrevd."
        return
    }
    _isLoading.value = true
    _errorMessage.value = null

    val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode} ${uiState.value.location}"
    val creatorId = accountService.currentUserId

    if (uiState.value.imageUrl.isNotBlank()) {
        uploadImageToFirebase(
            Uri.parse(uiState.value.imageUrl),
            onSuccess = { imageUrl ->
                handleUpdate(navController, imageUrl, combinedLocation, currentCategory, activityId, creatorId)
            },
            onError = { error ->
                _errorMessage.value = "Feil ved opplasting av bilde: ${error.message}"
                _isLoading.value = false
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
            val updatedActivity = createActivity(
                createdAt = Timestamp.now(),
                lastUpdated = Timestamp.now(),
                creatorId = creatorId,
                title = uiState.value.title,
                description = uiState.value.description,
                location = combinedLocation,
                maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                imageUrl = imageUrl,
                date = uiState.value.date ?: Timestamp.now(),
                startTime = uiState.value.startTime
            )

            val oldCategory = oldCategory ?: currentCategory
            activityService.updateActivity(oldCategory, uiState.value.category, activityId, updatedActivity)
            navController.navigate("home")
        } catch (e: Exception) {
            _errorMessage.value = "Feil ved oppdatering av aktivitet: ${e.message}"
        } finally {
            _isLoading.value = false
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

