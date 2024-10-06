package com.example.soclub.screens.signup

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.common.ext.isValidEmail
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

        if (!email.isValidEmail()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email)
            return
        }

        if (!password.isValidPassword()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_password)
            return
        }

        viewModelScope.launch {
            try {
                // Pass the name and age to the createEmailAccount function
                accountService.createEmailAccount(email, password, name, age) { error ->
                    if (error == null)
                        navController.navigate(AppScreens.HOME.name)
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_account_creation)
            }
        }
    }
}
