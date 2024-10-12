package com.example.soclub.service

import com.example.soclub.models.Activity

interface ActivityDetaillService {
    suspend fun getActivityById(category: String, activityId: String): Activity?
    suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean
    suspend fun updateRegistrationStatus(userId: String, activityId: String, status: String): Boolean
    suspend fun updateMaxParticipants(category: String, activityId: String, updatedMaxParticipants: Int): Boolean  // Legg til kategori
    suspend fun getRegisteredParticipantsCount(activityId: String): Int // Legg til denne funksjonen
    fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit)  // Ny funksjon for live-oppdateringer




}