package com.example.soclub.service
import com.example.soclub.models.Activity

interface EntriesService {
    suspend fun getActiveActivitiesForUser(userId: String, onUpdate: (List<Activity>) -> Unit)
    suspend fun getNotActiveActivitiesForUser(userId: String, onUpdate: (List<Activity>) -> Unit)
}