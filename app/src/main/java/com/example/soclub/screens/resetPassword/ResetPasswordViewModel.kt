package com.example.soclub.screens.resetPassword

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordUiState(
    val email: String = "",
    @StringRes val errorMessage: Int = 0
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(ResetPasswordUiState())
        private set

    private val email get() = uiState.value.email

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onForgotPasswordClick() {
        if (email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_email_required)
            return
        }

        if (!email.isValidEmail()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email)
            return
        }

        viewModelScope.launch {
            try {
                accountService.sendPasswordResetEmail(email) { error ->
                    if (error == null) {
                        uiState.value = uiState.value.copy(errorMessage = R.string.password_reset_email_sent)
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_send_reset_email)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_send_reset_email)
            }
        }
    }
}
