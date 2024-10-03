package com.example.soclub.service

import com.example.soclub.models.Activity

interface ActivityService {
    suspend fun createActivity(category: String, activity: Activity)
    suspend fun getActivities(category: String): List<Activity>
    suspend fun updateActivity(category: String, documentId: String, activity: Activity)
    suspend fun deleteActivity(category: String, documentId: String)
}