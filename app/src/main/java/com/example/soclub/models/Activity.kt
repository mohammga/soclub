package com.example.soclub.models

data class Activity(
    val imageUrl: String = "",  // URL til aktiviteten sitt bilde
    val title: String = "",     // Navn på aktiviteten
    val description: String = "",  // Kort beskrivelse
    val ageGroup: String = "",  // Aldersgruppe for aktiviteten
    val maxParticipants: Int = 0,  // Maks antall deltakere
    val location: String = ""   // Stedet hvor aktiviteten holdes
)