package com.example.soclub.components.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.soclub.R
import com.example.soclub.components.navigation.navBars.BottomNavBar
import com.example.soclub.components.navigation.navBars.HomeTopBar
import com.example.soclub.components.navigation.navBars.TopBar
import com.example.soclub.components.navigation.navBars.getCurrentScreen
import com.example.soclub.screens.activityDetail.ActivityDetailScreen
import com.example.soclub.screens.ads.AdsScreen
import com.example.soclub.screens.changePassword.ChangePasswordScreen
import com.example.soclub.screens.editActivity.EditActivityScreen
import com.example.soclub.screens.editPermission.EditPermissionScreen
import com.example.soclub.screens.editProfile.EditProfileScreen
import com.example.soclub.screens.entries.EntriesScreen
import com.example.soclub.screens.home.HomeScreen
import com.example.soclub.screens.newActivity.NewActivityScreen
import com.example.soclub.screens.notifications.NotificationsScreen
import com.example.soclub.screens.notifications.NotificationsViewModel
import com.example.soclub.screens.profile.ProfileScreen
import com.example.soclub.screens.resetPassword.ResetPasswordScreen
import com.example.soclub.screens.signin.SigninScreen
import com.example.soclub.screens.signup.SignupScreen
import com.example.soclub.screens.start.StartScreen
import com.example.soclub.screens.termsPrivacy.TermsPrivacyScreen
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.module.FirebaseModule
//hentet fra eksempel i forelesning/github
@Composable
fun AppNavigation(
    navController: NavHostController,
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {
    val currentScreen = getCurrentScreen(navController)

    val notificationCount by notificationsViewModel.notificationCount.collectAsState()


    val profileScreens = setOf(
        AppScreens.PROFILE.name,
        AppScreens.EDIT_PROFILE.name,
        AppScreens.CHANGE_PASSWORD.name,
        AppScreens.EDIT_PERMISSION.name,
        AppScreens.ADS.name,
        "editActivity"
    )

    val adjustedCurrentScreen = when {
        currentScreen.startsWith("detail") -> AppScreens.HOME.name
        currentScreen in profileScreens || currentScreen.startsWith("editActivity") -> AppScreens.PROFILE.name
        else -> currentScreen
    }

    val context = LocalContext.current
    val auth = remember { FirebaseModule.auth() }
    val firestore = remember { FirebaseModule.firestore() }
    val accountService = remember { AccountServiceImpl(auth, firestore, context) }

    Scaffold(
        topBar = {
            when {
                currentScreen.startsWith("detail") -> {
                    TopBar(navController, title = stringResource(R.string.activity), showBackButton = true)
                }
                currentScreen == AppScreens.SIGNUP.name -> {
                    TopBar(navController, title = "", showBackButton = true)
                }
                currentScreen == AppScreens.TERMS_PRIVACY.name -> {
                    TopBar(navController, title = stringResource(R.string.terms), showBackButton = true)
                }
                currentScreen == AppScreens.SIGNIN.name -> {
                    TopBar(navController, title = "", showBackButton = true)
                }
                currentScreen == AppScreens.RESET_PASSWORD.name -> {
                    TopBar(navController, title = "", showBackButton = true)
                }
                currentScreen == AppScreens.HOME.name -> {
                    HomeTopBar(title = stringResource(R.string.appname))
                }
                currentScreen == AppScreens.PROFILE.name -> {
                    TopBar(navController, title = stringResource(R.string.profile), showBackButton = false)
                }
                currentScreen == AppScreens.NOTIFICATIONS.name -> {
                    TopBar(navController, title = stringResource(R.string.notifications), showBackButton = false)
                }
                currentScreen == AppScreens.ADS.name -> {
                    TopBar(navController, title = stringResource(R.string.myAdd), showBackButton = true)
                }
                currentScreen == AppScreens.NEW_ACTIVITY.name -> {
                    TopBar(navController, title = stringResource(R.string.add_activity), showBackButton = false)
                }
                currentScreen == AppScreens.EDIT_PROFILE.name -> {
                    TopBar(navController, title = stringResource(R.string.change_profile), showBackButton = true)
                }
                currentScreen == AppScreens.CHANGE_PASSWORD.name -> {
                    TopBar(navController, title = stringResource(R.string.change_password), showBackButton = true)
                }
                currentScreen == AppScreens.EDIT_PERMISSION.name -> {
                    TopBar(navController, title = stringResource(R.string.editpermission), showBackButton = true)
                }
                currentScreen == AppScreens.ENTRIES.name -> {
                    TopBar(navController, title = stringResource(R.string.myEntries), showBackButton = false)
                }
                currentScreen.startsWith("editActivity") -> {
                    TopBar(navController, title = stringResource(R.string.editAktvititi), showBackButton = true)
                }
                else -> { /* Håndter andre skjermer */ }
            }
        },
        bottomBar = {
            val screensWithoutBottomBar = setOf(
                AppScreens.SIGNIN.name,
                AppScreens.SIGNUP.name,
                AppScreens.START.name,
                AppScreens.RESET_PASSWORD.name,
                AppScreens.TERMS_PRIVACY.name
            )

            if (currentScreen !in screensWithoutBottomBar) {
                BottomNavBar(
                    navController = navController,
                    currentScreen = adjustedCurrentScreen,
                    notificationCount = notificationCount
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (accountService.hasUser) AppScreens.HOME.name else AppScreens.START.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreens.START.name) { StartScreen(navController) }
            composable(AppScreens.SIGNUP.name) { SignupScreen(navController) }
            composable(AppScreens.SIGNIN.name) { SigninScreen(navController) }
            composable(AppScreens.RESET_PASSWORD.name) { ResetPasswordScreen() }
            composable(AppScreens.HOME.name) { HomeScreen(navController) }
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
                    category = category,
                    activityId = activityId
                )
            }
            composable(AppScreens.ENTRIES.name) { EntriesScreen(navController) }
            composable(AppScreens.PROFILE.name) { ProfileScreen(navController) }
            composable(AppScreens.CHANGE_PASSWORD.name) { ChangePasswordScreen() }
            composable(AppScreens.EDIT_PROFILE.name) { EditProfileScreen(navController) }
            composable(AppScreens.EDIT_PERMISSION.name) {
                EditPermissionScreen()
            }
            composable(AppScreens.NOTIFICATIONS.name) { NotificationsScreen() }
            composable(AppScreens.NEW_ACTIVITY.name) { NewActivityScreen(navController) }
            composable(AppScreens.ADS.name) { AdsScreen(navController) }
            composable(AppScreens.TERMS_PRIVACY.name) { TermsPrivacyScreen() }
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
