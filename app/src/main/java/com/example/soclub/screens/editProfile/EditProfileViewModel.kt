package com.example.soclub.screens.editProfile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.common.ext.isValidName
import com.example.soclub.models.UserInfo
import com.example.soclub.service.AccountService
import com.example.soclub.service.StorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val accountService: AccountService,
    private val storageService: StorageService // Inject StorageService
) : ViewModel() {

    var isSaving: MutableState<Boolean> = mutableStateOf(false)
        private set


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
                val imageUri = userInfo.imageUrl.let { Uri.parse(it) }

                uiState.value = uiState.value.copy(
                    firstname = userInfo.firstname,
                    lastname = userInfo.lastname,
                    imageUri = imageUri
                )
            } catch (e: Exception) {
                errorMessage.value = context.getString(R.string.error_message)
            } finally {
                delay(1000)
                isLoading.value = false
            }
        }
    }

    fun onNameChange(newValue: String) {
        val formattedFirstName = newValue
            .split(" ")
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercaseChar() } }
        val isNameDirty = formattedFirstName != uiState.value.firstname
        uiState.value = uiState.value.copy(firstname = formattedFirstName, isDirty = isNameDirty, firstnameError = null)
    }

    fun onLastnameChange(newValue: String) {
        val formattedLastName = newValue.replace(" ", "").replaceFirstChar { it.uppercaseChar() }
        val isLastnameDirty = formattedLastName != uiState.value.lastname
        uiState.value = uiState.value.copy(lastname = formattedLastName, isDirty = isLastnameDirty, lastnameError = null)
    }

    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(imageUri = uri, isDirty = true)
    }

    fun onSaveProfileClick(navController: NavController, context: Context) {
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
        isSaving.value = true

        val firstname = uiState.value.firstname
        val lastname = uiState.value.lastname
        val imageUri = uiState.value.imageUri

        if (imageUri != null) {
            // Check if the URI is a local content URI
            if (imageUri.toString().startsWith("content://")) {
                storageService.uploadImage(
                    imageUri = imageUri,
                    isActivity = false,
                    category = "",
                    onSuccess = { imageUrl ->
                        updateUserInfoAndNavigate(navController, firstname, lastname, imageUrl, context)
                    },
                    onError = { error ->
                        Log.e("EditProfileViewModel", "Error uploading image: ${error.message}")
                        uiState.value = uiState.value.copy(firstnameError = R.string.error_profile_creation)
                    }
                )
            } else {
                // Skip upload and use the existing URL if it's a remote URL
                updateUserInfoAndNavigate(navController, firstname, lastname, imageUri.toString(), context)
            }
        } else {
            updateUserInfoAndNavigate(navController, firstname, lastname, "", context)
        }
    }


    private fun updateUserInfoAndNavigate(navController: NavController, firstname: String, lastname: String, imageUrl: String, context: Context) {
        viewModelScope.launch {
            try {
                accountService.updateProfile(firstname = firstname, lastname = lastname, imageUrl = imageUrl) { error ->
                    isSaving.value = false
                    if (error == null) {
                        Toast.makeText(context, R.string.profile_han_been_changed, Toast.LENGTH_SHORT).show()
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
                isSaving.value = false
                uiState.value = uiState.value.copy(firstnameError = R.string.error_profile_creation)
            }
        }
    }
}
