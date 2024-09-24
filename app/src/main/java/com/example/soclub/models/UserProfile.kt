package com.example.soclub.models
import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId val id: String = "",
    val name: String = "",
    val age: String = "",
)