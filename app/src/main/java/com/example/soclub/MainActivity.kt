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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // Legg til en tilstand for nettverksstatus
    private var hasInternetConnection by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isPOST_NOTIFICATIONS = permissions[Manifest.permission.CAMERA] ?: isPOST_NOTIFICATIONS
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

                // Vis UI basert på nettverksstatus
                if (hasInternetConnection) {
                    // Hovedinnholdet vises hvis det er internett
                    AppNavigation(navController, activityService)
                } else {
                    // Vis "ingen internett"-skjermen hvis det ikke er internett
                    NoInternetScreen(onRetryClick = {
                        // Når brukeren klikker "Prøv igjen", sjekk om internett er tilbake
                        if (isNetworkAvailable()) {
                            hasInternetConnection = true
                        }
                    })
                }
            }
        }

        // Initialize ConnectivityManager for network monitoring
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Sjekk nettverk ved oppstart
        if (!isNetworkAvailable()) {
            hasInternetConnection = false
        }

        // Start monitoring network changes during app usage
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

    // Monitor network connection during app usage
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
                hasInternetConnection = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Handle network lost, show "no internet" UI
                hasInternetConnection = false
            }
        }

        // Register the network callback
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}

@Composable
fun NoInternetScreen(onRetryClick: () -> Unit) {
    // Layout som vises når det ikke er internettforbindelse
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Ingen internettforbindelse", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetryClick) {
            Text(text = "Prøv igjen")
        }
    }
}
