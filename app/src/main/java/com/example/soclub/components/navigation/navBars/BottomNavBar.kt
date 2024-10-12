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
fun BottomNavBar(navController: NavController, currentScreen: String) {
    NavigationBar {
        shortcuts.forEach { shortcut ->
            NavigationBarItem(
                icon = {
                    if (shortcut.showBadge) {
                        BadgedBox(badge = { Badge { Text("5") } }) {
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
                selected = currentScreen == shortcut.route.name, // Bruk currentScreen for å avgjøre aktivt ikon
                onClick = {
                    navController.navigate(shortcut.route.name) {
                        // Fjern historikk for å unngå dubletter
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
    // Bruk en statisk verdi som "Hjem" for testing og forhåndsvisning
    BottomNavBar(
        navController = rememberNavController(),
        currentScreen = AppScreens.HOME.name // Pass på å sette en verdi her, f.eks. "Hjem"
    )
}
