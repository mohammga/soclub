package com.example.soclub.service

import android.net.Uri

/**
 * Interface defining storage-related operations for the SoClub application.
 *
 * This interface abstracts the functionalities for managing media storage,
 * allowing different implementations (e.g., Firebase Storage, local storage) to be used interchangeably.
 */
interface StorageService {

    /**
     * Uploads an image to the designated storage location.
     *
     * This function handles the process of uploading an image from the provided [Uri] to the storage backend.
     * It supports categorizing images and provides callbacks for success and error handling.
     *
     * @param imageUri The [Uri] of the image to be uploaded.
     * @param isActivity A [Boolean] flag indicating whether the upload is initiated from an Activity context.
     *                   This can be used to manage context-specific operations or permissions.
     * @param category An optional [String] representing the category under which the image should be stored.
     *                 Defaults to an empty string if not provided.
     * @param onSuccess A callback function invoked upon successful upload.
     *                  Receives a [String] parameter representing the URL or identifier of the uploaded image.
     * @param onError A callback function invoked if an error occurs during the upload process.
     *                Receives an [Exception] detailing the cause of the failure.
     */
    fun uploadImage(
        imageUri: Uri,
        isActivity: Boolean,
        category: String = "",
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    )
}