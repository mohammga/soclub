package com.example.soclub.screens.signin

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SigninUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val generalError: Int? = null
)

@HiltViewModel
class SigninViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(SigninUiState())
        private set

    var isLoading = mutableStateOf(false)
        private set

    private val email get() = uiState.value.email
    private val password get() = uiState.value.password


    fun onEmailChange(newValue: String) {
        val lowerCaseEmail = newValue.lowercase()
        uiState.value = uiState.value.copy(email = lowerCaseEmail, emailError = null)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue, passwordError = null)
    }

    fun onLoginClick(navController: NavController, context: Context) {
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
        }

        uiState.value = uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError,
            generalError = null
        )

        if (hasError) return

        isLoading.value = true

        viewModelScope.launch {
            try {
                accountService.authenticateWithEmail(email, password) { error ->
                    isLoading.value = false
                    if (error == null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.success_login, email),
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate(AppScreens.HOME.name) {
                            popUpTo(AppScreens.SIGNIN.name) { inclusive = true }
                        }
                    } else {
                        uiState.value = uiState.value.copy(generalError = R.string.error_could_not_log_in)
                    }
                }
            } catch (e: Exception) {
                isLoading.value = false
                uiState.value = uiState.value.copy(generalError = R.string.error_could_not_log_in)
            }
        }
    }
}