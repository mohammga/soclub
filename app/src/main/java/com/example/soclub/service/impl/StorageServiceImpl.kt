package com.example.soclub.service.impl

import android.net.Uri
import android.util.Log
import com.example.soclub.service.StorageService
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class StorageServiceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : StorageService {

    override fun uploadImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef = firebaseStorage.reference.child("images/${imageUri.lastPathSegment}")
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
