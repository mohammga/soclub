package com.example.soclub.service

import com.example.soclub.models.Activity
import com.example.soclub.models.createActivity

interface ActivityService {
    suspend fun createActivity(category: String, activity: createActivity) // Opprett aktivitet i en kategori
    suspend fun getActivities(category: String): List<Activity>            // Hent aktiviteter for en kategori
    suspend fun getCategories(): List<String>                              // Hent alle kategorier
    suspend fun getAllActivitiesByCreator(creatorId: String): List<Activity> // Hent alle aktiviteter for en gitt bruker
}
