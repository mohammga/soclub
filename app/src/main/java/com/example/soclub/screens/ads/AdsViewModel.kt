package com.example.soclub.screens.ads

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.R
import com.example.soclub.models.EditActivity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the Ads screen.
 *
 * - Fetches activities created by the current user.
 * - Handles loading state and error messages.
 *
 * @param context Application context for accessing resources.
 * @param activityService Service for fetching activity data.
 * @param accountService Service for accessing account-related information.
 */
@HiltViewModel
class AdsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityService: ActivityService,
    private val accountService: AccountService
) : ViewModel() {

    /**
     * Represents the loading state of the Ads screen.
     * True when data is being fetched; false otherwise.
     */
    private val _isLoading = MutableStateFlow(true)

    /**
     * Holds any error messages encountered during data fetching.
     * Null if no errors occurred.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * Holds the list of activities created by the current user.
     * Initially an empty list.
     */
    private val _activities = MutableStateFlow<List<EditActivity>>(emptyList())


    /**
     * Exposes the loading state as an immutable flow.
     * Observed by the UI to display loading indicators.
     */
    val errorMessage: StateFlow<String?> = _errorMessage


    /**
     * Exposes the error message as an immutable flow.
     * Observed by the UI to display error messages.
     */
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Exposes the list of activities as an immutable flow.
     * Observed by the UI to display the activities created by the user.
     */
    val activities: StateFlow<List<EditActivity>> = _activities


    /**
     * Fetches the list of activities created by the current user.
     *
     * - Retrieves the creator ID from the account service.
     * - Fetches activities from the activity service.
     * - Updates the `_activities` state with the fetched activities.
     * - Handles any errors that occur during the fetching process.
     */

    //Måtte bruke AI for å fikse på det fordi funksjonenen ikke fungerte som den sakl
    fun fetchActivitiesByCreator() {
        val creatorId = accountService.currentUserId
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val fetchedActivities = activityService.getAllActivitiesByCreator(creatorId).map {
                    EditActivity(
                        creatorId = it.id,
                        imageUrl = it.imageUrl,
                        title = it.title,
                        description = it.description,
                        ageGroup = it.ageGroup,
                        maxParticipants = it.maxParticipants,
                        location = it.location,
                        date = it.date,
                        startTime = it.startTime,
                        category = it.category
                    )
                }

                _activities.value = fetchedActivities
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = context.getString(R.string.error_message)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
