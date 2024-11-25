package com.example.soclub.service.impl

import android.content.Context
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
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
                    if (error != null) {
                        throw Exception(context.getString(R.string.error_fetching_active_activities,
                            error.message), error)
                    }

                    if (snapshot == null || snapshot.isEmpty) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }


                    CoroutineScope(Dispatchers.IO).launch {
                        processActivities(snapshot, onUpdate)
                    }
                }
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_get_active_activities, e.message), e)
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
                    if (error != null) {
                        throw Exception(
                            context.getString(
                                R.string.error_fetching_not_active_activities,
                                error.message), error)
                    }

                    if (snapshot == null || snapshot.isEmpty) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }


                    CoroutineScope(Dispatchers.IO).launch {
                        processActivities(snapshot, onUpdate)
                    }
                }
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_get_not_active_activities, e.message), e)
        }
    }

    private suspend fun processActivities(
        snapshot: QuerySnapshot,
        onUpdate: (List<Activity>) -> Unit
    ) {
        try {
            val activityList = mutableListOf<Activity>()


            val activityCategoryPairs = snapshot.documents.mapNotNull { document ->
                val activityId = document.getString("activityId")
                val category = document.getString("category")
                if (activityId != null && category != null) {
                    activityId to category
                } else {
                    null
                }
            }


            for ((activityId, category) in activityCategoryPairs) {
                val activitySnapshot = firestore.collection("category")
                    .document(category)
                    .collection("activities")
                    .document(activityId)
                    .get()
                    .await()

                if (activitySnapshot.exists()) {
                    val activity = activitySnapshot.toObject(Activity::class.java)?.copy(
                        category = category,
                        id = activityId
                    )
                    if (activity != null) {
                        activityList.add(activity)
                    }
                }
            }
            onUpdate(activityList)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_process_activities, e.message), e)
        }
    }
}
