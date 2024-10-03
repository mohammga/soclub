package com.example.soclub.models

data class ActivityCategory(
    val categoryName: String = "",  // Navnet på kategorien, f.eks. "Festivaler"
    val activities: List<Activity> = listOf()  // Liste over aktiviteter i denne kategorien
)