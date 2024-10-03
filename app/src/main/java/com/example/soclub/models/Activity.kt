package com.example.soclub.models

data class Activity(
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: String = "",
    val maxParticipants: Int = 0,
    val location: String = ""
)