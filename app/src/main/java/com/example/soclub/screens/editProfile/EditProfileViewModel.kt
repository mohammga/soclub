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
    val name: String = "",
    val email: String = "",
    @StringRes val errorMessage: Int = 0
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    var uiState: MutableState<EditProfileState> = mutableStateOf(EditProfileState())
        private set

    // Funksjon for å laste inn brukerens profilinformasjon
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userInfo: UserInfo = accountService.getUserInfo()
                uiState.value = uiState.value.copy(
                    name = userInfo.name,   // Sett brukerens navn
                    email = userInfo.email  // Sett brukerens e-post
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_log_in)
            }
        }
    }

    fun onNameChange(newValue: String) {
        uiState.value = uiState.value.copy(name = newValue)
    }

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onSaveProfileClick(navController: NavController) {
        if (uiState.value.name.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_name_required)
            return
        }
        if (uiState.value.email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email)
            return
        }

        viewModelScope.launch {
            try {
                accountService.updateProfile(uiState.value.name, uiState.value.email) { error ->
                    if (error == null) {
                        // Naviger til profilsiden etter vellykket oppdatering
                        navController.navigate("profile") {
                            popUpTo("edit_profile") { inclusive = true }
                        }
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_account_creation)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_account_creation)
            }
        }
    }

}
