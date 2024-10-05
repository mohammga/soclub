package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivityServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ActivityService {

    override suspend fun createActivity(category: String, activity: Activity) {
        firestore.collection("activities").document(category)
            .collection("activities").add(activity).await()
    }


    override suspend fun getActivities(category: String): List<Activity> {
        val snapshot = firestore.collection("category").document(category)
            .collection("activities").get().await()

        return snapshot.documents.mapNotNull { document ->
            val activity = document.toObject(Activity::class.java)

            val fullLocation = activity?.location ?: "Ukjent"
            val lastWord = fullLocation.substringAfterLast(" ")
            val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

            activity?.copy(
                id = document.id,
                location = lastWord,  // Her setter vi siste ord som location
                description = restOfAddress // Bruker beskrivelsefeltet midlertidig for resten av adressen
            )
        }
    }


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




    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("category").get().await()

        // Hent alle kategorier (dokument-ID-er)
        val categories = snapshot.documents.map { document ->
            document.id  // Returnerer dokument-ID-ene som tilsvarer kategorinavnene
        }

        // Sorter slik at "Forslag" kommer først
        return categories.sortedByDescending { it == "Forslag" }
    }


    override suspend fun updateActivity(category: String, documentId: String, activity: Activity) {
        firestore.collection("activities").document(category)
            .collection("activities").document(documentId).set(activity).await()
    }

    override suspend fun deleteActivity(category: String, documentId: String) {
        firestore.collection("activities").document(category)
            .collection("activities").document(documentId).delete().await()
    }
}
