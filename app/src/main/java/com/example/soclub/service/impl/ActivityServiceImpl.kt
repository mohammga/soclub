package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.models.createActivity
import com.example.soclub.models.editActivity
import com.example.soclub.service.ActivityService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivityServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ActivityService {

    override suspend fun createActivity(category: String, activity: createActivity) {
        firestore.collection("category").document(category)
            .collection("activities").add(activity).await()
    }

    override suspend fun getActivityById(category: String, activityId: String): Activity? {
        val documentSnapshot = firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .get()
            .await()
        return documentSnapshot.toObject(Activity::class.java)
    }

    override suspend fun getActivities(category: String): List<Activity> {
        val snapshot = firestore.collection("category").document(category)
            .collection("activities").get().await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Activity::class.java)?.copy(id = document.id)
        }
    }

    override suspend fun getAllActivitiesByCreator(creatorId: String): List<editActivity> {
        val categoriesSnapshot = firestore.collection("category").get().await()
        val allActivities = mutableListOf<editActivity>()

        for (categoryDocument in categoriesSnapshot.documents) {
            val categoryName = categoryDocument.id

            val activitiesSnapshot = firestore.collection("category").document(categoryName)
                .collection("activities")
                .whereEqualTo("creatorId", creatorId)
                .get().await()

            activitiesSnapshot.documents.mapNotNullTo(allActivities) { document ->
                document.toObject(editActivity::class.java)?.copy(id = document.id, category = categoryName)
            }
        }

        return allActivities
    }

    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("category").get().await()
        return snapshot.documents.map { document -> document.id }
    }

    override suspend fun updateActivity(category: String, newCategory: String, activityId: String, updatedActivity: createActivity) {
        if (category != newCategory) {
            firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)
                .delete()
                .await()
            createActivity(newCategory, updatedActivity)
        } else {
            firestore.collection("category")
                .document(newCategory)
                .collection("activities")
                .document(activityId)
                .set(updatedActivity)
                .await()
        }
    }

    override suspend fun deleteActivity(category: String, activityId: String) {
        firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .delete()
            .await()
    }
}
