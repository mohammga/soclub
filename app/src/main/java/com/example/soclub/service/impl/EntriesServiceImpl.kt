package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EntriesServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EntriesService {

    override suspend fun getActiveActivitiesForUser(userId: String): List<Activity> {
        // Hent alle registreringer med status "aktiv" for brukeren
        val registrations = firestore.collection("registrations")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "aktiv")
            .get()
            .await()

        val activityList = mutableListOf<Activity>()

        for (document in registrations.documents) {
            val activityId = document.getString("activityId") ?: continue

            // For hvert activityId, hent aktiviteten fra activities-samlingen
            val activitySnapshot = firestore.collectionGroup("activities")
                .whereEqualTo("id", activityId)
                .get()
                .await()

            if (!activitySnapshot.isEmpty) {
                val activity = activitySnapshot.documents.first().toObject(Activity::class.java)
                if (activity != null) {
                    activityList.add(activity)
                }
            }
        }
        return activityList
    }
}
