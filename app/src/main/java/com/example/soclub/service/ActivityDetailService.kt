package com.example.soclub.service

import com.example.soclub.models.Activity
import com.google.firebase.firestore.ListenerRegistration

/**
 * Interface defining detailed activity-related operations for the SoClub application.
 *
 * This interface abstracts the functionalities for managing detailed aspects of user activities,
 * such as registration status, participant counts, and real-time updates.
 * It allows different implementations (e.g., Firebase Firestore, Room Database, custom backend) to be used interchangeably.
 */
interface ActivityDetailService {

    /**
     * Retrieves an activity by its unique identifier within a specified category.
     *
     * @param category The category under which the activity is classified.
     * @param activityId The unique identifier of the activity to retrieve.
     *
     * @return An [Activity] object if found, or `null` if no matching activity exists.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getActivityById(category: String, activityId: String): Activity?

    /**
     * Checks whether a specific user is registered for a particular activity.
     *
     * @param userId The unique identifier of the user.
     * @param activityId The unique identifier of the activity.
     *
     * @return `true` if the user is registered for the activity, `false` otherwise.
     *
     * @throws [Exception] if the check process fails.
     */
    suspend fun isUserRegisteredForActivity(userId: String, activityId: String): Boolean

    /**
     * Updates the registration status of a user for a specific activity.
     *
     * @param userId The unique identifier of the user.
     * @param activityId The unique identifier of the activity.
     * @param status The new registration status (e.g., "registered", "cancelled").
     *
     * @return `true` if the update is successful, `false` otherwise.
     *
     * @throws [Exception] if the update process fails.
     */
// Update the method signature to include 'category'
// Update the method signature to include 'category'
    suspend fun updateRegistrationStatus(userId: String, activityId: String, category: String, status: String): Boolean

    /**
     * Retrieves the count of registered participants for a specific activity.
     *
     * @param activityId The unique identifier of the activity.
     *
     * @return An [Int] representing the number of registered participants.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getRegisteredParticipantsCount(activityId: String): Int

    /**
     * Listens for real-time updates to the registration count of a specific activity.
     *
     * This function sets up a listener that triggers the [onUpdate] callback whenever the
     * number of registered participants changes.
     *
     * @param activityId The unique identifier of the activity to monitor.
     * @param onUpdate A callback function invoked with the updated count of registered participants.
     *
     * @return A [ListenerRegistration] object that can be used to remove the listener when it's no longer needed.
     */
    fun listenToRegistrationUpdates(activityId: String, onUpdate: (Int) -> Unit): ListenerRegistration

    /**
     * Listens for real-time updates to a specific activity's details.
     *
     * This function sets up a listener that triggers the [onUpdate] callback whenever
     * there are changes to the activity's information.
     *
     * @param category The category under which the activity is classified.
     * @param activityId The unique identifier of the activity to monitor.
     * @param onUpdate A callback function invoked with the updated [Activity] object.
     *
     * @return A [ListenerRegistration] object that can be used to remove the listener when it's no longer needed.
     */
    fun listenForActivityUpdates(category: String, activityId: String, onUpdate: (Activity) -> Unit): ListenerRegistration
}
