package com.example.soclub.screens.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    var userInfo by mutableStateOf<UserInfo?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isLoggingOut by mutableStateOf(false)
        private set

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            isLoading = true
            try {
                userInfo = accountService.getUserInfo()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun onSignOut(navController: NavController, context: Context) {
        isLoggingOut = true
        viewModelScope.launch {
            accountService.signOut()
            isLoggingOut = false
            Toast.makeText(context, context.getString(R.string.sign_out_success), Toast.LENGTH_LONG).show()
            navController.navigate(AppScreens.SIGNIN.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
