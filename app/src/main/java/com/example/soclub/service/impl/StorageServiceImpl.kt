package com.example.soclub.service.impl

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

/**
 * Implementation of the [StorageService] interface, responsible for handling image uploads
 * to Firebase Storage.
 *
 * @property firebaseStorage Instance of FirebaseStorage used for uploading files.
 */
class StorageServiceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
) : StorageService {

    /**
     * Uploads an image to Firebase Storage.
     *
     * @param imageUri The [Uri] of the image to upload.
     * @param isActivity A flag indicating if the upload is related to an activity or a user.
     *                   If `true`, the image is stored under the category folder; otherwise,
     *                   it is stored under the "User" folder.
     * @param category The category folder name for storing the image if `isActivity` is `true`.
     * @param onSuccess Callback invoked when the image is successfully uploaded.
     *                  The callback provides the download URL of the uploaded image as a [String].
     * @param onError Callback invoked when an error occurs during the upload.
     *                The callback provides the exception [Exception] that caused the error.
     */
    override fun uploadImage(
        imageUri: Uri,
        isActivity: Boolean,
        category: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Launch a coroutine on the IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Determine the folder path based on the isActivity flag
                val folderPath = if (isActivity) {
                    "$category/${imageUri.lastPathSegment}"
                } else {
                    "User/${imageUri.lastPathSegment}"
                }

                // Reference to the storage location in Firebase
                val storageRef = firebaseStorage.reference.child(folderPath)

                // Upload the file to Firebase Storage and await its completion
                storageRef.putFile(imageUri).await()

                // Get the download URL of the uploaded file
                val downloadUrl = storageRef.downloadUrl.await()

                // Switch to the main dispatcher to invoke the onSuccess callback
                withContext(Dispatchers.Main) {
                    onSuccess(downloadUrl.toString())
                }
            } catch (e: Exception) {
                // Log the error and invoke the onError callback
                Log.e("StorageServiceImpl", "Error uploading image: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}


