package com.example.soclub.screens.editProfile

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
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

    private val name get() = uiState.value.name
    private val email get() = uiState.value.email

    fun onNameChange(newValue: String) {
        uiState.value = uiState.value.copy(name = newValue)
    }

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onSaveProfileClick() {
        if (name.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_name_required)
            return
        }
        if (email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email)
            return
        }

        viewModelScope.launch {
            try {
                accountService.updateProfile(name, email) { error ->
                    if (error == null) {
                        uiState.value = uiState.value.copy(errorMessage = 0) // Clear errors on success
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_account_creation)
            }
        }
    }
}


