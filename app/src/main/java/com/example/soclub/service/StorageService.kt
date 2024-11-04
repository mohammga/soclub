package com.example.soclub.service

import android.net.Uri

interface StorageService {
    fun uploadImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    )
}
