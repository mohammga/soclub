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
import com.example.soclub.common.ext.isAgeValidMinimum
import com.example.soclub.common.ext.isPasswordLongEnough
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.common.ext.isValidName
import com.example.soclub.common.ext.isValidPassword
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
    @StringRes val errorMessage: Int = 0
)


@HiltViewModel
class SignupViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(RegistrationNewUserState())
        private set

    private val email get() = uiState.value.email
    private val name get() = uiState.value.name
    private val password get() = uiState.value.password
    private val age get() = uiState.value.age

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onNameChange(newValue: String) {
        uiState.value = uiState.value.copy(name = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onAgeChange(newValue: String) {
        uiState.value = uiState.value.copy(age = newValue)
    }

    fun onSignUpClick(navController: NavController) {
        if (name.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_name_required)
            return
        }

        if (age.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_age_required)
            return
        }

        if (email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_email_required)
            return
        }

        if (password.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_password_required)
            return
        }

        if (!name.isValidName()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_name)
            return
        }

        if (!age.isAgeNumeric()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_age)
            return
        }

        if (!age.isAgeValidMinimum()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_age_minimum)
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
                val convertedAge = age.toIntOrNull() ?: 0 // Sett til 0 hvis ikke konvertering er mulig
                accountService.createEmailAccount(email, password, name, convertedAge) { error ->
                    if (error == null)
                        navController.navigate(AppScreens.HOME.name)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_account_creation)
            }
        }
    }
}
