package com.example.soclub.service

import com.example.soclub.models.Activity
import com.example.soclub.models.createActivity
import com.example.soclub.models.editActivity

interface ActivityService {
    suspend fun getActivityById(category: String, activityId: String): Activity?
    suspend fun createActivity(category: String, activity: createActivity) // Opprett aktivitet i en kategori
    suspend fun getActivities(category: String): List<Activity>            // Hent aktiviteter for en kategori
    suspend fun getCategories(): List<String>                              // Hent alle kategorier
    suspend fun getAllActivitiesByCreator(creatorId: String): List<editActivity> // Hent alle aktiviteter for en gitt bruker
    suspend fun updateActivity(category: String, newCategory: String, activityId: String, updatedActivity: createActivity)
}
