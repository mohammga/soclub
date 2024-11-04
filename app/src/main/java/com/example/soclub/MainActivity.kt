package com.example.soclub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.soclub.components.navigation.AppNavigation
import com.example.soclub.screens.noInternet.NoInternetScreen
import com.example.soclub.service.ActivityService
import com.example.soclub.ui.theme.SoclubTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityService: ActivityService

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionGranted = false
    private var isGalleryPermissionGranted = false // Updated from camera to gallery
    private var isPOST_NOTIFICATIONS = false

    private lateinit var connectivityManager: ConnectivityManager
    private var hasInternetConnection by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isPOST_NOTIFICATIONS = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: isPOST_NOTIFICATIONS

            // Updated from camera permission to gallery permission
            isGalleryPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: isGalleryPermissionGranted
            } else {
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isGalleryPermissionGranted
            }
        }

        requestPermission()

        enableEdgeToEdge()

        setContent {
            SoclubTheme {
                val navController = rememberNavController()

                if (hasInternetConnection) {
                    AppNavigation(navController)
                } else {
                    NoInternetScreen(onRetryClick = {
                        if (isNetworkAvailable()) {
                            hasInternetConnection = true
                        }
                    })
                }
            }
        }

        // Initialize ConnectivityManager for network monitoring
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check network at startup
        if (!isNetworkAvailable()) {
            hasInternetConnection = false
        }

        // Start monitoring network changes
        monitorNetworkConnection()
    }

    private fun requestPermission() {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // Updated from camera to gallery permission
        isGalleryPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isPOST_NOTIFICATIONS) {
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request appropriate gallery permissions based on Android version
        if (!isGalleryPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun isNetworkAvailable(): Boolean {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun monitorNetworkConnection() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                hasInternetConnection = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                hasInternetConnection = false
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}
