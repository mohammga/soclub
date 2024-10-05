package com.example.soclub.components.navigation

import HomeTopBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.soclub.components.navigation.navBars.BottomNavBar
import com.example.soclub.components.navigation.navBars.TopBar
import com.example.soclub.components.navigation.navBars.getCurrentScreen
import com.example.soclub.screens.activityDetail.ActivityDetailScreen
import com.example.soclub.screens.changePassword.ChangePasswordScreen
import com.example.soclub.screens.editPermission.EditPermissionScreen
import com.example.soclub.screens.editProfile.EditProfileScreen
import com.example.soclub.screens.entries.EntriesScreen
import com.example.soclub.screens.home.HomeScreen
import com.example.soclub.screens.profile.ProfileScreen
import com.example.soclub.screens.signin.SigninScreen
import com.example.soclub.screens.signup.SignupScreen;
import com.example.soclub.screens.start.StartScreen;
import com.example.soclub.service.ActivityService
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.module.FirebaseModule

@Composable
fun AppNavigation(navController: NavHostController, activityService: ActivityService) {
    val navController = rememberNavController()
    val currentScreen = getCurrentScreen(navController)

    // Inject FirebaseAuth and FirebaseFirestore using FirebaseModule
    val auth = remember { FirebaseModule.auth() }
    val firestore = remember { FirebaseModule.firestore() }

    // Pass both auth and firestore to AccountServiceImpl
    val accountService = remember { AccountServiceImpl(auth, firestore) }

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
                AppScreens.DETAIL.name -> {
                    TopBar(navController, title = "Aktivitet", showBackButton = true)
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
            startDestination =
            if (accountService.hasUser) {
                AppScreens.HOME.name
            } else {
                AppScreens.START.name
            },
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
            composable(
                route = "detail/{activityId}",
                arguments = listOf(navArgument("activityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId")
                ActivityDetailScreen(navController, activityId, activityService)
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



