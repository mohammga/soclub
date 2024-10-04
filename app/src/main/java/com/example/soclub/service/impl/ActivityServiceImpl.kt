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
        val snapshot = firestore.collection("activities").document(category)
            .collection("activities").get().await()
        return snapshot.documents.mapNotNull { document ->
            document.toObject(Activity::class.java)
        }
    }

    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("activities").get().await()
        return snapshot.documents.map { document ->
            document.id  // Returnerer dokument-ID-ene som tilsvarer kategorinavnene
        }
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
