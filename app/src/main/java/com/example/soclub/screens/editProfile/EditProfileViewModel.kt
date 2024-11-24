package com.example.soclub.screens.editProfile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject



/**
 * Data class representing the state of the Edit Profile screen.
 *
 * @property firstname The user's first name.
 * @property lastname The user's last name.
 * @property imageUri The URI of the user's profile image (can be null if no image is selected).
 * @property firstnameError Resource ID for an error message related to the first name (nullable).
 * @property lastnameError Resource ID for an error message related to the last name (nullable).
 * @property isDirty Boolean indicating whether the user has made unsaved changes to the profile.
 */
data class EditProfileState(
    val firstname: String = "",
    val lastname: String = "",
    val imageUri: Uri? = null,
    @StringRes val firstnameError: Int? = null,
    @StringRes val lastnameError: Int? = null,
    val isDirty: Boolean = false
)


/**
 * A ViewModel for the Edit Profile screen, handling user interactions, data state management,
 * and communication with services for updating the user profile.
 *
 * @property context The application context for string resources and Toasts.
 * @property accountService Service to manage account-related data and operations.
 * @property storageService Service to manage image uploads.
 */

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountService: AccountService,
    private val storageService: StorageService
) : ViewModel() {
    /** Indicates whether the profile data is being saved. */
    var isSaving: MutableState<Boolean> = mutableStateOf(false)
        private set

    /**
     * The current UI state of the edit profile screen.
     */
    var uiState: MutableState<EditProfileState> = mutableStateOf(EditProfileState())
        private set

    /**
     * Indicates whether a edit profile operation is currently in progress.
     */
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
        private set

    /**
     * Holds any error messages encountered during data fetching.
     * Null if no errors occurred.
     */
    var errorMessage: MutableState<String?> = mutableStateOf(null)
        private set


    /**
     * Loads the user's profile information and updates the UI state.
     * Displays an error message if the operation fails.
     */
    fun loadUserProfile() {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val userInfo: UserInfo = accountService.getUserInfo()
                val imageUri = userInfo.imageUrl.let { Uri.parse(it) }

                uiState.value = uiState.value.copy(
                    firstname = userInfo.firstname,
                    lastname = userInfo.lastname,
                    imageUri = if (userInfo.imageUrl.isNotBlank()) Uri.parse(userInfo.imageUrl) else null
                )
            } catch (e: Exception) {
                errorMessage.value = context.getString(R.string.error_message)
            } finally {
                delay(1000)
                isLoading.value = false
            }
        }
    }

    /**
     * Handles updates to the first name input, ensuring formatting and validation.
     *
     * @param newValue The new value of the first name.
     */

    fun onNameChange(newValue: String) {
        val formattedFirstName = newValue
            .split(" ")
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercaseChar() } }
        val isNameDirty = formattedFirstName != uiState.value.firstname
        uiState.value = uiState.value.copy(firstname = formattedFirstName, isDirty = isNameDirty, firstnameError = null)
    }


    /**
     * Handles updates to the last name input, ensuring formatting and validation.
     *
     * @param newValue The new value of the last name.
     */
    fun onLastnameChange(newValue: String) {
        val formattedLastName = newValue.replace(" ", "").replaceFirstChar { it.uppercaseChar() }
        val isLastnameDirty = formattedLastName != uiState.value.lastname
        uiState.value = uiState.value.copy(lastname = formattedLastName, isDirty = isLastnameDirty, lastnameError = null)
    }

    /**
     * Handles selection of an image for the activity.
     *
     * @param uri The URI of the selected image.
     */
    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(imageUri = uri, isDirty = true)
    }

    /**
     * Saves the profile changes, uploading the image if necessary, and navigates back to the profile screen.
     *
     * @param navController Controller for managing navigation.
     * @param context Context for displaying error messages and navigating.
     */
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
                updateUserInfoAndNavigate(navController, firstname, lastname, imageUri.toString(), context)
            }
        } else {
            updateUserInfoAndNavigate(navController, firstname, lastname, "", context)
        }
    }

    /**
     * Updates the user's profile information and navigates to the profile screen.
     *
     * @param navController Controller for managing navigation.
     * @param firstname The updated first name.
     * @param lastname The updated last name.
     * @param imageUrl The URL of the uploaded profile image.
     * @param context Context for displaying error messages and navigation.
     */
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
