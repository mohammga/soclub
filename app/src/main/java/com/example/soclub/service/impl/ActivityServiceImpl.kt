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
                location = lastWord,
                description = restOfAddress
            )
        }
    }


    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("category").get().await()
        val categories = snapshot.documents.map { document ->
            document.id
        }
        return categories.sortedByDescending { it == "Forslag" }
    }
}
