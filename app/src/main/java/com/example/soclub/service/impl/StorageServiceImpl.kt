package com.example.soclub.service.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.soclub.service.StorageService
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StorageServiceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val context: Context
) : StorageService {

    override fun uploadImage(
        imageUri: Uri,
        isActivity: Boolean,
        category: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Determine folder based on whether it's an activity or a user profile image
                val folderPath = if (isActivity) {
                    "$category/${imageUri.lastPathSegment}"
                } else {
                    "User/${imageUri.lastPathSegment}"
                }

                val storageRef = firebaseStorage.reference.child(folderPath)

                // Upload the file
                storageRef.putFile(imageUri).await()

                // Retrieve the download URL
                val downloadUrl = storageRef.downloadUrl.await()

                // Call onSuccess on the main thread
                withContext(Dispatchers.Main) {
                    onSuccess(downloadUrl.toString())
                }
            } catch (e: Exception) {
                Log.e("StorageServiceImpl", "Error uploading image: ${e.message}", e)

                // Call onError on the main thread
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}
