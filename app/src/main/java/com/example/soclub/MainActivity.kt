// Fil: MainActivity.kt
package com.example.soclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.soclub.components.navigation.AppNavigation
//import com.example.soclub.models.sendAllCategoriesAndActivitiesToFirestore
//import com.example.soclub.models.deleteAllActivitiesFromCategories
import com.example.soclub.screens.noInternet.NoInternetScreen
import com.example.soclub.ui.theme.SoClubTheme
import com.example.soclub.ui.theme.ThemeMode
import com.example.soclub.utils.NetworkHelper
import com.example.soclub.utils.PermissionHelper
import com.example.soclub.utils.requestExactAlarmPermissionIfNeeded
import com.example.soclub.screens.activityDetail.ActivityDetailViewModel
import com.example.soclub.service.ActivityService
import com.example.soclub.utils.ThemePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityService: ActivityService

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var networkHelper: NetworkHelper

    private val activityDetailViewModel: ActivityDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionHelper.updatePermissionsStatus(permissions)
        }

        permissionHelper = PermissionHelper(this, permissionLauncher)
        networkHelper = NetworkHelper(this)

        permissionHelper.requestPermissions()

        activityDetailViewModel.requestAlarmPermission.observe(this) { shouldRequest ->
            if (shouldRequest) {
                requestExactAlarmPermissionIfNeeded(this)
                activityDetailViewModel.resetAlarmPermissionRequest()
            }
        }
        //deleteAllActivitiesFromCategories()
        //sendAllCategoriesAndActivitiesToFirestore()

        setContent {
            val context = LocalContext.current
            val themePreferenceManager = remember { ThemePreferenceManager(context) }
            val themeMode by themePreferenceManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val coroutineScope = rememberCoroutineScope()

            SoClubTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
            ) {
                val navController = rememberNavController()
                val hasInternetConnection by remember { networkHelper::hasInternetConnection }

                if (hasInternetConnection) {
                    AppNavigation(
                        navController = navController,
                        themeMode = themeMode,
                        onThemeChange = { newThemeMode ->
                            coroutineScope.launch {
                                themePreferenceManager.saveThemeMode(newThemeMode)
                            }
                        }
                    )
                } else {
                    NoInternetScreen()
                }
            }
        }
    }
}
