package com.example.soclub.service

import com.example.soclub.models.Activity
import com.example.soclub.models.createActivity
import com.example.soclub.models.editActivity

interface ActivityService {
    suspend fun getActivityById(category: String, activityId: String): Activity?
    suspend fun createActivity(category: String, activity: createActivity)
    suspend fun getActivities(category: String): List<Activity>
    suspend fun getCategories(): List<String>
    suspend fun getAllActivitiesByCreator(creatorId: String): List<editActivity>
    suspend fun updateActivity(category: String, newCategory: String, activityId: String, updatedActivity: createActivity)
    suspend fun deleteActivity(category: String, activityId: String) // Ny funksjon for å slette en aktivitet
}
