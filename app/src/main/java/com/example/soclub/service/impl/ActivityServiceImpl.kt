package com.example.soclub.service.impl

import android.app.Application
import android.location.Address
import android.location.Geocoder
import com.example.soclub.models.Activity
import com.example.soclub.models.CreateActivity
import com.example.soclub.models.EditActivity
import com.example.soclub.service.ActivityService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration


import android.location.Location
import android.os.Build
import android.util.Log
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException

import java.util.Locale
import kotlin.coroutines.resume


class ActivityServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val application: Application
) : ActivityService {

    override fun listenForActivities(onUpdate: (List<Activity>) -> Unit): ListenerRegistration {
        return firestore.collectionGroup("activities")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val activities = snapshot.documents.mapNotNull { doc ->
                    val category = doc.reference.parent.parent?.id
                    doc.toObject(Activity::class.java)?.copy(id = doc.id, category = category)
                }
                onUpdate(activities)
            }
    }

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
        val lastUpdated = documentSnapshot.getTimestamp("lastUpdated") ?: Timestamp.now()

        return activity?.copy(
            location = lastWord,
            restOfAddress = restOfAddress,
            lastUpdated = lastUpdated
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


    override fun listenForNearestActivities(
        userLocation: Location,
        maxDistance: Float,
        onUpdate: (List<Activity>) -> Unit
    ): ListenerRegistration {
        return firestore.collectionGroup("activities")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                // Use a coroutine scope to allow suspending function calls
                CoroutineScope(Dispatchers.IO).launch {
                    val activities = snapshot.documents.mapNotNull { doc ->
                        val activity = doc.toObject(Activity::class.java)
                        if (activity != null) {
                            // Call the suspend function within the coroutine
                            val location = getLocationFromAddress(activity.location)
                            if (location != null) {
                                val distance = userLocation.distanceTo(location)
                                if (distance <= maxDistance) {
                                    activity.copy(id = doc.id)
                                } else null
                            } else null
                        } else null
                    }
                    onUpdate(activities)
                }
            }
    }

    // Helper function to get location from address
    private suspend fun getLocationFromAddress(address: String?): Location? {
        if (address.isNullOrEmpty()) return null
        val geocoder = Geocoder(application.applicationContext, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For API 33 and above, use the GeocodeListener
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val firstAddress = addresses.firstOrNull()
                        val location = firstAddress?.let {
                            Location("").apply {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                        }
                        cont.resume(location)
                    }

                    override fun onError(errorMessage: String?) {
                        cont.resume(null)
                    }
                })
            }
        } else {
            // For lower API levels, use deprecated getFromLocationName method
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(address, 1)
                    addresses?.firstOrNull()?.let {
                        Location("").apply {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    }
                } catch (e: IOException) {
                    Log.e("ActivityServiceImpl", "Error getting location: ${e.message}")
                    null
                }
            }
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
        val batch = firestore.batch()

        // Referanse til aktivitetens dokument i /category/<category>/activities/<activityId>
        val activityDocRef = firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
        batch.delete(activityDocRef)

        // Hent og slett alle notifications med denne activityId
        val notificationsSnapshot = firestore.collection("notifications")
            .whereEqualTo("activityId", activityId)
            .get()
            .await()
        notificationsSnapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        // Hent og slett alle registrations med denne activityId
        val registrationsSnapshot = firestore.collection("registrations")
            .whereEqualTo("activityId", activityId)
            .get()
            .await()
        registrationsSnapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        // Commit batch
        batch.commit().await()
    }

    override suspend fun getRegisteredUsersForActivity(activityId: String): List<String> {
        return try {
            val registrationsSnapshot = firestore.collection("registrations")
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            registrationsSnapshot.documents.mapNotNull { it.getString("userId") }
        } catch (e: Exception) {
            Log.e("ActivityService", "Error getting registered users: ${e.message}")
            emptyList()
        }
    }

}