package com.example.soclub.components.navigation

import AdsScreen
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
import com.example.soclub.components.navigation.navBars.HomeTopBar
import com.example.soclub.components.navigation.navBars.TopBar
import com.example.soclub.components.navigation.navBars.getCurrentScreen
import com.example.soclub.screens.newActivity.NewActivityScreen
import com.example.soclub.screens.activityDetail.ActivityDetailScreen
import com.example.soclub.screens.changePassword.ChangePasswordScreen
import com.example.soclub.screens.editActivity.EditActivityScreen
import com.example.soclub.screens.editPermission.EditPermissionScreen
import com.example.soclub.screens.editProfile.EditProfileScreen
import com.example.soclub.screens.entries.EntriesScreen
import com.example.soclub.screens.home.HomeScreen

import com.example.soclub.screens.notifications.NotificationsScreen
import com.example.soclub.screens.profile.ProfileScreen
import com.example.soclub.screens.resetPassword.ResetPasswordScreen
import com.example.soclub.screens.signin.SigninScreen
import com.example.soclub.screens.start.StartScreen
import com.example.soclub.screens.signup.SignupScreen
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
            when {
                currentScreen.startsWith("detail") -> { // Check if the current screen is a detail screen
                    TopBar(navController, title = "Aktivitet", showBackButton = true)
                }

                currentScreen == AppScreens.SIGNUP.name -> {
                    TopBar(navController, title = "", showBackButton = true)
                }

                currentScreen == AppScreens.SIGNIN.name -> {
                    TopBar(navController, title = "", showBackButton = true)
                }

                currentScreen == AppScreens.RESET_PASSWORD.name -> {
                    TopBar(navController, title = "", showBackButton = true)
                }

                currentScreen == AppScreens.HOME.name -> {
                    HomeTopBar(navController, title = "SoClub")
                }

                currentScreen == AppScreens.PROFILE.name -> {
                    TopBar(navController, title = "Profil", showBackButton = false)
                }

                currentScreen == AppScreens.NOTIFICATIONS.name -> {
                    TopBar(navController, title = "Varslinger", showBackButton = false)
                }

                currentScreen == AppScreens.ADS.name -> {
                    TopBar(navController, title = "Mine annonser", showBackButton = true)
                }

                currentScreen == AppScreens.NEW_ACTIVITY.name -> {
                    TopBar(navController, title = "Legg til aktivitet", showBackButton = false)
                }

                currentScreen == AppScreens.EDIT_PROFILE.name -> {
                    TopBar(navController, title = "Endre Profil", showBackButton = true)
                }

                currentScreen == AppScreens.CHANGE_PASSWORD.name -> {
                    TopBar(navController, title = "Endre passord", showBackButton = true)
                }

                currentScreen == AppScreens.EDIT_PERMISSION.name -> {
                    TopBar(navController, title = "Endre tillatelser", showBackButton = true)
                }

                currentScreen == AppScreens.ENTRIES.name -> {
                    TopBar(navController, title = "Mine Påmeldinger", showBackButton = false)
                }

                currentScreen.startsWith("editActivity") -> { // Check if the current screen is a detail screen
                    TopBar(navController, title = "Endre aktivitet", showBackButton = true)
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
                AppScreens.START.name,
                AppScreens.RESET_PASSWORD.name
            )

            if (currentScreen !in screensWithoutBottomBar) {
                BottomNavBar(
                    navController = navController,
                    currentScreen = if (currentScreen.startsWith("detail")) AppScreens.HOME.name else currentScreen
                )

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
            composable(AppScreens.RESET_PASSWORD.name) {
                ResetPasswordScreen(navController)
            }
            composable(AppScreens.HOME.name) {
                HomeScreen(navController)
            }
            composable(
                route = "detail/{category}/{activityId}",
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("activityId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                val activityId = backStackEntry.arguments?.getString("activityId")
                ActivityDetailScreen(
                    navController = navController,
                    category = category,
                    activityId = activityId
                )
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
            composable(AppScreens.NOTIFICATIONS.name) {
                NotificationsScreen(navController)
            }
            composable(AppScreens.NEW_ACTIVITY.name) {
                NewActivityScreen(navController)
            }
            composable(AppScreens.ADS.name) {
                AdsScreen(navController)
            }


            composable(
                route = "editActivity/{category}/{activityId}",
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("activityId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                val activityId = backStackEntry.arguments?.getString("activityId")

                if (category != null && activityId != null) {
                    EditActivityScreen(
                        navController = navController,
                        category = category,
                        activityId = activityId
                    )
                }
            }
        }
    }
}
