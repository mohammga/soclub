package com.example.soclub

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.soclub.components.navigation.AppNavigation
import com.example.soclub.service.ActivityService
import com.example.soclub.ui.theme.SoclubTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Injiser ActivityService via Hilt
    @Inject
    lateinit var activityService: ActivityService

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionGranted = false
    private var isCameraPermissionGranted = false
    private var isPOST_NOTIFICATIONS = false

    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sjekk om det er internettforbindelse, hvis ikke, avslutt appen
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Ingen internettforbindelse. Appen avsluttes.", Toast.LENGTH_LONG).show()
            finish() // Avslutt appen hvis det ikke er nettverk
            return
        }

        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isPOST_NOTIFICATIONS = permissions[Manifest.permission.CAMERA]?:isPOST_NOTIFICATIONS
            isCameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: isCameraPermissionGranted
        }

        // Request permissions
        requestPermission()

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Set the content of the activity
        setContent {
            SoclubTheme {
                val navController = rememberNavController()
                AppNavigation(navController, activityService)  // Passer ActivityService til AppNavigation
            }
        }

        // Initialize ConnectivityManager for network monitoring
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Start monitoring network
        monitorNetworkConnection()
    }

    private fun requestPermission() {
        // Check permissions
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        isCameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        // Create a list of permissions to request
        val permissionRequest: MutableList<String> = ArrayList()

        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isPOST_NOTIFICATIONS) {
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!isCameraPermissionGranted) {
            permissionRequest.add(Manifest.permission.CAMERA)
        }

        // Launch the permission request if needed
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        // Intent to open the camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Check if there is an app that can handle this intent
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "No camera application found.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the image that was captured
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            // Here you can do something with the image, e.g. show it in an ImageView
            Toast.makeText(this, "Image captured!", Toast.LENGTH_SHORT).show()
        }


    }

    // Sjekk nettverkstilgang
    private fun isNetworkAvailable(): Boolean {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Monitor network connection
    private fun monitorNetworkConnection() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Handle network available
                Toast.makeText(this@MainActivity, "Nettverk tilgjengelig", Toast.LENGTH_SHORT).show()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val isUnmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                if (isUnmetered) {
                    Toast.makeText(this@MainActivity, "Nettverket er umålt", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Nettverket er målt", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Handle network lost
                Toast.makeText(this@MainActivity, "Nettverk mistet", Toast.LENGTH_SHORT).show()
            }
        }

        // Register the network callback
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}
