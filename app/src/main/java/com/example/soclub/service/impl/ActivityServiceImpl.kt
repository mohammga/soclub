package com.example.soclub.service.impl

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.models.CreateActivity
import com.example.soclub.models.EditActivity
import com.example.soclub.service.ActivityService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject


class ActivityServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val application: Context
) : ActivityService {

    override fun listenForActivities(onUpdate: (List<Activity>) -> Unit): ListenerRegistration {
        return firestore.collectionGroup("activities")
            .addSnapshotListener { snapshot, error ->
                if (error != null)
                    throw Exception(
                        application.getString(R.string.error_listen_activities_failed, error.message),
                        error
                    )

                val activities = snapshot?.documents?.mapNotNull { doc ->
                    val category = doc.reference.parent.parent?.id
                    doc.toObject(Activity::class.java)?.copy(id = doc.id, category = category)
                } ?: emptyList()
                onUpdate(activities)
            }
    }

    override suspend fun createActivity(category: String, activity: CreateActivity) {
        try {
            firestore.collection("category").document(category)
                .collection("activities").add(activity).await()
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_create_activity_failed, e.message), e)
        }
    }

    override suspend fun getActivityById(category: String, activityId: String): Activity {
        try {
            val documentSnapshot = firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)
                .get()
                .await()

            val activity = documentSnapshot.toObject(Activity::class.java)
                ?: throw Exception(application.getString(R.string.error_activity_not_found))


            val fullLocation = activity.location
            val lastWord = fullLocation.substringAfterLast(" ")
            val restOfAddress = fullLocation.substringBeforeLast(" ", "Unknown")
            val lastUpdated = documentSnapshot.getTimestamp("lastUpdated") ?: Timestamp.now()

            return activity.copy(
                location = lastWord,
                restOfAddress = restOfAddress,
                lastUpdated = lastUpdated
            )
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_fetch_activity_by_id_failed, e.message), e)
        }
    }

    override suspend fun getActivities(category: String): List<Activity> {
        try {
            val snapshot = firestore.collection("category").document(category)
                .collection("activities").get().await()

            return snapshot.documents.mapNotNull { document ->
                val activity = document.toObject(Activity::class.java)
                    ?: throw Exception(application.getString(R.string.error_activity_data_parsing_failed))
                val fullLocation = activity.location
                val lastWord = fullLocation.substringAfterLast(" ")
                val restOfAddress = fullLocation.substringBeforeLast(" ", "Unknown")

                activity.copy(
                    id = document.id,
                    location = lastWord,
                    description = restOfAddress
                )
            }
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_get_activities_failed, e.message), e)
        }
    }

    override fun listenForNearestActivities(
        userLocation: Location,
        maxDistance: Float,
        onUpdate: (List<Activity>) -> Unit
    ): ListenerRegistration {
        return firestore.collectionGroup("activities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) throw Exception(
                    application.getString(R.string.error_listening_nearest_activities, error.message), error)


                CoroutineScope(Dispatchers.IO).launch {
                    val activities = snapshot?.documents?.mapNotNull { doc ->
                        val activity = doc.toObject(Activity::class.java)
                        val location = activity?.location?.let { getLocationFromAddress(it) }
                        if (location != null && userLocation.distanceTo(location) <= maxDistance) {
                            activity.copy(id = doc.id)
                        } else null
                    } ?: emptyList()
                    onUpdate(activities)
                }
            }
    }

    @Suppress("DEPRECATION")
    private suspend fun getLocationFromAddress(address: String?): Location? {
        if (address.isNullOrEmpty()) throw Exception("Invalid address")
        val geocoder = Geocoder(application.applicationContext, Locale.getDefault())

        return try {
            withContext(Dispatchers.IO) {
                val addresses = geocoder.getFromLocationName(address, 1)
                addresses?.firstOrNull()?.let {
                    Location("").apply {
                        latitude = it.latitude
                        longitude = it.longitude
                    }
                } ?: throw Exception(application.getString(R.string.error_resolve_location_failed))

            }
        } catch (e: IOException) {
            throw Exception(
                application.getString(R.string.error_geocoder_failed, e.message), e)

        }
    }

    override suspend fun getAllActivitiesByCreator(creatorId: String): List<EditActivity> {
        try {
            val categoriesSnapshot = firestore.collection("category").get().await()
            val allActivities = mutableListOf<EditActivity>()

            for (categoryDocument in categoriesSnapshot.documents) {
                val categoryName = categoryDocument.id
                val activitiesSnapshot = firestore.collection("category").document(categoryName)
                    .collection("activities")
                    .whereEqualTo("creatorId", creatorId)
                    .get()
                    .await()

                activitiesSnapshot.documents.mapNotNullTo(allActivities) { document ->
                    val activity = document.toObject(EditActivity::class.java)
                        ?: throw Exception("Activity data parsing failed")
                    val fullLocation = activity.location
                    val lastWord = fullLocation.substringAfterLast(" ")
                    val restOfAddress = fullLocation.substringBeforeLast(" ", "Unknown")

                    activity.copy(
                        id = document.id,
                        location = lastWord,
                        description = restOfAddress,
                        category = categoryName
                    )
                }
            }

            return allActivities
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_fetch_activities_by_creator, e.message), e)
        }
    }

    override suspend fun deleteActivity(category: String, activityId: String) {
        try {
            val batch = firestore.batch()

            val activityDocRef = firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)
            batch.delete(activityDocRef)

            val notificationsSnapshot = firestore.collection("notifications")
                .whereEqualTo("activityId", activityId)
                .get()
                .await()
            notificationsSnapshot.documents.forEach { doc -> batch.delete(doc.reference) }

            val registrationsSnapshot = firestore.collection("registrations")
                .whereEqualTo("activityId", activityId)
                .get()
                .await()
            registrationsSnapshot.documents.forEach { doc -> batch.delete(doc.reference) }

            batch.commit().await()
        } catch (e: Exception) {
                throw Exception(
                    application.getString(R.string.error_delete_activity_failed, e.message), e)
            }

        }


    override suspend fun getCategories(): List<String> {
        try {
            val snapshot = firestore.collection("category").get().await()
            return snapshot.documents.map { document -> document.id }
                .sortedByDescending { it }
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_fetch_categories_failed, e.message), e)
        }
    }

    override suspend fun updateActivity(
        category: String,
        newCategory: String,
        activityId: String,
        updatedActivity: CreateActivity
    ) {
        try {
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
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_update_activity_failed, e.message), e)
        }
    }

    override suspend fun getAllActivities(): List<Activity> {
        try {
            val activityList = mutableListOf<Activity>()
            val categoriesSnapshot = firestore.collection("category").get().await()

            for (categoryDoc in categoriesSnapshot.documents) {
                val categoryName = categoryDoc.id

                try {
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
                } catch (e: Exception) {
                    throw Exception(
                            application.getString(R.string.error_fetch_activities_for_category, categoryName, e.message), e)
                }
            }

            return activityList
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_fetch_all_activities_failed, e.message), e)
        }
    }

    override suspend fun getActivitiesGroupedByCategory(): Map<String, List<Activity>> {
        try {
            val activitiesByCategory = mutableMapOf<String, List<Activity>>()
            val categoriesSnapshot = firestore.collection("category").get().await()

            for (categoryDoc in categoriesSnapshot.documents) {
                val categoryName = categoryDoc.id

                try {
                    val activitiesSnapshot = firestore.collection("category")
                        .document(categoryName)
                        .collection("activities")
                        .get()
                        .await()

                    val activities = activitiesSnapshot.toObjects(Activity::class.java)
                    activitiesByCategory[categoryName] = activities
                } catch (e: Exception) {
                    throw Exception(
                            application.getString(R.string.error_fetch_activities_for_category, categoryName, e.message),e)
                }
            }

            return activitiesByCategory
        } catch (e: Exception) {
                throw Exception(
                    application.getString(R.string.error_fetch_grouped_activities_failed, e.message), e)

        }
    }

    override suspend fun getRegisteredUsersForActivity(activityId: String): List<String> {
        try {
            val registrationsSnapshot = firestore.collection("registrations")
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            return registrationsSnapshot.documents.mapNotNull { it.getString("userId") }
        } catch (e: Exception) {
            throw Exception(
                application.getString(R.string.error_fetch_registered_users_failed, e.message), e)
        }
    }
}
