package com.example.soclub.service.impl

import com.example.soclub.models.Activity
import com.example.soclub.models.createActivity
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
                description = restOfAddress, // Bruker beskrivelsefeltet midlertidig for resten av adressen,



            )
        }
    }

    override suspend fun getAllActivitiesByCreator(creatorId: String): List<Activity> {
        // Henter alle kategorier først
        val categoriesSnapshot = firestore.collection("category").get().await()

        // Oppretter en liste for å samle aktiviteter
        val allActivities = mutableListOf<Activity>()

        // Går gjennom hver kategori og henter aktiviteter for den kategorien
        for (categoryDocument in categoriesSnapshot.documents) {
            val categoryId = categoryDocument.id

            // Henter aktiviteter i den aktuelle kategorien som matcher creatorId
            val activitiesSnapshot = firestore.collection("category").document(categoryId)
                .collection("activities")
                .whereEqualTo("creatorId", creatorId)
                .get().await()

            // Legger til aktiviteter i den samlede listen, modifiserer location og description
            activitiesSnapshot.documents.mapNotNullTo(allActivities) { document ->
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

        return allActivities
    }




    override suspend fun getCategories(): List<String> {
        val snapshot = firestore.collection("category").get().await()

        // Hent alle kategorier (dokument-ID-er)
        val categories = snapshot.documents.map { document ->
            document.id  // Returnerer dokument-ID-ene som tilsvarer kategorinavnene
        }

        // Sorter slik at "Forslag" kommer først
        return categories.sortedByDescending { it == "Forslag" }
    }















}
