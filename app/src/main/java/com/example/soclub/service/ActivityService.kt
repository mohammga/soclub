package com.example.soclub.service

import com.example.soclub.models.Activity
import com.example.soclub.models.createActivity

interface ActivityService {
    suspend fun createActivity(category: String, activity: createActivity)
    suspend fun getActivities(category: String): List<Activity>
    suspend fun getCategories(): List<String>
}
