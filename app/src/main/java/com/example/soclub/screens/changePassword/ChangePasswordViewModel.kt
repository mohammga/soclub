package com.example.soclub.screens.changePassword

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
import com.example.soclub.common.ext.containsDigit
import com.example.soclub.common.ext.containsLowerCase
import com.example.soclub.common.ext.containsNoWhitespace
import com.example.soclub.common.ext.containsUpperCase
import com.example.soclub.common.ext.isPasswordLongEnough
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing the state of the Change Password screen.
 *
 * @property oldPassword The current password of the user.
 * @property newPassword The new password entered by the user.
 * @property confirmPassword The confirmation of the new password.
 * @property oldPasswordError Resource ID for any error related to the old password.
 * @property newPasswordError Resource ID for any error related to the new password.
 * @property confirmPasswordError Resource ID for any error related to confirming the new password.
 * @property generalError Resource ID for any general error during the password change process.
 */
data class ChangePasswordState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    @StringRes val oldPasswordError: Int? = null,
    @StringRes val newPasswordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val generalError: Int? = null
)

/**
 * ViewModel for managing the state and logic of the Change Password screen.
 *
 * Handles input validation, state management, and communication with the account service for changing passwords.
 *
 * @param accountService The service responsible for user account operations, including changing passwords.
 */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    /**
     * Mutable state indicating whether a password change request is currently being processed.
     */
    var isProcessing: MutableState<Boolean> = mutableStateOf(false)
        private set


    /**
     * Mutable state holding the current state of the Change Password screen, including user inputs and error messages.
     */
    var uiState: MutableState<ChangePasswordState> = mutableStateOf(ChangePasswordState())
        private set


    /**
     * Gets the value of the old password entered by the user from the current UI state.
     *
     * @return The old password entered by the user.
     */
    private val oldPassword get() = uiState.value.oldPassword

    /**
     * Gets the value of the new password entered by the user from the current UI state.
     *
     * @return The new password entered by the user.
     */
    private val newPassword get() = uiState.value.newPassword

    /**
     * Gets the value of the confirmation password entered by the user from the current UI state.
     *
     * @return The confirmation password entered by the user.
     */
    private val confirmPassword get() = uiState.value.confirmPassword



    /**
     * Updates the state with the new value of the old password.
     *
     * @param newValue The new value of the old password entered by the user.
     */
    fun onOldPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(oldPassword = newValue, oldPasswordError = null)
    }

    /**
     * Updates the state with the new value of the new password.
     *
     * @param newValue The new value of the new password entered by the user.
     */
    fun onNewPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(newPassword = newValue, newPasswordError = null)
    }

    /**
     * Updates the state with the new value of the confirmation password.
     *
     * @param newValue The new value of the confirmation password entered by the user.
     */
    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue, confirmPasswordError = null)
    }


    /**
     * Validates the inputs and initiates the password change process if validation passes.
     *
     * Displays appropriate error messages for invalid inputs and sends a request to change the password.
     *
     * @param context The context for showing Toast messages and accessing resources.
     */
    fun onChangePasswordClick(context: Context) {
        var hasError = false
        var oldPasswordError: Int? = null
        var newPasswordError: Int? = null
        var confirmPasswordError: Int? = null

        if (oldPassword.isBlank()) {
            oldPasswordError = R.string.error_old_password_required
            hasError = true
        }
        if (newPassword.isBlank()) {
            newPasswordError = R.string.error_new_password_required
            hasError = true
        } else if (!newPassword.isPasswordLongEnough()) {
            newPasswordError = R.string.error_password_too_short
            hasError = true
        } else if (!newPassword.containsUpperCase()) {
            newPasswordError = R.string.error_password_missing_uppercase
            hasError = true
        } else if (!newPassword.containsLowerCase()) {
            newPasswordError = R.string.error_password_missing_lowercase
            hasError = true
        } else if (!newPassword.containsDigit()) {
            newPasswordError = R.string.error_password_missing_digit
            hasError = true
        } else if (!newPassword.containsNoWhitespace()) {
            newPasswordError = R.string.error_password_contains_whitespace
            hasError = true
        }
        if (confirmPassword.isBlank()) {
            confirmPasswordError = R.string.error_confirm_password_required
            hasError = true
        } else if (newPassword != confirmPassword) {
            confirmPasswordError = R.string.password_mismatch_error
            hasError = true
        }
        uiState.value = uiState.value.copy(
            oldPasswordError = oldPasswordError,
            newPasswordError = newPasswordError,
            confirmPasswordError = confirmPasswordError
        )

        if (hasError) return
        isProcessing.value = true

        viewModelScope.launch {
            try {
                accountService.changePassword(oldPassword, newPassword)
                isProcessing.value = false
                uiState.value = ChangePasswordState()
                Toast.makeText(context, context.getString(R.string.password_change), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                isProcessing.value = false
                uiState.value = uiState.value.copy(generalError = R.string.error_could_not_change_password)
            }
        }
    }

}