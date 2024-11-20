package com.example.soclub.service.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.soclub.service.StorageService
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class StorageServiceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val context: Context
) : StorageService {

    override fun uploadImage(
        imageUri: Uri,
        isActivity: Boolean, // Added parameter to determine folder
        category: String,     // Added parameter for activity category
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Determine folder based on whether it's an activity or a user profile image
        val folderPath = if (isActivity) {
            "/$category/${imageUri.lastPathSegment}"
        } else {
            "User/${imageUri.lastPathSegment}"
        }

        val storageRef = firebaseStorage.reference.child(folderPath)
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("StorageServiceImpl", "Error uploading image: ${exception.message}")
                onError(exception)
            }
    }
}