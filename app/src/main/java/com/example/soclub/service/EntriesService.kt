package com.example.soclub.service

import com.example.soclub.models.Activity

/**
 * Interface defining entry-related operations for the SoClub application.
 *
 * This interface abstracts the functionalities for managing user activities,
 * allowing different implementations (e.g., Firebase Firestore, Room Database, custom backend) to be used interchangeably.
 */
interface EntriesService {

    /**
     * Retrieves a list of active activities for a specific user.
     *
     * This function fetches all activities that are currently active for the user identified by [userId].
     * Active activities typically refer to ongoing or currently relevant engagements within the application.
     *
     * @param userId The unique identifier of the user whose active activities are to be retrieved.
     * @param onUpdate A callback function invoked with the list of active [Activity] objects upon successful retrieval.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getActiveActivitiesForUser(
        userId: String,
        onUpdate: (List<Activity>) -> Unit
    )

    /**
     * Retrieves a list of inactive activities for a specific user.
     *
     * This function fetches all activities that are currently inactive or have been completed for the user identified by [userId].
     * Inactive activities typically refer to past engagements or those that are no longer active within the application.
     *
     * @param userId The unique identifier of the user whose inactive activities are to be retrieved.
     * @param onUpdate A callback function invoked with the list of inactive [Activity] objects upon successful retrieval.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getNotActiveActivitiesForUser(
        userId: String,
        onUpdate: (List<Activity>) -> Unit
    )
}
