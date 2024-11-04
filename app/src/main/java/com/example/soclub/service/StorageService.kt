package com.example.soclub.service

import android.net.Uri

interface StorageService {
    fun uploadImage(
        imageUri: Uri,
        isActivity: Boolean,
        category: String = "",
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    )
}
