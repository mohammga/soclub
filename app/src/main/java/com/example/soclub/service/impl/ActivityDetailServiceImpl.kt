package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityDetaillService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class ActivityDetailServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ActivityDetaillService {


    override suspend fun getActivityById(category: String, activityId: String): Activity? {
        val documentSnapshot = firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .get()
            .await()

        val activity = documentSnapshot.toObject(Activity::class.java)

        // Hent full adresse
        val fullLocation = activity?.location ?: "Ukjent"
        val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

        // Hent dato fra dokumentet
        val date = documentSnapshot.getTimestamp("date")

        // Hent startTime som String fra dokumentet
        val startTime = documentSnapshot.getString("startTime") ?: "Ukjent tid"

        // Returner aktiviteten med date, restOfAddress, og startTime
        return activity?.copy(
            restOfAddress = restOfAddress,
            date = date,
            startTime = startTime // Legg til startTime her
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

                for (document in registrationRef.documents) {
                    firestore.collection("registrations")
                        .document(document.id)
                        .update(mapOf(
                            "status" to status,
                        )).await()
                }
            } else {

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

    override fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit) {
        firestore.collection("registrations")
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





}