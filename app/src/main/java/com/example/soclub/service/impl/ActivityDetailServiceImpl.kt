package com.example.soclub.service.impl

import android.content.Context
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityDetailService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration


class ActivityDetailServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,

) : ActivityDetailService {


    override suspend fun getActivityById(category: String, activityId: String): Activity? {
        val documentSnapshot = firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .get()
            .await()

        val activity = documentSnapshot.toObject(Activity::class.java)

        // Sørg for at 'creatorId' er hentet korrekt
        val creatorId = documentSnapshot.getString("creatorId") ?: ""

        // Hent andre nødvendige felt
        val fullLocation = activity?.location ?:"Ukjent"
        val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")
        val date = documentSnapshot.getTimestamp("date")
        val startTime = documentSnapshot.getString("startTime") ?: "Ukjent tid"

        // Returner aktiviteten med 'creatorId' satt
        return activity?.copy(
            restOfAddress = restOfAddress,
            date = date,
            startTime = startTime,
            creatorId = creatorId // Sett 'creatorId' her
        )
    }


    override suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean {
        val registrationRef = firestore.collection("registrations")
            .whereEqualTo("userId", userId)
            .whereEqualTo("activityId", activityId)
            .get().await()

        if (!registrationRef.isEmpty) {
            val document = registrationRef.documents.first()
            val status = document.getString("status")
            return status == "aktiv"
        }

        return false
    }



    override suspend fun updateRegistrationStatus(userId: String, activityId: String, status: String): Boolean {
        return try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            if (!registrationRef.isEmpty) {
                // Update existing registration
                for (document in registrationRef.documents) {
                    firestore.collection("registrations")
                        .document(document.id)
                        .update(mapOf("status" to status)).await()
                }
            } else {
                // Create a new registration entry
                val newRegistration = hashMapOf(
                    "userId" to userId,
                    "activityId" to activityId,
                    "status" to status,
                    "timestamp" to Timestamp(Date())
                )
                firestore.collection("registrations").add(newRegistration).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getRegisteredParticipantsCount(activityId: String): Int {
        val registrationRef = firestore.collection("registrations")
            .whereEqualTo("activityId", activityId)
            .whereEqualTo("status", "aktiv")
            .get().await()

        return registrationRef.size()
    }

    override fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("registrations")
            .whereEqualTo("activityId", activityId)
            .whereEqualTo("status", "aktiv")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val count = snapshot.size()
                    onUpdate(count)
                }
            }
    }

    override fun listenForActivityUpdates(category: String, activityId: String, onUpdate: (Activity) -> Unit): ListenerRegistration {
        return firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val activity = snapshot.toObject(Activity::class.java)
                if (activity != null) {
                    onUpdate(activity)
                }
            }
    }






}