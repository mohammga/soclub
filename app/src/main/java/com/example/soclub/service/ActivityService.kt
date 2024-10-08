package com.example.soclub.service

import com.example.soclub.models.Activity

interface ActivityService {
    suspend fun createActivity(category: String, activity: Activity)
    suspend fun getActivities(category: String): List<Activity>
    suspend fun getCategories(): List<String>
    suspend fun updateActivity(category: String, documentId: String, activity: Activity)
    suspend fun deleteActivity(category: String, documentId: String)
    suspend fun getActivityById(category: String, activityId: String): Activity?


    // Nye metoder for påmelding og avmelding
    suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean
    suspend fun registerUserForActivity(userId: String, activityId: String)
    suspend fun unregisterUserFromActivity(userId: String, activityId: String)
    suspend fun updateRegistrationStatus(userId: String, activityId: String, status: String): Boolean
    suspend fun createRegistration(userId: String, activityId: String, status: String, timestamp: Long)

}
