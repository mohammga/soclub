package com.example.soclub.ui.navigation

import HomeTopBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.soclub.ui.navigation.navBars.BottomNavBar
import com.example.soclub.ui.navigation.navBars.TopBar
import com.example.soclub.ui.navigation.navBars.getCurrentScreen
import com.example.soclub.ui.screens.changePassword.ChangePasswordScreen
import com.example.soclub.ui.screens.editPermission.EditPermissionScreen
import com.example.soclub.ui.screens.editProfile.EditProfileScreen
import com.example.soclub.ui.screens.entries.EntriesScreen
import com.example.soclub.ui.screens.home.HomeScreen
import com.example.soclub.ui.screens.profile.ProfileScreen
import com.example.soclub.ui.screens.signin.SigninScreen
import com.example.soclub.ui.screens.signup.SignupScreen;
import com.example.soclub.ui.screens.start.StartScreen;


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentScreen = getCurrentScreen(navController)

    Scaffold(
        topBar = {
            when (currentScreen) {
                AppScreens.SIGNUP.name -> {
                    TopBar(navController, title = "Join Clubhouse", showBackButton = true)
                }
                AppScreens.SIGNIN.name -> {
                    TopBar(navController, title = "Logg inn", showBackButton = true)
                }
                AppScreens.HOME.name -> {
                    HomeTopBar(navController, title = "SoClub")
                }
                AppScreens.PROFILE.name -> {
                    TopBar(navController, title = "Profil", showBackButton = false)
                }
                AppScreens.EDIT_PROFILE.name -> {
                TopBar(navController, title = "Endre Profil", showBackButton = true)
                }

                AppScreens.CHANGE_PASSWORD.name -> {
                    TopBar(navController, title = "Endre passord", showBackButton = true)
                }

                AppScreens.EDIT_PERMISSION.name -> {
                    TopBar(navController, title = "Endre tillatelser", showBackButton = true)
                }

                AppScreens.ENTRIES.name -> {
                    TopBar(navController, title = "Mine Påmeldinger", showBackButton = false)
                }
                else -> {
                    // Handle other screens
                }
            }
        },
        bottomBar = {
            val screensWithoutBottomBar = setOf(
                AppScreens.SIGNIN.name,
                AppScreens.SIGNUP.name,
                AppScreens.START.name
            )

            if (currentScreen !in screensWithoutBottomBar) {
                BottomNavBar(navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppScreens.START.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreens.START.name) {
                StartScreen(navController)
            }
            composable(AppScreens.SIGNUP.name) {
                SignupScreen(navController)
            }
            composable(AppScreens.SIGNIN.name) {
                SigninScreen(navController)
            }
            composable(AppScreens.HOME.name) {
                HomeScreen(navController)
            }
            composable(AppScreens.ENTRIES.name) {
                EntriesScreen(navController)
            }
            composable(AppScreens.PROFILE.name) {
                ProfileScreen(navController)
            }
            composable(AppScreens.CHANGE_PASSWORD.name) {
                ChangePasswordScreen(navController)
            }
            composable(AppScreens.EDIT_PROFILE.name) {
                EditProfileScreen(navController)
            }

            composable(AppScreens.EDIT_PERMISSION.name) {
                EditPermissionScreen(navController)
            }

        }
    }
}



