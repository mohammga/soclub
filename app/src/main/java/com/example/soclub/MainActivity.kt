package com.example.soclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.soclub.components.navigation.AppNavigation
import com.example.soclub.screens.noInternet.NoInternetScreen
import com.example.soclub.service.ActivityService
import com.example.soclub.ui.theme.SoClubTheme
import com.example.soclub.utils.NetworkHelper
import com.example.soclub.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityService: ActivityService

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var networkHelper: NetworkHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionHelper.updatePermissionsStatus(permissions)
        }

        permissionHelper = PermissionHelper(this, permissionLauncher)
        networkHelper = NetworkHelper(this)

        permissionHelper.requestPermissions()
        enableEdgeToEdge()

        setContent {
            SoClubTheme {
                val navController = rememberNavController()
                val hasInternetConnection by remember { networkHelper::hasInternetConnection }

                if (hasInternetConnection) {
                    AppNavigation(navController)
                } else {
                    NoInternetScreen()
                }
            }
        }
    }
}
