package com.example.soclub.screens.signin

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
import com.example.soclub.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SigninUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = ""
)

@HiltViewModel
class SigninViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    var uiState = mutableStateOf(SigninUiState())
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
            Toast.makeText(context, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            uiState.value = uiState.value.copy(errorMessage = R.string.error_please_insert_valid_email.toString())
            return
        }

        if (!password.isValidPassword()) {
            Toast.makeText(context, R.string.error_invalid_password, Toast.LENGTH_SHORT).show()
            uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_password_details.toString())
            return
        }

        viewModelScope.launch {
            try {
                accountService.authenticateWithEmail(email, password) { error ->
                    if (error == null) {
                        Toast.makeText(context, R.string.signing_in_as.toString(), Toast.LENGTH_SHORT).show()
                        viewModelScope.launch {
                            accountService.currentUser.collectLatest { user ->
                                if (user.id?.isNotEmpty() == true) {
                                    navController.navigate(AppScreens.HOME.name) {
                                        popUpTo(AppScreens.SIGNIN.name) { inclusive = true }
                                    }
                                }
                            }
                        }
                    } else {
                        uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_log_in.toString())
                    }
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_log_in.toString())
            }
        }
    }

    fun onForgotPasswordClick() {
        try {
            val email = uiState.value.email
            if (email.isEmpty()) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_enter_reset_email.toString())
                return
            }

            if (!isValidEmail(email)) {
                uiState.value = uiState.value.copy(errorMessage = R.string.error_invalid_email_address.toString())
                return
            }

            viewModelScope.launch {
                accountService.sendPasswordResetEmail(email) { error ->
                    uiState.value = uiState.value.copy(
                        errorMessage = if (error == null) {
                            R.string.password_reset_email_sent.toString()
                        } else {
                            error.message.orEmpty()
                        }
                    )
                }
            }
        } catch (e: Exception) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_could_not_send_reset_email.toString())
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
