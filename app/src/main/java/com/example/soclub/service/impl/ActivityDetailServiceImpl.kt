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

        val fullLocation = activity?.location ?: "Ukjent"
        val lastWord = fullLocation.substringAfterLast(" ")
        val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

        return activity?.copy(
            location = lastWord,
            restOfAddress = restOfAddress  // Fyll inn resten av adressen her
        )
    }


    override suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean {
        val registrationRef = firestore.collection("registrations")
            .whereEqualTo("userId", userId)
            .whereEqualTo("activityId", activityId)
            .get().await()

        return !registrationRef.isEmpty
    }


    override suspend fun updateRegistrationStatus(userId: String, activityId: String, status: String): Boolean {
        return try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            if (!registrationRef.isEmpty) {
                // Hvis registreringen allerede finnes, oppdater status og dato
                for (document in registrationRef.documents) {
                    firestore.collection("registrations")
                        .document(document.id)
                        .update(mapOf(
                            "status" to status,
                        )).await()
                }
            } else {
                // Hvis ingen registrering finnes, opprett en ny
                val newRegistration = hashMapOf(
                    "userId" to userId,
                    "activityId" to activityId,
                    "status" to status,
                    "timestamp" to Timestamp(Date()) // Lagre påmeldingsdato
                )

                firestore.collection("registrations").add(newRegistration).await()
            }
            true  // Operasjonen var vellykket
        } catch (e: Exception) {
            e.printStackTrace()
            false  // Noe gikk galt
        }
    }

    override suspend fun updateMaxParticipants(category: String, activityId: String, updatedMaxParticipants: Int): Boolean {
        return try {
            // Oppdater maxParticipants-feltet i riktig kategori og aktivitet i Firestore
            firestore.collection("category").document(category)
                .collection("activities").document(activityId)
                .update("maxParticipants", updatedMaxParticipants)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}