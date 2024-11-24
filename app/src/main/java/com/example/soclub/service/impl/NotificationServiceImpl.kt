package com.example.soclub.service.impl

import android.content.Context
import com.example.soclub.R
import com.example.soclub.models.Notification
import com.example.soclub.service.NotificationService
import com.example.soclub.service.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Implementation of the NotificationService interface.
 * Handles operations related to notifications, including saving, retrieving, deleting,
 * and streaming notifications from the Firestore database.
 *
 * @param firestore Firestore instance used to interact with the database.
 * @param accountService AccountService to retrieve the current user's ID.
 * @param context Android context for accessing resources such as strings.
 */
class NotificationServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService,
    private val context: Context
) : NotificationService {

    /**
     * Saves a notification to the Firestore database.
     *
     * @param notification The notification object to save.
     * @throws Exception if an error occurs during the save operation.
     */
    override suspend fun saveNotification(notification: Notification) {
        try {
            firestore.collection("notifications")
                .add(notification.toMap())
                .await()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_save_notification, e.message), e)
        }
    }

    /**
     * Retrieves all notifications for the current user.
     *
     * @return A list of notifications sorted by timestamp in descending order.
     * @throws Exception if an error occurs during the fetch operation.
     */
    override suspend fun getAllNotifications(): List<Notification> {
        val userId = accountService.currentUserId
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            return snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_fetch_notifications, e.message), e)
        }
    }

    /**
     * Deletes a specific notification for the current user from the Firestore database.
     *
     * @param notification The notification object to delete.
     * @throws Exception if an error occurs during the delete operation.
     */
    override suspend fun deleteNotification(notification: Notification) {
        val userId = accountService.currentUserId
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("message", notification.message)
                .whereEqualTo("timestamp", notification.timestamp)
                .whereEqualTo("activityId", notification.activityId)
                .get()
                .await()

            for (document in snapshot.documents) {
                document.reference.delete().await()
            }
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_delete_notification, e.message), e)
        }
    }

    /**
     * Streams notifications for the current user in real-time using a Firestore snapshot listener.
     *
     * @return A Flow emitting lists of notifications whenever the Firestore data changes.
     */
    override fun getNotificationsStream(): Flow<List<Notification>> = callbackFlow {
        val userId = accountService.currentUserId
        if (userId.isEmpty()) {
            trySend(emptyList()).isSuccess
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration: ListenerRegistration = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    this.close(
                        Exception(
                            context.getString(R.string.error_listening_notifications, error.message),
                            error
                        )
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                        .sortedByDescending { it.timestamp }
                    trySend(notifications).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
}

