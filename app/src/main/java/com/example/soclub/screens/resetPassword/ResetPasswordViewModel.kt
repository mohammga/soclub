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

/**
 * Represents the UI state for the Reset Password screen.
 *
 * @property email The user's input email address.
 * @property emailError Optional resource ID for email validation error messages.
 * @property statusMessage Optional resource ID for status messages after attempting to send the reset email.
 */
data class ResetPasswordUiState(
    val email: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val statusMessage: Int? = null
)

/**
 * ViewModel for handling the logic of the Reset Password screen.
 *
 * This ViewModel manages the state of the Reset Password UI, handles user input,
 * performs validation, and communicates with [AccountService] to send password reset emails.
 *
 * @property accountService The service responsible for account-related operations.
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    /**
     * The current UI state of the Reset Password screen.
     */
    var uiState = mutableStateOf(ResetPasswordUiState())
        private set

    /**
     * Indicates whether a password reset operation is currently in progress.
     */
    var isLoading = mutableStateOf(false)
        private set

    /**
     * Retrieves the current email from the UI state.
     */
    private val email get() = uiState.value.email

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
     * Handles the password reset action when the user clicks the reset button.
     *
     * Performs input validation, displays appropriate error messages,
     * and attempts to send a password reset email using [AccountService].
     * Upon successful email dispatch, displays a success message.
     * If an error occurs, updates the UI state with an error message.
     *
     * @param context The [Context] used for displaying Toast messages.
     */
    fun onForgotPasswordClick(context: Context) {
        var emailError: Int? = null

        // Validate email input
        if (email.isBlank()) {
            emailError = R.string.error_email_required
        } else if (!email.isValidEmail()) {
            emailError = R.string.error_invalid_email
        }

        // Update UI state with email validation error if any
        if (emailError != null) {
            uiState.value = uiState.value.copy(emailError = emailError)
            return
        }

        // Indicate that a password reset operation is in progress
        isLoading.value = true

        // Launch a coroutine to perform the password reset operation asynchronously
        viewModelScope.launch {
            try {
                accountService.sendPasswordResetEmail(email) { error ->
                    // Reset loading state
                    isLoading.value = false

                    if (error == null) {
                        // Display success message
                        Toast.makeText(context, context.getString(R.string.password_reset_email_sent), Toast.LENGTH_LONG).show()
                        // Update UI state with success status message
                        uiState.value = ResetPasswordUiState(statusMessage = R.string.password_reset_email_sent)
                    } else {
                        // Update UI state with error status message
                        uiState.value = uiState.value.copy(statusMessage = R.string.error_could_not_send_reset_email)
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during the password reset process
                isLoading.value = false
                uiState.value = uiState.value.copy(statusMessage = R.string.error_could_not_send_reset_email)
            }
        }
    }
}
