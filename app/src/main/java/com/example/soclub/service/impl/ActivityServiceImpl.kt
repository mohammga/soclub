package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.models.CreateActivity
import com.example.soclub.models.EditActivity
import com.example.soclub.service.ActivityService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivityServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ActivityService {

    override suspend fun createActivity(category: String, activity: CreateActivity) {
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

        val activity = documentSnapshot.toObject(Activity::class.java)

        val fullLocation = activity?.location ?: "Ukjent"
        val lastWord = fullLocation.substringAfterLast(" ")
        val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

        return activity?.copy(
            location = lastWord,
            restOfAddress = restOfAddress
        )
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


    override suspend fun getAllActivitiesByCreator(creatorId: String): List<EditActivity> {
        val categoriesSnapshot = firestore.collection("category").get().await()
        val allActivities = mutableListOf<EditActivity>()


        for (categoryDocument in categoriesSnapshot.documents) {
            val categoryName = categoryDocument.id


            val activitiesSnapshot = firestore.collection("category").document(categoryName)
                .collection("activities")
                .whereEqualTo("creatorId", creatorId)
                .get().await()


            activitiesSnapshot.documents.mapNotNullTo(allActivities) { document ->
                val activity = document.toObject(EditActivity::class.java)

                val fullLocation = activity?.location ?: "Ukjent"
                val lastWord = fullLocation.substringAfterLast(" ")
                val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

                activity?.copy(
                    id = document.id,
                    location = lastWord,
                    description = restOfAddress,
                    category = categoryName
                )
            }
        }

        return allActivities
    }


    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("category").get().await()
        val categories = snapshot.documents.map { document ->
            document.id
        }
        return categories.sortedByDescending { it == "Forslag" }
    }

    override suspend fun updateActivity(category: String, newCategory: String, activityId: String, updatedActivity: CreateActivity) {

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

    override suspend fun getAllActivities(): List<Activity> {
        val activityList = mutableListOf<Activity>()
        val categoriesSnapshot = firestore.collection("category").get().await()

        for (categoryDoc in categoriesSnapshot.documents) {
            val categoryName = categoryDoc.id
            val activitiesSnapshot = firestore.collection("category")
                .document(categoryDoc.id)
                .collection("activities")
                .get()
                .await()

            val activities = activitiesSnapshot.documents.mapNotNull { document ->
                val activity = document.toObject(Activity::class.java)
                activity?.copy(
                    id = document.id,
                    category = categoryName
                )
            }
            activityList.addAll(activities)
        }

        return activityList
    }


    override suspend fun getActivitiesGroupedByCategory(): Map<String, List<Activity>> {
        val categoriesSnapshot = firestore.collection("category").get().await()
        val activitiesByCategory = mutableMapOf<String, List<Activity>>()

        for (categoryDoc in categoriesSnapshot.documents) {
            val categoryName = categoryDoc.id
            val activitiesSnapshot = firestore.collection("category")
                .document(categoryName)
                .collection("activities")
                .get()
                .await()

            val activities = activitiesSnapshot.toObjects(Activity::class.java)
            activitiesByCategory[categoryName] = activities
        }

        return activitiesByCategory
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