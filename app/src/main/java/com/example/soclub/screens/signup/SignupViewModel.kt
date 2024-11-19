package com.example.soclub.screens.signup

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.common.ext.*
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationNewUserState(
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val password: String = "",
    val age: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val ageError: Int? = null,
    @StringRes val generalError: Int? = null
)

@HiltViewModel
class SignupViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(RegistrationNewUserState())
        private set

    var isLoading = mutableStateOf(false) // Ny tilstand for å spore registreringsstatus
        private set


    fun onEmailChange(newValue: String) {
        val lowerCaseEmail = newValue.lowercase()
        uiState.value = uiState.value.copy(email = lowerCaseEmail, emailError = null)
    }

    fun onFirstNameChange(newValue: String) {
        val formattedFirstName = newValue
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
        uiState.value = uiState.value.copy(firstname = formattedFirstName, firstNameError = null)
    }

    infix fun onLastNameChange(newValue: String) {
        val formattedLastName = newValue.replace(" ", "").replaceFirstChar { it.uppercaseChar() }
        uiState.value = uiState.value.copy(lastname = formattedLastName, lastNameError = null)
    }


    infix fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue, passwordError = null)
    }

    fun onAgeChange(newValue: String) {
        uiState.value = uiState.value.copy(age = newValue, ageError = null)
    }

    fun onSignUpClick(navController: NavController, context: Context) {
        var hasError = false
        var firstNameError: Int? = null
        var lastNameError: Int? = null
        var ageError: Int? = null
        var emailError: Int? = null
        var passwordError: Int? = null

        if (uiState.value.firstname.isBlank()) {
            firstNameError = R.string.error_first_name_required
            hasError = true
        } else if (!uiState.value.firstname.isValidName()) {
            firstNameError = R.string.error_invalid_firstname
            hasError = true
        }

        if (uiState.value.lastname.isBlank()) {
            lastNameError = R.string.error_last_name_required
            hasError = true
        } else if (!uiState.value.lastname.isValidName()) {
            lastNameError = R.string.error_invalid_lastname
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
            firstNameError = firstNameError,
            lastNameError = lastNameError,
            ageError = ageError,
            emailError = emailError,
            passwordError = passwordError,
            generalError = null // Reset general error on each new attempt
        )

        if (hasError) return

        // Start registreringsprosessen
        isLoading.value = true

        viewModelScope.launch {
            try {
                val convertedAge = uiState.value.age.toIntOrNull() ?: 0
                accountService.createEmailAccount(
                    uiState.value.email,
                    uiState.value.password,
                    uiState.value.firstname,
                    uiState.value.lastname,
                    convertedAge
                ) { error ->
                    isLoading.value = false // Fullfør registreringsprosessen

                    if (error == null) {
                        // Sucessmelding, naviger til Hjem
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.registration_success_login,
                                uiState.value.email
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                        // Tilbakestill inputfeltene etter suksessfull registrering
                        uiState.value = RegistrationNewUserState()
                        navController.navigate(AppScreens.HOME.name)
                    } else {
                        uiState.value =
                            uiState.value.copy(generalError = R.string.error_account_creation)
                    }
                }
            } catch (e: Exception) {
                isLoading.value = false
                uiState.value = uiState.value.copy(generalError = R.string.error_account_creation)
            }
        }
    }
}
