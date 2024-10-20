package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.service.EntriesService
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration



class EntriesServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EntriesService {


    private var activeListenerRegistration: ListenerRegistration? = null
    private var notActiveListenerRegistration: ListenerRegistration? = null

    override suspend fun getActiveActivitiesForUser(
        userId: String,
        onUpdate: (List<Activity>) -> Unit
    ) {

        activeListenerRegistration?.remove()
       activeListenerRegistration = firestore.collection("registrations")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "aktiv")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val activityList = mutableListOf<Activity>()

                for (document in snapshot.documents) {
                    val activityId = document.getString("activityId") ?: continue

                    // Iterer gjennom kategorier for å finne riktig aktivitet
                    firestore.collection("category").get().addOnSuccessListener { categories ->
                        for (categoryDoc in categories.documents) {
                            val category = categoryDoc.id

                            // Hent aktiviteten
                            firestore.collection("category")
                                .document(category)
                                .collection("activities")
                                .document(activityId)
                                .get()
                                .addOnSuccessListener { activitySnapshot ->
                                    if (activitySnapshot.exists()) {
                                        val activity = activitySnapshot.toObject(Activity::class.java)
                                        if (activity != null) {
                                            activityList.add(activity)
                                            onUpdate(activityList) // Oppdater UI for hver ny aktivitet
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }



    override suspend fun getNotActiveActivitiesForUser(
        userId: String,
        onUpdate: (List<Activity>) -> Unit
    ) {

        notActiveListenerRegistration?.remove()
        notActiveListenerRegistration = firestore.collection("registrations")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "notAktiv") // Endret status til "notAktiv"
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val activityList = mutableListOf<Activity>()

                for (document in snapshot.documents) {
                    val activityId = document.getString("activityId") ?: continue

                    // Iterer gjennom kategorier for å finne riktig aktivitet
                    firestore.collection("category").get().addOnSuccessListener { categories ->
                        for (categoryDoc in categories.documents) {
                            val category = categoryDoc.id

                            // Hent aktiviteten
                            firestore.collection("category")
                                .document(category)
                                .collection("activities")
                                .document(activityId)
                                .get()
                                .addOnSuccessListener { activitySnapshot ->
                                    if (activitySnapshot.exists()) {
                                        val activity = activitySnapshot.toObject(Activity::class.java)
                                        if (activity != null) {
                                            activityList.add(activity)
                                            onUpdate(activityList) // Oppdater UI for hver ny aktivitet
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }
}









