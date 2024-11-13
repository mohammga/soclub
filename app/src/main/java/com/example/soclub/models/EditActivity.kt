package com.example.soclub.models

import com.google.firebase.Timestamp

data class EditActivity(
    val id: String = "",
    val creatorId: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: Int = 0,
    val maxParticipants: Int = 0,
    val location: String = "",
    val date: Timestamp? = null,
    val startTime: String = "",
    val category: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastUpdated: Timestamp = Timestamp.now(),
)

