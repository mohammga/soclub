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
import com.example.soclub.models.User
import kotlinx.coroutines.tasks.await
import com.example.soclub.models.UserInfo


@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _publisherUser = MutableStateFlow<UserInfo?>(null)
    val publisherUser: StateFlow<UserInfo?> = _publisherUser

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> = _activities

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    private val _canRegister = MutableStateFlow(true)
    val canRegister: StateFlow<Boolean> = _canRegister

    private val _currentParticipants = MutableStateFlow(0)
    val currentParticipants: StateFlow<Int> = _currentParticipants

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isCreator = MutableStateFlow(false)
    val isCreator: StateFlow<Boolean> = _isCreator

    private var registrationListener: ListenerRegistration? = null

    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity

    private var activityListener: ListenerRegistration? = null


    private val _requestAlarmPermission = MutableLiveData<Boolean>()
    val requestAlarmPermission: LiveData<Boolean> = _requestAlarmPermission


    private val _isProcessingRegistration = MutableStateFlow(false)
    val isProcessingRegistration: StateFlow<Boolean> = _isProcessingRegistration


    private fun checkAndRequestExactAlarmPermission() {
        _requestAlarmPermission.value = true
    }

    fun resetAlarmPermissionRequest() {
        _requestAlarmPermission.value = false
    }


    override fun onCleared() {
        super.onCleared()
        registrationListener?.remove()
        activityListener?.remove()
    }

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

                // Sett opp sanntidslytting på aktivitetens data
                activityListener?.remove()  // Fjern eventuell tidligere lytter for å unngå duplikater
                activityListener = activityDetailService.listenForActivityUpdates(category, activityId) { updatedActivity ->
                    _activity.value = updatedActivity
                }

                // Sett opp sanntidslytting på antall deltakere
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


    private fun loadRegisteredParticipants(activityId: String) {
        viewModelScope.launch {
            val count = activityDetailService.getRegisteredParticipantsCount(activityId)
            _currentParticipants.value = count
        }
    }


    fun updateRegistrationForActivity(activityId: String, isRegistering: Boolean) {
        viewModelScope.launch {
            _isProcessingRegistration.value = true // Start registreringsprosessen

            val userId = accountService.currentUserId
            val status = if (isRegistering) "aktiv" else "notAktiv"

            val success = activityDetailService.updateRegistrationStatus(userId, activityId, status)
            if (success) {
                _isRegistered.value = isRegistering

                val currentActivity = _activity.value
                if (currentActivity != null) {
                    loadRegisteredParticipants(activityId)

                    val startTimeMillis = getActivityStartTimeInMillis(currentActivity)
                    if (isRegistering && startTimeMillis != null) {
                        // Schedule notifications...
                        scheduleNotificationForActivity(
                            activityTitle = currentActivity.title,
                            activityId = activityId,
                            startTimeMillis = startTimeMillis,
                            userId = userId
                        )
                        // Immediate registration notification
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
                        // Cancel notifications...
                        cancelNotificationForActivity(context, activityId)
                        // Immediate cancellation notification
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

            _isProcessingRegistration.value = false // Fullfør registreringsprosessen
        }
    }


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

    private fun scheduleNotificationForActivity(
        activityTitle: String,
        activityId: String,
        startTimeMillis: Long,
        userId: String
    ) {
        val currentTimeMillis = System.currentTimeMillis()

        checkAndRequestExactAlarmPermission()

        // Calculate times for 24 hours, 12 hours, 1 hour, and 2 minutes before the activity
//        val twoMinutesBefore = startTimeMillis - (2 * 60 * 1000)
        val oneHourBefore = startTimeMillis - (60 * 60 * 1000)
        val twelveHoursBefore = startTimeMillis - (12 * 60 * 60 * 1000)
        val twentyFourHoursBefore = startTimeMillis - (24 * 60 * 60 * 1000)

        // Schedule each reminder with a custom message
        if (twentyFourHoursBefore > currentTimeMillis) {
            scheduleReminder(
                context = context,
                reminderTime = twentyFourHoursBefore,
                activityTitle = activityTitle,
                activityId = "${activityId}_24hr",
                userId = userId,
                saveToDatabase = false  // Don't save to Firestore immediately
            )
        }

        if (twelveHoursBefore > currentTimeMillis) {
            scheduleReminder(
                context = context,
                reminderTime = twelveHoursBefore,
                activityTitle = activityTitle,
                activityId = "${activityId}_12hr",
                userId = userId,
                saveToDatabase = false  // Don't save to Firestore immediately
            )
        }

        if (oneHourBefore > currentTimeMillis) {
            scheduleReminder(
                context = context,
                reminderTime = oneHourBefore,
                activityTitle = activityTitle,
                activityId = "${activityId}_1hr",
                userId = userId,
                saveToDatabase = false  // Don't save to Firestore immediately
            )
        }

//        if (twoMinutesBefore > currentTimeMillis) {
//            scheduleReminder(
//                context = context,
//                reminderTime = twoMinutesBefore,
//                activityTitle = activityTitle,
//                activityId = "${activityId}_2min",
//                userId = userId,
//                saveToDatabase = false  // Set to false if you don’t want to save each notification to Firestore immediately
//            )
//        }
    }


}