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


data class ChangePasswordState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    @StringRes val oldPasswordError: Int? = null,
    @StringRes val newPasswordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val generalError: Int? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    var uiState: MutableState<ChangePasswordState> = mutableStateOf(ChangePasswordState())
        private set

    private val oldPassword get() = uiState.value.oldPassword
    private val newPassword get() = uiState.value.newPassword
    private val confirmPassword get() = uiState.value.confirmPassword

    fun onOldPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(oldPassword = newValue, oldPasswordError = null)
    }

    fun onNewPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(newPassword = newValue, newPasswordError = null)
    }

    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue, confirmPasswordError = null)
    }

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

        viewModelScope.launch {
            try {
                accountService.changePassword(oldPassword, newPassword) { error ->
                    if (error == null) {
                        uiState.value = ChangePasswordState() // Reset state after success
                        Toast.makeText(context, context.getString(R.string.password_change), Toast.LENGTH_LONG).show()
                    } else {
                        uiState.value = uiState.value.copy(generalError = R.string.error_could_not_change_password)
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(generalError = R.string.error_could_not_change_password)
            }
        }
    }
}
