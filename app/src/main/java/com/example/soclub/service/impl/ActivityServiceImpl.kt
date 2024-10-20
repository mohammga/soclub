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

    // Opprett ny aktivitet
    override suspend fun createActivity(category: String, activity: createActivity) {
        firestore.collection("category").document(category)
            .collection("activities").add(activity).await()
    }

    // Hent en aktivitet basert på ID
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

    // Hent alle aktiviteter i en kategori
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

    // Hent alle aktiviteter opprettet av en bestemt bruker
    override suspend fun getAllActivitiesByCreator(creatorId: String): List<editActivity> {
        val categoriesSnapshot = firestore.collection("category").get().await()
        val allActivities = mutableListOf<editActivity>()

        // Gå gjennom hver kategori (dokument)
        for (categoryDocument in categoriesSnapshot.documents) {
            val categoryName = categoryDocument.id

            // Hent alle aktiviteter i denne kategorien som matcher creatorId
            val activitiesSnapshot = firestore.collection("category").document(categoryName)
                .collection("activities")
                .whereEqualTo("creatorId", creatorId)
                .get().await()

            // Mapper aktivitetene og legger til dem i listen
            activitiesSnapshot.documents.mapNotNullTo(allActivities) { document ->
                val activity = document.toObject(editActivity::class.java)

                val fullLocation = activity?.location ?: "Ukjent"
                val lastWord = fullLocation.substringAfterLast(" ")
                val restOfAddress = fullLocation.substringBeforeLast(" ", "Ukjent")

                activity?.copy(
                    id = document.id,
                    location = lastWord,
                    description = restOfAddress,
                    category = categoryName  // Bruker kategoriens navn
                )
            }
        }

        return allActivities
    }

    // Hent alle kategorier
    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("category").get().await()
        val categories = snapshot.documents.map { document ->
            document.id
        }
        return categories.sortedByDescending { it == "Forslag" }
    }

    override suspend fun updateActivity(category: String, newCategory: String, activityId: String, updatedActivity: createActivity) {
        // Check if the category has changed
        if (category != newCategory) {
            // Delete the activity from the old category
            firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)
                .delete()
                .await()

            // Use the createActivity function to add the updated activity to the new category
            createActivity(newCategory, updatedActivity)
        } else {
            // If the category hasn't changed, just update the activity in the current category
            firestore.collection("category")
                .document(newCategory)
                .collection("activities")
                .document(activityId)
                .set(updatedActivity)
                .await()
        }
    }

    // Legg til denne funksjonen i ActivityService
    override suspend fun getAllActivities(): List<Activity> {
        val activityList = mutableListOf<Activity>()
        val categoriesSnapshot = firestore.collection("category").get().await()

        // Iterer over alle kategoriene
        for (categoryDoc in categoriesSnapshot.documents) {
            val activitiesSnapshot = firestore.collection("category")
                .document(categoryDoc.id)
                .collection("activities")
                .get()
                .await()

            // Legg til aktivitetene fra denne kategorien til listen
            val activities = activitiesSnapshot.toObjects(Activity::class.java)
            activityList.addAll(activities)
        }

        return activityList
    }

    // Hent alle kategorier og deres aktiviteter
    override suspend fun getActivitiesGroupedByCategory(): Map<String, List<Activity>> {
        val categoriesSnapshot = firestore.collection("category").get().await()
        val activitiesByCategory = mutableMapOf<String, List<Activity>>()

        // Iterer over hver kategori, og hent aktiviteter
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




}
