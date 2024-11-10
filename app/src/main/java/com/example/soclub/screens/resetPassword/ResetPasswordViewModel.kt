package com.example.soclub.screens.resetPassword

import android.content.Context
import android.widget.Toast
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
    @StringRes val emailError: Int? = null,
    @StringRes val statusMessage: Int? = null
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(ResetPasswordUiState())
        private set

    private val email get() = uiState.value.email

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue, emailError = null)
    }

    fun onForgotPasswordClick(context: Context) {
        var emailError: Int? = null

        if (email.isBlank()) {
            emailError = R.string.error_email_required
        } else if (!email.isValidEmail()) {
            emailError = R.string.error_invalid_email
        }

        if (emailError != null) {
            uiState.value = uiState.value.copy(emailError = emailError)
            return
        }

        viewModelScope.launch {
            try {
                accountService.sendPasswordResetEmail(email) { error ->
                    if (error == null) {
                        // Show success toast and reset email input
                        Toast.makeText(context, context.getString(R.string.password_reset_email_sent), Toast.LENGTH_LONG).show()
                        uiState.value = ResetPasswordUiState(statusMessage = R.string.password_reset_email_sent)
                    } else {
                        uiState.value = uiState.value.copy(statusMessage = R.string.error_could_not_send_reset_email)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(statusMessage = R.string.error_could_not_send_reset_email)
            }
        }
    }
}
