package com.example.soclub.service

import com.example.soclub.models.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining notification-related operations for the SoClub application.
 *
 * This interface abstracts the functionalities for managing user notifications,
 * allowing different implementations (e.g., Firebase, Room, custom backend) to be used interchangeably.
 */
interface NotificationService {

    /**
     * Saves a new notification to the data source.
     *
     * @param notification The [Notification] object to be saved.
     *
     * @throws [Exception] if the operation fails.
     */
    suspend fun saveNotification(notification: Notification)

    /**
     * Retrieves all notifications from the data source.
     *
     * @return A [List] of [Notification] objects representing all saved notifications.
     *
     * @throws [Exception] if the retrieval fails.
     */
    suspend fun getAllNotifications(): List<Notification>

    /**
     * Deletes a specific notification from the data source.
     *
     * @param notification The [Notification] object to be deleted.
     *
     * @throws [Exception] if the deletion fails.
     */
    suspend fun deleteNotification(notification: Notification)

    /**
     * Provides a reactive stream of notifications that updates in real-time.
     *
     * This [Flow] emits a new [List] of [Notification] objects whenever there is a change
     * in the notifications data source, such as additions or deletions.
     *
     * @return A [Flow] emitting [List] of [Notification] objects.
     */
    fun getNotificationsStream(): Flow<List<Notification>>
}
