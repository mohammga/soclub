package com.example.soclub.screens.LoginSelection

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSelectionViewModel @Inject constructor(private val accountService: AccountService) : ViewModel() {

    fun signInAnonymously(
        context: Context,
        navController: NavController
    ) {
        viewModelScope.launch {
            if (!accountService.hasUser) {
                accountService.createAnonymousAccount()
                Toast.makeText(context, "Logging in Anonymously...", Toast.LENGTH_SHORT).show()
            }

            accountService.currentUser.collectLatest { user ->
                if (user.id?.isNotEmpty() == true) {
                    navController.navigate(AppScreens.TEXT.name) {
                        popUpTo(AppScreens.LOGIN_SELECTION.name) { inclusive = true }
                    }
                }
            }
        }
    }
}
