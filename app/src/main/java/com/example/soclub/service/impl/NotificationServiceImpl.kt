package com.example.soclub.service.impl

import com.example.soclub.models.Notification
import com.example.soclub.service.NotificationService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow

class NotificationServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationService {

    override suspend fun saveNotification(notification: Notification) {
        firestore.collection("notifications")
            .add(notification.toMap())
            .await()
    }

    override suspend fun getAllNotifications(): List<Notification> {
        val snapshot = firestore.collection("notifications").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
    }

    override suspend fun deleteNotification(notification: Notification) {
        val snapshot = firestore.collection("notifications")
            .whereEqualTo("message", notification.message)
            .whereEqualTo("timestamp", notification.timestamp)
            .get()
            .await()

        for (document in snapshot.documents) {
            document.reference.delete().await()
        }
    }

    override fun getNotificationsStream(): Flow<List<Notification>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = firestore.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow if there’s an error
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                    trySend(notifications) // Send the updated list to the flow
                }
            }
        awaitClose { listenerRegistration.remove() } // Remove the listener when the flow is closed
    }
}
