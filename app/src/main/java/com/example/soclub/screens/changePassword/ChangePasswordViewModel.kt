package com.example.soclub.screens.changePassword

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
import com.example.soclub.common.ext.containsDigit
import com.example.soclub.common.ext.containsLowerCase
import com.example.soclub.common.ext.containsNoWhitespace
import com.example.soclub.common.ext.containsUpperCase
import com.example.soclub.common.ext.isPasswordLongEnough
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.soclub.common.ext.isValidPassword

data class ChangePasswordState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    @StringRes val errorMessage: Int = 0
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    var uiState: MutableState<ChangePasswordState> = mutableStateOf(ChangePasswordState())
        private set

    private val oldPassword get() = uiState.value.oldPassword
    private val newPassword get() = uiState.value.newPassword
    private val confirmPassword get() = uiState.value.confirmPassword

    fun onOldPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(oldPassword = newValue)
    }

    fun onNewPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(newPassword = newValue)
    }

    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    fun onChangePasswordClick() {
        if (oldPassword.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_old_password_required)
            return
        }

        if (newPassword.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_new_password_required)
            return
        }

        if (confirmPassword.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_confirm_password_required)
            return
        }

        if (!newPassword.isPasswordLongEnough()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_too_short)
            return
        }

        if (!newPassword.containsUpperCase()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_missing_uppercase)
            return
        }

        if (!newPassword.containsLowerCase()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_missing_lowercase)
            return
        }

        if (!newPassword.containsDigit()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_missing_digit)
            return
        }

        if (!newPassword.containsNoWhitespace()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_contains_whitespace)
            return
        }

        if (newPassword != confirmPassword) {
            uiState.value = uiState.value.copy(errorMessage = R.string.password_mismatch_error)
            return
        }

        viewModelScope.launch {
            try {
                accountService.changePassword(oldPassword, newPassword) { error ->
                    if (error == null) {
                        // Tømmer inputfeltene etter vellykket endring
                        uiState.value = ChangePasswordState() // Tilbakestill til tomme verdier
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_change_password)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_change_password)
            }
        }
    }
}
