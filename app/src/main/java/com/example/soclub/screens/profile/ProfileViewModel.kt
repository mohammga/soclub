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

    /**
     * Holds the user's profile information.
     *
     * Initially set to `null` until the data is fetched from the account service.
     */
    var userInfo by mutableStateOf<UserInfo?>(null)
        private set

    /**
     * Indicates whether the user information is currently being fetched.
     *
     * Initially set to `true` and updated to `false` once the data fetching is complete.
     */
    var isLoading by mutableStateOf(true)
        private set

    /**
     * Tracks whether the user is currently in the process of logging out.
     *
     * Set to `true` during the sign-out process and `false` when the process completes.
     */
    var isLoggingOut by mutableStateOf(false)
        private set

    /**
     * Initializes the ViewModel by fetching the user's profile information.
     *
     * This ensures the `userInfo` is populated as soon as the ViewModel is created.
     */
    init {
        fetchUserInfo()
    }

    /**
     * Fetches the current user's information from the account service.
     *
     * Updates the `userInfo` state with the retrieved data and sets `isLoading` to `false` once the data is loaded.
     * If an error occurs, it silently fails and `isLoading` is still set to `false`.
     */
    fun fetchUserInfo() {
        viewModelScope.launch {
            isLoading = true
            try {
                userInfo = accountService.getUserInfo()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Signs out the current user and navigates to the Sign In screen.
     *
     * Displays a toast message upon successful sign-out. If the sign-out process is successful,
     * all navigation backstack entries are cleared, and the user is redirected to the Sign In screen.
     *
     * @param navController The NavController used to navigate between screens.
     * @param context The context used to display toast messages.
     */
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
