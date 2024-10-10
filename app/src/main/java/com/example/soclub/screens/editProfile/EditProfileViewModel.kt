package com.example.soclub.screens.editProfile

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.models.UserInfo
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val firstname: String = "",
    val lastname: String = "",
    @StringRes val errorMessage: Int = 0
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

                uiState.value = uiState.value.copy(
                    firstname = firstname,
                    lastname = lastname
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_profile_info)
            }
        }
    }

    fun onNameChange(newValue: String) {
        uiState.value = uiState.value.copy(firstname = newValue)
    }

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(lastname = newValue)
    }

    fun onSaveProfileClick(navController: NavController) {
        if (uiState.value.firstname.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_name_required)
            return
        }
        if (uiState.value.lastname.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email)
            return
        }

        // Kombiner fornavn og etternavn til fullt navn
        val fullName = "${uiState.value.firstname} ${uiState.value.lastname}"

        viewModelScope.launch {
            try {
                // Oppdater Firestore med nytt navn og e-post
                accountService.updateProfile(
                    name = fullName,
                ) { error ->
                    if (error == null) {
                        // Naviger tilbake til profilsiden ved vellykket oppdatering
                        navController.navigate("profile") {
                            popUpTo("edit_profile") { inclusive = true }
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
