package com.example.soclub.screens.resetPassword

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.R
import com.example.soclub.common.ext.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordUiState(
    val email: String = "",
    val errorMessage: String = ""
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(ResetPasswordUiState())
        private set

    private val email get() = uiState.value.email
    private val errorMessage get() = uiState.value.errorMessage

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onForgotPasswordClick() {
        try {
            if (email.isEmpty()) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_enter_reset_email.toString())
                return
            }

            if (!isValidEmail(email)) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email_address.toString())
                return
            }

            viewModelScope.launch {
                accountService.sendPasswordResetEmail(email) { error ->
                    uiState.value = uiState.value.copy(
                        errorMessage = if (error == null) {
                            R.string.password_reset_email_sent.toString()
                        } else {
                            error.message.orEmpty()
                        }
                    )
                }
            }
        } catch (e: Exception) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_send_reset_email.toString())
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
