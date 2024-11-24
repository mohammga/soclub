package com.example.soclub.service.impl
import android.content.Context
import com.example.soclub.R
import com.example.soclub.models.Activity
import com.example.soclub.service.ActivityDetailService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration


/**
 * Implementation of the ActivityDetailService interface to handle operations
 * related to activities, such as fetching activity details, user registrations, and updates.
 *
 * @param firestore Firebase Firestore instance for database operations.
 * @param context Android Context for accessing resources.
 */
class ActivityDetailServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val context: Context
) : ActivityDetailService {

    /**
     * Fetches an activity by its category and ID.
     *
     * @param category The category of the activity.
     * @param activityId The ID of the activity.
     * @return An Activity object with the fetched details.
     * @throws Exception if the activity is not found or fetching fails.
     */
    override suspend fun getActivityById(category: String, activityId: String): Activity {
        try {
            val documentSnapshot = firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)
                .get()
                .await()

            val activity = documentSnapshot.toObject(Activity::class.java)
                ?: throw Exception(context.getString(R.string.error_activity_not_found))

            val creatorId = documentSnapshot.getString("creatorId") ?: ""
            val fullLocation = activity.location
            val restOfAddress = fullLocation.substringBeforeLast(" ", context.getString(R.string.unknown))
            val date = documentSnapshot.getTimestamp("date")
            val startTime = documentSnapshot.getString("startTime") ?: context.getString(R.string.unknown_time)

            return activity.copy(
                restOfAddress = restOfAddress,
                date = date,
                startTime = startTime,
                creatorId = creatorId
            )
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_fetch_activity, e.message), e)
        }
    }

    /**
     * Checks if a user is registered for a specific activity.
     *
     * @param userId The ID of the user.
     * @param activityId The ID of the activity.
     * @return True if the user is registered and active, otherwise false.
     * @throws Exception if the query fails.
     */
    override suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean {
        try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            if (!registrationRef.isEmpty) {
                val status = registrationRef.documents.first().getString("status")
                return status == context.getString(R.string.status_active)
            }
            return false
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_user_registration, e.message), e)
        }
    }

    /**
     * Updates the registration status of a user for an activity.
     *
     * @param userId The ID of the user.
     * @param activityId The ID of the activity.
     * @param status The new registration status.
     * @return True if the operation is successful.
     * @throws Exception if the update fails.
     */
    override suspend fun updateRegistrationStatus(userId: String, activityId: String, status: String): Boolean {
        try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            if (!registrationRef.isEmpty) {
                for (document in registrationRef.documents) {
                    firestore.collection("registrations")
                        .document(document.id)
                        .update(mapOf("status" to status))
                        .await()
                }
            } else {
                val newRegistration = hashMapOf(
                    "userId" to userId,
                    "activityId" to activityId,
                    "status" to status,
                    "timestamp" to Timestamp(Date())
                )
                firestore.collection("registrations").add(newRegistration).await()
            }
            return true
        } catch (e: Exception) {
            val errorMessage = context.getString(R.string.error_update_registration_status, e.message)
            throw Exception(errorMessage, e)
        }
    }

    /**
     * Retrieves the count of registered participants for a specific activity.
     *
     * @param activityId The ID of the activity.
     * @return The count of active participants.
     * @throws Exception if the query fails.
     */
    override suspend fun getRegisteredParticipantsCount(activityId: String): Int {
        try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("activityId", activityId)
                .whereEqualTo("status", "aktiv")
                .get()
                .await()

            return registrationRef.size()
        } catch (e: Exception) {
            val errorMessage = context.getString(R.string.error_get_registered_participants_count, e.message)
            throw Exception(errorMessage, e)
        }
    }

    /**
     * Listens for updates to the registration count of a specific activity.
     *
     * @param activityId The ID of the activity.
     * @param onUpdate A callback function invoked with the updated count.
     * @return A ListenerRegistration object to manage the listener lifecycle.
     */
    override fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("registrations")
            .whereEqualTo("activityId", activityId)
            .whereEqualTo("status", "aktiv")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = context.getString(R.string.error_listening_registration_updates, error.message)
                    throw Exception(errorMessage, error)
                }
                snapshot?.let {
                    val count = it.size()
                    onUpdate(count)
                }
            }
    }

    /**
     * Listens for updates to a specific activity.
     *
     * @param category The category of the activity.
     * @param activityId The ID of the activity.
     * @param onUpdate A callback function invoked with the updated activity details.
     * @return A ListenerRegistration object to manage the listener lifecycle.
     */
    override fun listenForActivityUpdates(category: String, activityId: String, onUpdate: (Activity) -> Unit): ListenerRegistration {
        return firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    val errorMessage = context.getString(
                        R.string.error_listening_activity_updates,
                        error?.message ?: "Ukjent feil"
                    )
                    throw Exception(errorMessage)
                }
                val activity = snapshot.toObject(Activity::class.java)
                    ?: throw Exception(context.getString(R.string.error_parsing_activity_data))
                onUpdate(activity)
            }
    }
}

