package com.example.soclub.service.impl

import android.content.Context
import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EntriesServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val context: Context
) : EntriesService {

    private var activeListenerRegistration: ListenerRegistration? = null
    private var notActiveListenerRegistration: ListenerRegistration? = null

    override suspend fun getActiveActivitiesForUser(
        userId: String,
        onUpdate: (List<Activity>) -> Unit
    ) {
        try {
            activeListenerRegistration?.remove()

            activeListenerRegistration = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "aktiv")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) throw Exception("Error fetching active activities: ${error.message}", error)

                    if (snapshot == null || snapshot.isEmpty) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot.documents.mapNotNull { document ->
                         document.getString("activityId")
                            ?: throw Exception("Missing activityId in registration document")
                        firestore.collection("category").get()
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        processActivities(snapshot, tasks, onUpdate)
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to get active activities for user: ${e.message}", e)
        }
    }

    override suspend fun getNotActiveActivitiesForUser(
        userId: String,
        onUpdate: (List<Activity>) -> Unit
    ) {
        try {
            notActiveListenerRegistration?.remove()

            notActiveListenerRegistration = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "notAktiv")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) throw Exception("Error fetching not active activities: ${error.message}", error)

                    if (snapshot == null || snapshot.isEmpty) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot.documents.mapNotNull { document ->
                    document.getString("activityId")
                            ?: throw Exception("Missing activityId in registration document")
                        firestore.collection("category").get()
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        processActivities(snapshot, tasks, onUpdate)
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to get not active activities for user: ${e.message}", e)
        }
    }

    private suspend fun processActivities(
        snapshot: QuerySnapshot,
        tasks: List<Task<QuerySnapshot>>,
        onUpdate: (List<Activity>) -> Unit
    ) {
        try {
            val activityList = mutableListOf<Activity>()

            tasks.forEach { task ->
                val categories = task.await().documents
                for (categoryDoc in categories) {
                    val category = categoryDoc.id
                    val activityIds = snapshot.documents.mapNotNull { it.getString("activityId") }

                    activityIds.forEach { id ->
                        val activitySnapshot = firestore.collection("category")
                            .document(category)
                            .collection("activities")
                            .document(id)
                            .get()
                            .await()

                        if (activitySnapshot.exists()) {
                            val activity = activitySnapshot.toObject(Activity::class.java)?.copy(
                                category = category,
                                id = id
                            )
                            if (activity != null) {
                                activityList.add(activity)
                            }
                        }
                    }
                }
            }
            onUpdate(activityList)
        } catch (e: Exception) {
            throw Exception("Failed to process activities: ${e.message}", e)
        }
    }
}
