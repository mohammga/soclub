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

class ActivityDetailServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,

    private val context: Context

) : ActivityDetailService {

    override suspend fun getActivityById(category: String, activityId: String): Activity {
        try {
            val documentSnapshot = firestore.collection("category")
                .document(category)
                .collection("activities")
                .document(activityId)
                .get()
                .await()


            val activity = documentSnapshot.toObject(Activity::class.java)
                //?: throw Exception("Activity not found")
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
            //throw Exception("Failed to fetch activity: ${e.message}", e)
            //val errorMessage = context.getString(R.string.error_fetch_activity, e.message)
            //throw Exception(errorMessage, e)
            throw Exception(context.getString(R.string.error_fetch_activity, e.message), e)
        }
          }

    override suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean {
        try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("activityId", activityId)
                .get()
                .await()

            if (!registrationRef.isEmpty) {
                val status = registrationRef.documents.first().getString("status")
                return status == context.getString(R.string.status_active)//"aktiv"
            }
            return false
        } catch (e: Exception) {
            //throw Exception("Failed to check user registration: ${e.message}", e)
            throw Exception(
                context.getString(R.string.error_user_registration, e.message), e)
        }
    }

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
            //throw Exception("Failed to update registration status: ${e.message}", e)
            val errorMessage = context.getString(R.string.error_update_registration_status, e.message)
            throw Exception(errorMessage, e)
        }

    }

    override suspend fun getRegisteredParticipantsCount(activityId: String): Int {
        try {
            val registrationRef = firestore.collection("registrations")
                .whereEqualTo("activityId", activityId)
                .whereEqualTo("status", "aktiv")
                .get()
                .await()

            return registrationRef.size()
        } catch (e: Exception) {
            //throw Exception("Failed to get registered participants count: ${e.message}", e)
            val errorMessage = context.getString(R.string.error_get_registered_participants_count, e.message)
            throw Exception(errorMessage, e)
        }
    }

    override fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("registrations")
            .whereEqualTo("activityId", activityId)
            .whereEqualTo("status", "aktiv")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    //throw Exception("Error listening to registration updates: ${error.message}", error)
                    val errorMessage = context.getString(R.string.error_listening_registration_updates, error.message)
                    throw Exception(errorMessage, error)
                }
                snapshot?.let {
                    val count = it.size()
                    onUpdate(count)
                }
            }
    }

    override fun listenForActivityUpdates(category: String, activityId: String, onUpdate: (Activity) -> Unit): ListenerRegistration {
        return firestore.collection("category")
            .document(category)
            .collection("activities")
            .document(activityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    //throw Exception("Error listening for activity updates: ${error?.message ?: "Unknown error"}")
                    val errorMessage = context.getString(
                        R.string.error_listening_activity_updates,
                        error?.message ?: "Ukjent feil"
                    )
                    throw Exception(errorMessage)
                }
                val activity = snapshot.toObject(Activity::class.java)
                    //?: throw Exception("Failed to parse activity data")
                    ?: throw Exception(context.getString(R.string.error_parsing_activity_data))
                onUpdate(activity)

                onUpdate(activity)
            }
    }
}
