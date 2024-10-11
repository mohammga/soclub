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
    @StringRes val errorMessage: Int = 0
)

@HiltViewModel
class SigninViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(SigninUiState())
        private set

    private val email get() = uiState.value.email
    private val password get() = uiState.value.password

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onLoginClick(context: Context, navController: NavController) {

        if (email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_email_required)
            return
        }

        if (password.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_required)
            return
        }

        if (!email.isValidEmail()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email)
            return
        }

        if (!password.isPasswordLongEnough()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_too_short)
            return
        }

        if (!password.containsUpperCase()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_missing_uppercase)
            return
        }

        if (!password.containsLowerCase()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_missing_lowercase)
            return
        }

        if (!password.containsDigit()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_missing_digit)
            return
        }

        if (!password.containsNoWhitespace()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_contains_whitespace)
            return
        }


        viewModelScope.launch {
            try {
                accountService.authenticateWithEmail(email, password) { error ->
                    if (error == null) {
                        navController.navigate(AppScreens.HOME.name) {
                            popUpTo(AppScreens.SIGNIN.name) { inclusive = true }
                        }
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_log_in)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_log_in)
            }
        }
    }
}
