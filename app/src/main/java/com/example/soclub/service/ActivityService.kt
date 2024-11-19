package com.example.soclub.service

import android.location.Location
import com.example.soclub.models.Activity
import com.example.soclub.models.CreateActivity
import com.example.soclub.models.EditActivity
import com.google.firebase.firestore.ListenerRegistration

/**
 * Interface defining activity-related operations for the SoClub application.
 *
 * This interface abstracts the functionalities for managing user activities,
 * allowing different implementations (e.g., Firebase Firestore, Room Database, custom backend) to be used interchangeably.
 */
interface ActivityService {

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
     * Creates a new activity within a specified category.
     *
     * @param category The category under which the activity should be created.
     * @param activity The [CreateActivity] object containing details of the activity to be created.
     *
     * @throws [Exception] if the creation process fails.
     */
    suspend fun createActivity(category: String, activity: CreateActivity)

    /**
     * Retrieves all activities within a specified category.
     *
     * @param category The category for which to fetch activities.
     *
     * @return A [List] of [Activity] objects belonging to the specified category.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getActivities(category: String): List<Activity>

    /**
     * Retrieves all available activity categories.
     *
     * @return A [List] of [String] objects representing the names of all categories.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getCategories(): List<String>

    /**
     * Retrieves all activities created by a specific user.
     *
     * @param creatorId The unique identifier of the user who created the activities.
     *
     * @return A [List] of [EditActivity] objects representing activities created by the user.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getAllActivitiesByCreator(creatorId: String): List<EditActivity>

    /**
     * Updates an existing activity within a specified category.
     *
     * @param category The current category of the activity.
     * @param newCategory The new category to which the activity should be moved (if applicable).
     * @param activityId The unique identifier of the activity to update.
     * @param updatedActivity The [CreateActivity] object containing updated details of the activity.
     *
     * @throws [Exception] if the update process fails.
     */
    suspend fun updateActivity(
        category: String,
        newCategory: String,
        activityId: String,
        updatedActivity: CreateActivity
    )

    /**
     * Retrieves all activities across all categories.
     *
     * @return A [List] of [Activity] objects representing all activities in the application.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getAllActivities(): List<Activity>

    /**
     * Retrieves all activities grouped by their respective categories.
     *
     * @return A [Map] where each key is a category [String] and the corresponding value is a [List] of [Activity] objects within that category.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getActivitiesGroupedByCategory(): Map<String, List<Activity>>

    /**
     * Deletes a specific activity within a specified category.
     *
     * @param category The category under which the activity is classified.
     * @param activityId The unique identifier of the activity to delete.
     *
     * @throws [Exception] if the deletion process fails.
     */
    suspend fun deleteActivity(category: String, activityId: String)

    /**
     * Sets up a listener to receive real-time updates for activities.
     *
     * This function listens for any changes to the activities data source and invokes the [onUpdate] callback
     * with the latest list of [Activity] objects whenever changes occur.
     *
     * @param onUpdate A callback function that is invoked with the updated list of [Activity] objects.
     *
     * @return A [ListenerRegistration] object that can be used to remove the listener when it's no longer needed.
     *
     * @throws [Exception] if setting up the listener fails.
     */
    fun listenForActivities(onUpdate: (List<Activity>) -> Unit): ListenerRegistration

    /**
     * Sets up a listener to receive real-time updates for activities nearest to the user's location.
     *
     * This function listens for activities within a specified distance from the user's current location
     * and invokes the [onUpdate] callback with the list of nearby [Activity] objects whenever relevant changes occur.
     *
     * @param userLocation The [Location] object representing the user's current location.
     * @param maxDistance The maximum distance (in meters) within which to consider activities as nearby.
     * @param onUpdate A callback function that is invoked with the updated list of nearby [Activity] objects.
     *
     * @return A [ListenerRegistration] object that can be used to remove the listener when it's no longer needed.
     *
     * @throws [Exception] if setting up the listener fails.
     */
    fun listenForNearestActivities(
        userLocation: Location,
        maxDistance: Float,
        onUpdate: (List<Activity>) -> Unit
    ): ListenerRegistration

    /**
     * Retrieves a list of user IDs who have registered for a specific activity.
     *
     * @param activityId The unique identifier of the activity for which to fetch registered users.
     *
     * @return A [List] of [String] objects representing the user IDs of registered users.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getRegisteredUsersForActivity(activityId: String): List<String>
}
