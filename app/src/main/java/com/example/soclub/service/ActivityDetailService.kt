package com.example.soclub.service

import com.example.soclub.models.Activity
import com.google.firebase.firestore.ListenerRegistration

interface ActivityDetailService {
    suspend fun getActivityById(category: String, activityId: String): Activity?
    suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean
    suspend fun updateRegistrationStatus(userId: String, activityId: String, status: String): Boolean
    suspend fun getRegisteredParticipantsCount(activityId: String): Int
    fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit): ListenerRegistration
    fun listenForActivityUpdates(category: String, activityId: String, onUpdate: (Activity) -> Unit): ListenerRegistration


}