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

/**
 * Represents the UI state for the Sign-In screen.
 *
 * @property email The user's input email address.
 * @property password The user's input password.
 * @property emailError Optional resource ID for email validation error messages.
 * @property passwordError Optional resource ID for password validation error messages.
 * @property generalError Optional resource ID for general error messages.
 */
data class SigninUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val generalError: Int? = null
)

/**
 * ViewModel for handling the Sign-In screen logic.
 *
 * This ViewModel manages the state of the Sign-In UI, handles user input,
 * performs validation, and communicates with the [AccountService] to authenticate users.
 *
 * @property accountService The service responsible for account-related operations.
 */
@HiltViewModel
class SigninViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    /**
     * The current UI state of the Sign-In screen.
     */
    var uiState = mutableStateOf(SigninUiState())
        private set

    /**
     * Indicates whether a sign-in operation is currently in progress.
     */
    var isLoading = mutableStateOf(false)
        private set

    /**
     * Retrieves the current email input from the UI state.
     */
    private val email get() = uiState.value.email

    /**
     * Retrieves the current password input from the UI state.
     */
    private val password get() = uiState.value.password

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
     * Updates the password value in the UI state.
     *
     * Clears any existing password error.
     *
     * @param newValue The new password input from the user.
     */
    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue, passwordError = null)
    }

    /**
     * Handles the login action when the user clicks the login button.
     *
     * Performs input validation, displays appropriate error messages,
     * and attempts to authenticate the user using the [AccountService].
     * On successful authentication, navigates to the home screen.
     *
     * @param navController The [NavController] used for navigation.
     * @param context The [Context] used for displaying Toast messages.
     */
    fun onLoginClick(navController: NavController, context: Context) {
        var hasError = false
        var emailError: Int? = null
        var passwordError: Int? = null

        // Validate email input
        if (email.isBlank()) {
            emailError = R.string.error_email_required
            hasError = true
        } else if (!email.isValidEmail()) {
            emailError = R.string.error_invalid_email
            hasError = true
        }

        // Validate password input
        if (password.isBlank()) {
            passwordError = R.string.error_password_required
            hasError = true
        }

        // Update UI state with validation errors, if any
        uiState.value = uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError,
            generalError = null
        )

        // If there are validation errors, abort the login process
        if (hasError) return

        // Indicate that a login operation is in progress
        isLoading.value = true

        // Launch a coroutine for the authentication process
        viewModelScope.launch {
            try {
                accountService.authenticateWithEmail(email, password) { error ->
                    // Hide the loading indicator
                    isLoading.value = false

                    if (error == null) {
                        // Show success message
                        Toast.makeText(
                            context,
                            context.getString(R.string.success_login, email),
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to the Home screen and clear the back stack
                        navController.navigate(AppScreens.HOME.name) {
                            popUpTo(AppScreens.SIGNIN.name) { inclusive = true }
                        }
                    } else {
                        // Update UI state with a general login error
                        uiState.value = uiState.value.copy(generalError = R.string.error_could_not_log_in)
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during authentication
                isLoading.value = false
                uiState.value = uiState.value.copy(generalError = R.string.error_could_not_log_in)
            }
        }
    }
}
