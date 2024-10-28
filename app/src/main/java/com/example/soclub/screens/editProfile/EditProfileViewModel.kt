package com.example.soclub.screens.editProfile

import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.common.ext.isValidName
import com.example.soclub.models.UserInfo
import com.example.soclub.models.createActivity
import com.example.soclub.service.AccountService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val firstname: String = "",
    val lastname: String = "",
    val imageUrl: String = "",
    @StringRes val errorMessage: Int = 0,
    val isDirty: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    var uiState: MutableState<EditProfileState> = mutableStateOf(EditProfileState())
        private set

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userInfo: UserInfo = accountService.getUserInfo()

                val nameParts = userInfo.name.split(" ")

                val firstname = nameParts.firstOrNull() ?: ""
                val lastname = nameParts.drop(1).joinToString(" ")

                val imageUrl = userInfo.imageUrl ?: "" // Assuming `UserInfo` has `imageUrl`

                uiState.value = uiState.value.copy(
                    firstname = firstname,
                    lastname = lastname,
                    imageUrl = imageUrl,
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_profile_info)
            }
        }
    }

    fun onNameChange(newValue: String) {
        val isNameDirty = newValue != uiState.value.firstname
        uiState.value = uiState.value.copy(firstname = newValue, isDirty = isNameDirty)
    }

    fun onLastnameChange(newValue: String) {
        val isLastnameDirty = newValue != uiState.value.lastname
        uiState.value = uiState.value.copy(lastname = newValue, isDirty = isLastnameDirty)
    }

    fun onImageSelected(imagePath: String) {
        uiState.value = uiState.value.copy(imageUrl = imagePath)
    }

    private fun uploadImageToFirebase(imageUri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("User/${imageUri.lastPathSegment}")
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


    fun onSaveProfileClick(navController: NavController) {
        // Sjekk om fornavnet er tomt
        if (uiState.value.firstname.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_first_name_required)
            return
        }

        // Sjekk om fornavnet inneholder kun gyldige tegn
        if (!uiState.value.firstname.isValidName()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_firstname)
            return
        }

        // Sjekk om etternavnet er tomt
        if (uiState.value.lastname.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_last_name_required)
            return
        }

        // Sjekk om etternavnet inneholder kun gyldige tegn
        if (!uiState.value.lastname.isValidName()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_lastname)
            return
        }

        // Hvis alt er gyldig, kombiner fornavn og etternavn til fullt navn
        val fullName = "${uiState.value.firstname} ${uiState.value.lastname}"


        uploadImageToFirebase(
            Uri.parse(uiState.value.imageUrl),
            onSuccess = { imageUrl ->
                UpdateUserInfoAndNavigate(navController, fullName, imageUrl)
            },
            onError = { error ->
                Log.e("NewActivityViewModel", "Error uploading image: ${error.message}")
            }
        )

    }
    private fun UpdateUserInfoAndNavigate(navController: NavController, fullName: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                accountService.updateProfile(name = fullName, imageUrl = imageUrl) { error ->
                    if (error == null) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(2000)
                            navController.navigate("profile") {
                                popUpTo("edit_profile") { inclusive = true }
                            }
                        }
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_profile_creation)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_profile_creation)
            }
        }
    }






}

