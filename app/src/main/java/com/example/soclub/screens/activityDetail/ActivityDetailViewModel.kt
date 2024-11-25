package com.example.soclub.screens.activityDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.soclub.R
import com.example.soclub.utils.cancelNotificationForActivity
import com.example.soclub.utils.scheduleReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.soclub.models.UserInfo

/**
 * ViewModel for handling the logic of the Activity Detail Screen.
 * Manages the state of the activity, including registration status, publisher information,
 * and notifications for upcoming activities.
 *
 * Dependencies:
 * - AccountService: For user account-related operations.
 * - ActivityDetailService: For activity-related operations.
 * - FirebaseFirestore: For accessing Firestore data.
 *
 * @param context Application context for accessing resources.
 * @param accountService Service for managing account-related operations.
 * @param activityDetailService Service for managing activity details.
 * @param firestore Firebase Firestore instance for database access.
 */

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    /**
     * Private mutable state variables and listeners used to manage activity details
     * and user interactions.
     */
    private val _publisherUser = MutableStateFlow<UserInfo?>(null)
    private val _activities = MutableLiveData<List<Activity>>()
    private val _isRegistered = MutableStateFlow(false)
    private val _canRegister = MutableStateFlow(true)
    private val _currentParticipants = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isCreator = MutableStateFlow(false)
    private val _activity = MutableStateFlow<Activity?>(null)
    private val _requestAlarmPermission = MutableLiveData<Boolean>()
    private val _isProcessingRegistration = MutableStateFlow(false)
    private var registrationListener: ListenerRegistration? = null
    private var activityListener: ListenerRegistration? = null

    /**
     * Public state variables exposing data to the UI.
     * These variables are read-only to ensure immutability outside the ViewModel.
     */
    val publisherUser: StateFlow<UserInfo?> = _publisherUser
    val activities: LiveData<List<Activity>> = _activities
    val isRegistered: StateFlow<Boolean> = _isRegistered
    val canRegister: StateFlow<Boolean> = _canRegister
    val currentParticipants: StateFlow<Int> = _currentParticipants
    val isLoading: StateFlow<Boolean> = _isLoading
    val errorMessage: StateFlow<String?> = _errorMessage
    val isCreator: StateFlow<Boolean> = _isCreator
    val requestAlarmPermission: LiveData<Boolean> = _requestAlarmPermission
    val isProcessingRegistration: StateFlow<Boolean> = _isProcessingRegistration
    val activity: StateFlow<Activity?> = _activity

    /**
     * Called when the ViewModel is cleared from memory.
     * Removes any active listeners for activity and registration updates.
     */
    override fun onCleared() {
        super.onCleared()
        registrationListener?.remove()
        activityListener?.remove()
    }

    /**
     * Loads the details of an activity along with its registration status and updates the state.
     *
     * @param category The category of the activity.
     * @param activityId The unique identifier of the activity.
     */
    fun loadActivityWithStatus(category: String, activityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val loadedActivity = activityDetailService.getActivityById(category, activityId)
                _activity.value = loadedActivity

                val userId = accountService.currentUserId
                val registered = activityDetailService.isUserRegisteredForActivity(userId, activityId)
                _isRegistered.value = registered

                val userInfo = accountService.getUserInfo()
                if (loadedActivity != null) {
                    _isCreator.value = loadedActivity.creatorId == userId
                    _canRegister.value = userInfo.age >= loadedActivity.ageGroup && !_isCreator.value
                    fetchPublisherInfo(loadedActivity.creatorId)
                }

                loadRegisteredParticipants(activityId)

                activityListener?.remove()
                activityListener = activityDetailService.listenForActivityUpdates(category, activityId) { updatedActivity ->
                    _activity.value = updatedActivity
                }

                registrationListener = activityDetailService.listenToRegistrationUpdates(activityId) { count ->
                    _currentParticipants.value = count
                }

            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.error_message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetches the information of the user who created the activity.
     *
     * @param creatorId The unique identifier of the creator.
     */
    private fun fetchPublisherInfo(creatorId: String?) {
        if (creatorId.isNullOrEmpty() || creatorId == "admin") {
            _publisherUser.value = null
        } else {
            viewModelScope.launch {
                try {
                    val userDoc = firestore.collection("users").document(creatorId).get().await()
                    val userInfo = userDoc.toObject(UserInfo::class.java)
                    _publisherUser.value = userInfo
                } catch (e: Exception) {
                    Log.e("ActivityDetailViewModel", "Error fetching publisher info", e)
                    _publisherUser.value = null
                }
            }
        }
    }

    /**
     * Loads the current number of participants registered for the activity.
     *
     * @param activityId The unique identifier of the activity.
     */
    fun loadRegisteredParticipants(activityId: String) {
        viewModelScope.launch {
            val count = activityDetailService.getRegisteredParticipantsCount(activityId)
            _currentParticipants.value = count
        }
    }

    /**
     * Updates the registration status of the user for a specific activity.
     *
     * @param activityId The unique identifier of the activity.
     * @param isRegistering True if the user is registering, false if unregistering.
     */
    fun updateRegistrationForActivity(activityId: String, category: String, isRegistering: Boolean) {
        viewModelScope.launch {
            _isProcessingRegistration.value = true

            val userId = accountService.currentUserId
            val status = if (isRegistering) "aktiv" else "notAktiv"

            val success = activityDetailService.updateRegistrationStatus(userId, activityId, category, status)
            if (success) {
                _isRegistered.value = isRegistering

                val currentActivity = _activity.value
                if (currentActivity != null) {
                    loadRegisteredParticipants(activityId)

                    val startTimeMillis = getActivityStartTimeInMillis(currentActivity)
                    if (isRegistering && startTimeMillis != null) {
                        scheduleNotificationForActivity(
                            activityTitle = currentActivity.title,
                            activityId = activityId,
                            startTimeMillis = startTimeMillis,
                            userId = userId
                        )
                        scheduleReminder(
                            context = context,
                            reminderTime = System.currentTimeMillis(),
                            activityTitle = currentActivity.title,
                            activityId = activityId,
                            userId = userId,
                            sendNow = true,
                            isCancellation = false,
                            isRegistration = true
                        )
                    } else if (!isRegistering) {
                        cancelNotificationForActivity(context, activityId)
                        scheduleReminder(
                            context = context,
                            reminderTime = System.currentTimeMillis(),
                            activityTitle = currentActivity.title,
                            activityId = activityId,
                            userId = userId,
                            sendNow = true,
                            isCancellation = true,
                            isRegistration = false
                        )
                    }
                }
            }

            _isProcessingRegistration.value = false
        }
    }

    /**
     * Requests exact alarm permissions if not already granted.
     */
    private fun checkAndRequestExactAlarmPermission() {
        _requestAlarmPermission.value = true
    }

    /**
     * Resets the alarm permission request flag.
     */
    fun resetAlarmPermissionRequest() {
        _requestAlarmPermission.value = false
    }


    /**
     * Calculates the start time of the activity in milliseconds.
     *
     * @param activity The activity object containing the start time and date.
     * @return The start time in milliseconds, or null if the format is invalid.
     */
    private fun getActivityStartTimeInMillis(activity: Activity): Long? {
        val activityDate = activity.date?.toDate() ?: return null
        val startTime = activity.startTime

        return try {
            val timeParts = startTime.split(":")
            val calendar = Calendar.getInstance().apply {
                time = activityDate
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e("ActivityDetailViewModel", "Invalid start time format: $startTime", e)
            null
        }
    }

    /**
     * Schedules notifications for an activity at different intervals before the start time.
     *
     * @param activityTitle The title of the activity.
     * @param activityId The unique identifier of the activity.
     * @param startTimeMillis The start time of the activity in milliseconds.
     * @param userId The unique identifier of the user.
     */
    private fun scheduleNotificationForActivity(
        activityTitle: String,
        activityId: String,
        startTimeMillis: Long,
        userId: String
    ) {
        val currentTimeMillis = System.currentTimeMillis()

        checkAndRequestExactAlarmPermission()

        val oneHourBefore = startTimeMillis - (60 * 60 * 1000)
        val twelveHoursBefore = startTimeMillis - (12 * 60 * 60 * 1000)
        val twentyFourHoursBefore = startTimeMillis - (24 * 60 * 60 * 1000)

        if (twentyFourHoursBefore > currentTimeMillis) {
            scheduleReminder(
                context = context,
                reminderTime = twentyFourHoursBefore,
                activityTitle = activityTitle,
                activityId = "${activityId}_24hr",
                userId = userId,
                saveToDatabase = false
            )
        }

        if (twelveHoursBefore > currentTimeMillis) {
            scheduleReminder(
                context = context,
                reminderTime = twelveHoursBefore,
                activityTitle = activityTitle,
                activityId = "${activityId}_12hr",
                userId = userId,
                saveToDatabase = false
            )
        }

        if (oneHourBefore > currentTimeMillis) {
            scheduleReminder(
                context = context,
                reminderTime = oneHourBefore,
                activityTitle = activityTitle,
                activityId = "${activityId}_1hr",
                userId = userId,
                saveToDatabase = false
            )
        }
    }

}