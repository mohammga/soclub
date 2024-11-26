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

/**
 * Represents the state of the registration screen for new users.
 *
 * @property email The user's input email address.
 * @property firstname The user's first name.
 * @property lastname The user's last name.
 * @property password The user's input password.
 * @property age The user's input age.
 * @property emailError Optional resource ID for email validation error messages.
 * @property firstNameError Optional resource ID for first name validation error messages.
 * @property lastNameError Optional resource ID for last name validation error messages.
 * @property passwordError Optional resource ID for password validation error messages.
 * @property ageError Optional resource ID for age validation error messages.
 * @property generalError Optional resource ID for general error messages.
 */
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

/**
 * ViewModel for handling the logic of the registration screen for new users.
 *
 * This ViewModel manages the state of the registration UI, handles user input,
 * performs validation, and communicates with [AccountService] to create new accounts.
 *
 * @property accountService The service responsible for account-related operations.
 */
@HiltViewModel
class SignupViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    /**
     * The current UI state of the registration screen.
     */
    var uiState = mutableStateOf(RegistrationNewUserState())
        private set

    /**
     * Indicates whether a registration operation is currently in progress.
     */
    var isLoading = mutableStateOf(false)
        private set

    /**
     * Updates the email value in the UI state.
     *
     * Converts the input to lowercase and clears any existing email error.
     *
     * @param newValue The new email input from the user.
     */
    fun onEmailChange(newValue: String) {
        val lowerCaseEmail = newValue.lowercase()
        uiState.value = uiState.value.copy(email = lowerCaseEmail, emailError = null)
    }

    /**
     * Updates the first name value in the UI state.
     *
     * Formats the first name by capitalizing the first letter of each word and clears any existing first name error.
     *
     * @param newValue The new first name input from the user.
     */
    fun onFirstNameChange(newValue: String) {
        val formattedFirstName = newValue
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
        uiState.value = uiState.value.copy(firstname = formattedFirstName, firstNameError = null)
    }

    /**
     * Updates the last name value in the UI state.
     *
     * Removes spaces and capitalizes the first letter of the last name, then clears any existing last name error.
     *
     * @param newValue The new last name input from the user.
     */
    infix fun onLastNameChange(newValue: String) {
        val formattedLastName = newValue.replace(" ", "").replaceFirstChar { it.uppercaseChar() }
        uiState.value = uiState.value.copy(lastname = formattedLastName, lastNameError = null)
    }

    /**
     * Updates the password value in the UI state.
     *
     * Clears any existing password error.
     *
     * @param newValue The new password input from the user.
     */
    infix fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue, passwordError = null)
    }

    /**
     * Updates the age value in the UI state.
     *
     * Clears any existing age error.
     *
     * @param newValue The new age input from the user.
     */
    fun onAgeChange(newValue: String) {
        uiState.value = uiState.value.copy(age = newValue, ageError = null)
    }

    /**
     * Handles the sign-up action when the user clicks the sign-up button.
     *
     * Performs input validation, displays appropriate error messages,
     * and attempts to create a new account using [AccountService].
     * Upon successful registration, navigates to the home screen.
     *
     * @param navController [NavController] used for navigation.
     * @param context [Context] used for displaying Toast messages.
     */

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
        } else if (!uiState.value.age.isValidAgeLimit()) {
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
            generalError = null
        )

        if (hasError) return
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
                )
                isLoading.value = false
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.registration_success_login,
                        uiState.value.email
                    ),
                    Toast.LENGTH_LONG
                ).show()
                uiState.value = RegistrationNewUserState()
                navController.navigate(AppScreens.HOME.name)
            } catch (e: Exception) {
                isLoading.value = false
                uiState.value = uiState.value.copy(generalError = R.string.error_account_creation)
            }
        }
    }
}
