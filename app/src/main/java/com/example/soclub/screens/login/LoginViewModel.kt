package com.example.soclub.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.common.ext.isValidEmail
import com.example.soclub.common.ext.isValidPassword
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(LoginUiState())
        private set

    private val email get() = uiState.value.email
    private val password get() = uiState.value.password
    private val errorMessage get() = uiState.value.errorMessage

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onLoginClick(context: Context, navController: NavController) {

        if (!email.isValidEmail()) {
            Toast.makeText(context, "Invalid email", Toast.LENGTH_SHORT).show()
            uiState.value = uiState.value.copy(errorMessage = "Please insert a valid email")
            return
        }

        if (!password.isValidPassword()) {
            Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show()
            uiState.value = uiState.value.copy(errorMessage = "Your password should have at least six digits and include one digit, one lower case letter and one upper case letter.")
            return
        }

        viewModelScope.launch {

            try {
                accountService.authenticateWithEmail(email, password) { error ->

                    if (error == null) {
                        Toast.makeText(context, "Signing in as $email...", Toast.LENGTH_SHORT).show()
                        viewModelScope.launch {
                            accountService.currentUser.collectLatest { user ->
                                if (user.id?.isNotEmpty() == true) {
                                    navController.navigate(AppScreens.TEXT.name) {
                                        popUpTo(AppScreens.LOGIN.name) { inclusive = true }
                                    }
                                }
                            }
                        }
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = "Could not log in")
                    }

                }
            } catch(e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = "Could not log in")
            }

        }

    }

    fun onForgotPasswordClick() {
        try {
            val email = uiState.value.email
            if (email.isEmpty()) {
                uiState.value = uiState.value.copy(errorMessage = "Please enter the email address you want to reset")
                return
            }

            if (!isValidEmail(email)) {
                uiState.value = uiState.value.copy(errorMessage = "Please enter a valid email address")
                return
            }

            viewModelScope.launch {
                accountService.sendPasswordResetEmail(email) { error ->
                    uiState.value = uiState.value.copy(
                        errorMessage = if (error == null) {
                            "Password reset email sent, please check your inbox"
                        } else {
                            error.message.orEmpty()
                        }
                    )
                }
            }
        } catch (e: Exception) {
            uiState.value = uiState.value.copy(errorMessage = "Could not send reset email")
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}