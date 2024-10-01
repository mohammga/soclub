package com.example.soclub.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
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

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                userInfo = accountService.getUserInfo()
            } catch (e: Exception) {
                // Handle error, e.g., log it or show a message to the user
            }
        }
    }

    fun onSignOut(navController: NavController) {
        viewModelScope.launch {
            accountService.signOut()
            navController.navigate(AppScreens.SIGNIN.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
