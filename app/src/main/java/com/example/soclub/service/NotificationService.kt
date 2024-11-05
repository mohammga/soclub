package com.example.soclub.service

import com.example.soclub.models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationService {
    suspend fun saveNotification(notification: Notification)
    suspend fun getAllNotifications(): List<Notification>
    suspend fun deleteNotification(notification: Notification)
    fun getNotificationsStream(): Flow<List<Notification>>
}
