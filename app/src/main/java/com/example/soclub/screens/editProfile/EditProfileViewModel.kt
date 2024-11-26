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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
    private val storageService: StorageService
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
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val userInfo: UserInfo = accountService.getUserInfo()
                val imageUri = if (userInfo.imageUrl.isNotBlank()) Uri.parse(userInfo.imageUrl) else null

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

        viewModelScope.launch {
            try {
                val imageUrl = if (imageUri != null && imageUri.toString().startsWith("content://")) {
                    storageService.uploadImageSuspend(imageUri)
                } else {
                    imageUri?.toString().orEmpty()
                }
                accountService.updateProfile(firstname, lastname, imageUrl)

                Toast.makeText(context, R.string.profile_han_been_changed, Toast.LENGTH_SHORT).show()

                delay(2000)
                navController.navigate("profile") {
                    popUpTo("edit_profile") { inclusive = true }
                }
                isSaving.value = false
            } catch (e: Exception) {
                isSaving.value = false
                Log.e("EditProfileViewModel", "Error updating profile: ${e.message}", e)
                Toast.makeText(context, R.string.error_profile_creation, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Extension function to convert the callback-based uploadImage to a suspending function.
     * You need to implement this in your StorageService.
     */
    private suspend fun StorageService.uploadImageSuspend(imageUri: Uri): String = suspendCoroutine { cont ->
        uploadImage(
            imageUri = imageUri,
            isActivity = false,
            category = "",
            onSuccess = { imageUrl ->
                cont.resume(imageUrl)
            },
            onError = { error ->
                cont.resumeWithException(error)
            }
        )
    }
}
