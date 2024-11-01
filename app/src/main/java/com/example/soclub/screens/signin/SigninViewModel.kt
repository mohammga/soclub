package com.example.soclub.screens.signin

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.common.ext.isPasswordLongEnough
import com.example.soclub.common.ext.containsUpperCase
import com.example.soclub.common.ext.containsLowerCase
import com.example.soclub.common.ext.containsDigit

import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.R
import com.example.soclub.common.ext.containsNoWhitespace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SigninUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null
)

@HiltViewModel
class SigninViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(SigninUiState())
        private set

    private val email get() = uiState.value.email
    private val password get() = uiState.value.password

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue, emailError = null)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue, passwordError = null)
    }

    fun onLoginClick(context: Context, navController: NavController) {
        var hasError = false
        var emailError: Int? = null
        var passwordError: Int? = null

        if (email.isBlank()) {
            emailError = R.string.error_email_required
            hasError = true
        } else if (!email.isValidEmail()) {
            emailError = R.string.error_invalid_email
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = R.string.error_password_required
            hasError = true
        } else if (!password.isPasswordLongEnough()) {
            passwordError = R.string.error_password_too_short
            hasError = true
        } else if (!password.containsUpperCase()) {
            passwordError = R.string.error_password_missing_uppercase
            hasError = true
        } else if (!password.containsLowerCase()) {
            passwordError = R.string.error_password_missing_lowercase
            hasError = true
        } else if (!password.containsDigit()) {
            passwordError = R.string.error_password_missing_digit
            hasError = true
        } else if (!password.containsNoWhitespace()) {
            passwordError = R.string.error_password_contains_whitespace
            hasError = true
        }

        uiState.value = uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        if (hasError) return

        viewModelScope.launch {
            try {
                accountService.authenticateWithEmail(email, password) { error ->
                    if (error == null) {
                        navController.navigate(AppScreens.HOME.name) {
                            popUpTo(AppScreens.SIGNIN.name) { inclusive = true }
                        }
                    } else {
                        uiState.value = uiState.value.copy(emailError = R.string.error_could_not_log_in)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(emailError = R.string.error_could_not_log_in)
            }
        }
    }
}
