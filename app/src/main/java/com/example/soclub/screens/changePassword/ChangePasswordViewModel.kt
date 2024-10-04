package com.example.soclub.screens.changePassword

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    @StringRes val errorMessage: Int = 0
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
        uiState.value = uiState.value.copy(oldPassword = newValue)
    }

    fun onNewPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(newPassword = newValue)
    }

    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    fun onChangePasswordClick(navController: NavController) {
        if (newPassword != confirmPassword) {
            uiState.value = uiState.value.copy(errorMessage = R.string.password_mismatch_error)
            return
        }

        viewModelScope.launch {
            try {
                accountService.changePassword(oldPassword, newPassword) { error ->
                    if (error == null) {
                        uiState.value = uiState.value.copy(errorMessage = 0) // Clear errors on success
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_send_reset_email)
            }
        }
    }
}
