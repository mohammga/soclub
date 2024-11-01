package com.example.soclub.screens.signup

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.common.ext.containsDigit
import com.example.soclub.common.ext.containsLowerCase
import com.example.soclub.common.ext.containsNoWhitespace
import com.example.soclub.common.ext.containsUpperCase
import com.example.soclub.common.ext.isAgeNumeric
import com.example.soclub.common.ext.isAgeValid
import com.example.soclub.common.ext.isPasswordLongEnough
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.common.ext.isValidName
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


data class RegistrationNewUserState(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val age: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val nameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val ageError: Int? = null
)

@HiltViewModel
class SignupViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(RegistrationNewUserState())
        private set

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue, emailError = null)
    }

    fun onNameChange(newValue: String) {
        uiState.value = uiState.value.copy(name = newValue, nameError = null)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue, passwordError = null)
    }

    fun onAgeChange(newValue: String) {
        uiState.value = uiState.value.copy(age = newValue, ageError = null)
    }

    fun onSignUpClick(navController: NavController) {
        var hasError = false
        var nameError: Int? = null
        var ageError: Int? = null
        var emailError: Int? = null
        var passwordError: Int? = null

        if (uiState.value.name.isBlank()) {
            nameError = R.string.error_name_required
            hasError = true
        } else if (!uiState.value.name.isValidName()) {
            nameError = R.string.error_invalid_name
            hasError = true
        }

        if (uiState.value.age.isBlank()) {
            ageError = R.string.error_age_required
            hasError = true
        } else if (!uiState.value.age.isAgeNumeric()) {
            ageError = R.string.error_invalid_age
            hasError = true
        } else if (!uiState.value.age.isAgeValid()) {
            ageError = R.string.error_age_minimum
            hasError = true
        }

        if (uiState.value.email.isBlank()) {
            emailError = R.string.error_email_required
            hasError = true
        } else if (!uiState.value.email.isValidEmail()) {
            emailError = R.string.error_invalid_email
            hasError = true
        }

        if (uiState.value.password.isBlank()) {
            passwordError = R.string.error_password_required
            hasError = true
        } else if (!uiState.value.password.isPasswordLongEnough()) {
            passwordError = R.string.error_password_too_short
            hasError = true
        } else if (!uiState.value.password.containsUpperCase()) {
            passwordError = R.string.error_password_missing_uppercase
            hasError = true
        } else if (!uiState.value.password.containsLowerCase()) {
            passwordError = R.string.error_password_missing_lowercase
            hasError = true
        } else if (!uiState.value.password.containsDigit()) {
            passwordError = R.string.error_password_missing_digit
            hasError = true
        } else if (!uiState.value.password.containsNoWhitespace()) {
            passwordError = R.string.error_password_contains_whitespace
            hasError = true
        }

        uiState.value = uiState.value.copy(
            nameError = nameError,
            ageError = ageError,
            emailError = emailError,
            passwordError = passwordError
        )

        if (hasError) return

        viewModelScope.launch {
            try {
                val convertedAge = uiState.value.age.toIntOrNull() ?: 0
                accountService.createEmailAccount(uiState.value.email, uiState.value.password, uiState.value.name, convertedAge) { error ->
                    if (error == null) {
                        navController.navigate(AppScreens.HOME.name)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(emailError = R.string.error_account_creation)
            }
        }
    }
}
