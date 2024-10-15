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
                location = lastWord,  // Her setter vi siste ord som location
                description = restOfAddress // Bruker beskrivelsefeltet midlertidig for resten av adressen
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

        // Hent alle kategorier (dokument-ID-er)
        val categories = snapshot.documents.map { document ->
            document.id  // Returnerer dokument-ID-ene som tilsvarer kategorinavnene
        }

        // Sorter slik at "Forslag" kommer først
        return categories.sortedByDescending { it == "Forslag" }
    }

    // Oppdater aktivitet uten å overskrive creatorId
    override suspend fun updateActivity(category: String, activityId: String, updatedActivity: createActivity): Boolean {
        return try {
            // Referanse til dokumentet som skal oppdateres
            val documentRef = firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)

            // Hent den eksisterende aktiviteten først for å unngå å overskrive felter som creatorId
            val existingActivitySnapshot = documentRef.get().await()
            val existingActivity = existingActivitySnapshot.toObject(createActivity::class.java)

            if (existingActivity != null) {
                // Lag en kopi av updatedActivity med eksisterende creatorId
                val updatedActivityWithCreatorId = updatedActivity.copy(
                    creatorId = existingActivity.creatorId  // Bevar creatorId
                )

                // Utfør oppdateringen med det oppdaterte aktivitetet
                documentRef.set(updatedActivityWithCreatorId).await()
            }

            true  // Returnerer true hvis oppdateringen er vellykket
        } catch (e: Exception) {
            e.printStackTrace()
            false  // Returnerer false hvis det oppstår en feil
        }
    }
}
