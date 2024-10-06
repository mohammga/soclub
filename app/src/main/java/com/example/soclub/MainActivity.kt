package com.example.soclub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.soclub.components.navigation.AppNavigation
import com.example.soclub.ui.theme.SoclubTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionGranted = false
    private var isCameraPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isCameraPermissionGranted = permissions[Manifest.permission.CAMERA]?:isCameraPermissionGranted

        }

        // Request permissions
        requestPermission()

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Set the content of the activity
        setContent {
            SoclubTheme {
                AppNavigation()
            }
        }
    }

    private fun requestPermission() {
        // Check permissions
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // Create a list of permissions to request
        val permissionRequest: MutableList<String> = ArrayList()

        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isCameraPermissionGranted){
            permissionRequest.add(Manifest.permission.CAMERA)
        }
        // Launch the permission request if needed
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }else{
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



}

