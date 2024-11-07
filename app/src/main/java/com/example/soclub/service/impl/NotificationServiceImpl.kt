package com.example.soclub.service.impl

import com.example.soclub.models.Notification
import com.example.soclub.service.NotificationService
import com.example.soclub.service.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.ListenerRegistration

class NotificationServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : NotificationService {

    override suspend fun saveNotification(notification: Notification) {
        firestore.collection("notifications")
            .add(notification.toMap())
            .await()
    }

    override suspend fun getAllNotifications(): List<Notification> {
        val userId = accountService.currentUserId
        val snapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
            .sortedByDescending { it.timestamp }
    }

    override suspend fun deleteNotification(notification: Notification) {
        val userId = accountService.currentUserId
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
    }

    override fun getNotificationsStream(): Flow<List<Notification>> = callbackFlow {
        val userId = accountService.currentUserId
        val listenerRegistration: ListenerRegistration = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                    trySend(notifications).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
}
