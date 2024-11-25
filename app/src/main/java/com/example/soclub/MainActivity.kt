package com.example.soclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.soclub.components.navigation.AppNavigation
import com.example.soclub.screens.noInternet.NoInternetScreen
import com.example.soclub.ui.theme.SoClubTheme
import com.example.soclub.utils.NetworkHelper
import com.example.soclub.utils.PermissionHelper
import com.example.soclub.utils.requestExactAlarmPermissionIfNeeded
import com.example.soclub.screens.activityDetail.ActivityDetailViewModel
import com.example.soclub.service.ActivityService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



/**
 * MainActivity serves as the entry point of the application.
 * It initializes required permissions, network monitoring, and sets up the UI content.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityService: ActivityService

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var networkHelper: NetworkHelper

    private val activityDetailViewModel: ActivityDetailViewModel by viewModels()

    /**
     * Called when the activity is first created. Initializes permissions, network helper, and alarm permissions.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this contains the most recent data supplied. Otherwise, it is null.
     */
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

        setContent {
            SoClubTheme {
                val navController = rememberNavController()
                val hasInternetConnection by remember { networkHelper::hasInternetConnection }

                if (hasInternetConnection) {
                    AppNavigation(navController = navController)
                } else {
                    NoInternetScreen()
                }
            }
        }
    }
}

