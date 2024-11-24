package com.example.soclub.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

data class Activity(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: Int = 0,
    val maxParticipants: Int = 0,
    val location: String = "",
    val restOfAddress: String = "",
    val date: Timestamp? = null,
    val creatorId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val startTime: String = "",
    val category: String? = null,
    val lastUpdated: Timestamp = Timestamp.now()
)
