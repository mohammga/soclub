package com.example.soclub.screens.registration

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.common.ext.isValidPassword
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationNewUserState(
    val email: String = "",
    val confirmEmail: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @StringRes val errorMessage: Int = 0
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(RegistrationNewUserState())
        private set

    private val email get() = uiState.value.email
    private val confirmEmail get() = uiState.value.confirmEmail
    private val password get() = uiState.value.password
    private val confirmPassword get() = uiState.value.confirmPassword

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onConfirmEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmEmail = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    fun onSignUpClick(navController: NavController) {
        if (!email.isValidEmail()) {
            uiState.value = uiState.value.copy(errorMessage = 0)
            return
        }

        else if (!password.isValidPassword()) {
            uiState.value = uiState.value.copy(errorMessage = 0)
            return
        }

        else if (!(password == uiState.value.confirmPassword)) {
            uiState.value = uiState.value.copy(errorMessage = 0)
            return
        }

        viewModelScope.launch {
            try {
                accountService.createEmailAccount(email, password) { error ->
                    if (error == null)
                        navController.navigate(AppScreens.LOGIN.name)
                }
            }
            catch(e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = 0)
            }
        }
    }
}