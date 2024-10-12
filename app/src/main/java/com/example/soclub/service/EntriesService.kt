package com.example.soclub.service

import com.example.soclub.models.Activity

interface EntriesService {
    suspend fun getActiveActivitiesForUser(userId: String): List<Activity>
}