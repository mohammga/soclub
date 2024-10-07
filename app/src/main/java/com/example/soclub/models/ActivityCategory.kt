package com.example.soclub.models

data class ActivityCategory(
    val categoryName: String = "",
    val activities: List<Activity> = listOf()
)