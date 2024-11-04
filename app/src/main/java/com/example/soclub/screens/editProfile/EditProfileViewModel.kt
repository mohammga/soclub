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
import com.example.soclub.service.AccountService
import com.example.soclub.service.StorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val firstname: String = "",
    val lastname: String = "",
    val imageUri: Uri? = null,
    @StringRes val firstnameError: Int? = null,
    @StringRes val lastnameError: Int? = null,
    val isDirty: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val storageService: StorageService // Inject StorageService
) : ViewModel() {

    var uiState: MutableState<EditProfileState> = mutableStateOf(EditProfileState())
        private set

    var isLoading: MutableState<Boolean> = mutableStateOf(true)
        private set

    var errorMessage: MutableState<String?> = mutableStateOf(null)
        private set

    fun loadUserProfile() {
        isLoading.value = true
        errorMessage.value = null  // Reset error message on reload
        viewModelScope.launch {
            try {
                val userInfo: UserInfo = accountService.getUserInfo()
                val nameParts = userInfo.name.split(" ")
                val firstname = nameParts.firstOrNull() ?: ""
                val lastname = nameParts.drop(1).joinToString(" ")
                val imageUri = userInfo.imageUrl.let { Uri.parse(it) }

                uiState.value = uiState.value.copy(
                    firstname = firstname,
                    lastname = lastname,
                    imageUri = imageUri
                )
            } catch (e: Exception) {
                errorMessage.value = "Kunne ikke laste profilinformasjon. Vennligst prøv igjen senere."
            } finally {
                delay(1000)
                isLoading.value = false
            }
        }
    }

    fun onNameChange(newValue: String) {
        val isNameDirty = newValue != uiState.value.firstname
        uiState.value = uiState.value.copy(firstname = newValue, isDirty = isNameDirty, firstnameError = null)
    }

    fun onLastnameChange(newValue: String) {
        val isLastnameDirty = newValue != uiState.value.lastname
        uiState.value = uiState.value.copy(lastname = newValue, isDirty = isLastnameDirty, lastnameError = null)
    }

    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(imageUri = uri, isDirty = true)
    }

    fun onSaveProfileClick(navController: NavController) {
        var hasError = false
        var firstnameError: Int? = null
        var lastnameError: Int? = null

        if (uiState.value.firstname.isBlank()) {
            firstnameError = R.string.error_first_name_required
            hasError = true
        } else if (!uiState.value.firstname.isValidName()) {
            firstnameError = R.string.error_invalid_firstname
            hasError = true
        }

        if (uiState.value.lastname.isBlank()) {
            lastnameError = R.string.error_last_name_required
            hasError = true
        } else if (!uiState.value.lastname.isValidName()) {
            lastnameError = R.string.error_invalid_lastname
            hasError = true
        }

        uiState.value = uiState.value.copy(
            firstnameError = firstnameError,
            lastnameError = lastnameError
        )

        if (hasError) return

        val fullName = "${uiState.value.firstname} ${uiState.value.lastname}"
        val imageUri = uiState.value.imageUri

        if (imageUri != null) {
            // Use StorageService to upload the image
            storageService.uploadImage(
                imageUri = imageUri,
                isActivity = false, // Set to false for user profile images
                category = "", // Category not needed for user images
                onSuccess = { imageUrl ->
                    updateUserInfoAndNavigate(navController, fullName, imageUrl)
                },
                onError = { error ->
                    Log.e("EditProfileViewModel", "Error uploading image: ${error.message}")
                    uiState.value = uiState.value.copy(firstnameError = R.string.error_profile_creation)
                }
            )
        } else {
            updateUserInfoAndNavigate(navController, fullName, "") // No image URL if imageUri is null
        }
    }

    private fun updateUserInfoAndNavigate(navController: NavController, fullName: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                accountService.updateProfile(name = fullName, imageUrl = imageUrl) { error ->
                    if (error == null) {
                        viewModelScope.launch {
                            delay(2000)
                            navController.navigate("profile") {
                                popUpTo("edit_profile") { inclusive = true }
                            }
                        }
                    } else {
                        uiState.value = uiState.value.copy(firstnameError = R.string.error_profile_creation)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(firstnameError = R.string.error_profile_creation)
            }
        }
    }
}
