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
    @StringRes val errorMessage: Int = 0,
    val isDirty: Boolean = false // Ny variabel for å spore endringer
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
        val isNameDirty = newValue != uiState.value.firstname
        uiState.value = uiState.value.copy(firstname = newValue, isDirty = isNameDirty)
    }

    fun onLastnameChange(newValue: String) {
        val isLastnameDirty = newValue != uiState.value.lastname
        uiState.value = uiState.value.copy(lastname = newValue, isDirty = isLastnameDirty)
    }

    fun onSaveProfileClick(navController: NavController) {
        if (uiState.value.firstname.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_name_required)
            return
        }
        if (uiState.value.lastname.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_lastNmae_required)
            return
        }

        // Kombiner fornavn og etternavn til fullt navn
        val fullName = "${uiState.value.firstname} ${uiState.value.lastname}"

        viewModelScope.launch {
            try {

                accountService.updateProfile(
                    name = fullName,
                ) { error ->
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

