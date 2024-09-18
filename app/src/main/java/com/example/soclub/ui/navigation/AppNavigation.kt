package com.example.soclub.ui.navigation

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
            when(currentScreen) {
                AppScreens.SIGNUP.name -> { TopBar(navController) } // Vise toppbar på signup screen
                else -> { /* Ingen toppbar for andre skjermer */ }
            }
        },
        bottomBar = {
            val screensWithoutBottomBar = setOf(
                AppScreens.SIGNIN.name,
                AppScreens.SIGNUP.name,
                AppScreens.START.name
            )

            if (currentScreen !in screensWithoutBottomBar) {
                BottomNavBar(navController) // Vise bottombar på alle skjermer unntatt de spesifiserte
            }
        },
        modifier = Modifier.fillMaxSize()
    )
 { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppScreens.START.name,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(AppScreens.SIGNUP.name) {
                SignupScreen(navController)
            }

            composable(AppScreens.HOME.name) {
                HomeScreen(navController)
            }

            composable(AppScreens.START.name) {
                StartScreen(navController)
            }

            composable(AppScreens.PROFILE.name) {
                ProfileScreen(navController)
            }

            composable(AppScreens.SIGNIN.name) {
                SigninScreen(navController)
            }

            composable(AppScreens.ENTRIES.name) {
                EntriesScreen(navController)
            }

        }

    }
}
