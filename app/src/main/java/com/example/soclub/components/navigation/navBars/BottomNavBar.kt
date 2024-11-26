package com.example.soclub.components.navigation.navBars

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R
import com.example.soclub.components.navigation.AppScreens

data class BottomNavItems(val route: AppScreens, val icon: ImageVector, @StringRes val label: Int, val showBadge: Boolean = false)

val shortcuts = listOf(
    BottomNavItems(AppScreens.HOME, Icons.Default.Home, R.string.home),
    BottomNavItems(AppScreens.NOTIFICATIONS, Icons.Default.Notifications, R.string.notifications, showBadge = true),
    BottomNavItems(AppScreens.NEW_ACTIVITY, Icons.Default.AddCircleOutline, R.string.new_activity),
    BottomNavItems(AppScreens.ENTRIES, Icons.Default.DateRange, R.string.entries),
    BottomNavItems(AppScreens.PROFILE, Icons.Default.AccountCircle, R.string.profile)
)

@Composable
fun BottomNavBar(
    navController: NavController,
    currentScreen: String,
    notificationCount: Int
) {
    val profileScreens = setOf(
        AppScreens.PROFILE.name,
        AppScreens.EDIT_PROFILE.name,
        AppScreens.CHANGE_PASSWORD.name,
        AppScreens.EDIT_PERMISSION.name,
        AppScreens.ADS.name
    )

    NavigationBar {
        shortcuts.forEach { shortcut ->
            val isSelected = when (shortcut.route) {
                AppScreens.PROFILE -> currentScreen.startsWith("editActivity") || currentScreen in profileScreens
                AppScreens.HOME -> currentScreen == AppScreens.HOME.name
                else -> currentScreen == shortcut.route.name
            }

            NavigationBarItem(
                icon = {
                    if (shortcut.route == AppScreens.NOTIFICATIONS && notificationCount > 0) {
                        BadgedBox(badge = { Badge { Text(notificationCount.toString()) } }) {
                            Icon(shortcut.icon, contentDescription = stringResource(shortcut.label))
                        }
                    } else {
                        Icon(shortcut.icon, contentDescription = stringResource(shortcut.label))
                    }
                },
                label = {
                    Text(
                        text = stringResource(shortcut.label),
                        fontSize = 10.sp,
                    )
                },
                selected = isSelected,
                onClick = {
                    when {
                        shortcut.route == AppScreens.PROFILE && (currentScreen.startsWith("editActivity") || currentScreen in profileScreens) -> {
                            navController.navigate(AppScreens.PROFILE.name) {
                                popUpTo(AppScreens.PROFILE.name) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        shortcut.route == AppScreens.HOME -> {
                            navController.navigate(AppScreens.HOME.name) {
                                popUpTo(AppScreens.HOME.name) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        else -> {
                            navController.navigate(shortcut.route.name) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun getCurrentScreen(navController: NavController): String {
    return navController.currentBackStackEntryAsState().value?.destination?.route.toString()
}

@Preview
@Composable
fun BottomNavBarPreview() {
    BottomNavBar(
        navController = rememberNavController(),
        currentScreen = AppScreens.HOME.name,
        notificationCount = 0
    )
}
