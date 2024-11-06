package com.example.soclub.screens.activityDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soclub.models.Activity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.util.Log
import com.example.soclub.utils.cancelNotificationForActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.soclub.utils.scheduleNotificationForActivity
import java.util.Calendar


@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService
) : ViewModel() {

    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity

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

    // **Added StateFlow to indicate if the user is the creator**
    private val _isCreator = MutableStateFlow(false)
    val isCreator: StateFlow<Boolean> = _isCreator

    fun loadActivityWithStatus(category: String, activityId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(1000)
            try {
                val loadedActivity = activityDetailService.getActivityById(category, activityId)
                _activity.value = loadedActivity

                val userId = accountService.currentUserId
                val registered = activityDetailService.isUserRegisteredForActivity(userId, activityId)
                _isRegistered.value = registered

                val userInfo = accountService.getUserInfo()

                if (loadedActivity != null) {
                    // Bruk 'creatorId' i stedet for 'createdBy'
                    _isCreator.value = loadedActivity.creatorId == userId
                    _canRegister.value = userInfo.age >= loadedActivity.ageGroup && !_isCreator.value
                }

                loadRegisteredParticipants(activityId)
                activityDetailService.listenToRegistrationUpdates(activityId) { count ->
                    _currentParticipants.value = count
                }

            } catch (e: Exception) {
                _errorMessage.value = "Det skjedde en feil. Vennligst prøv igjen senere."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Rest of the ViewModel code remains the same
    // ...

    private fun loadRegisteredParticipants(activityId: String) {
        viewModelScope.launch {
            val count = activityDetailService.getRegisteredParticipantsCount(activityId)
            _currentParticipants.value = count
        }
    }

    fun updateRegistrationForActivity(activityId: String, isRegistering: Boolean) {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            val status = if (isRegistering) "aktiv" else "notAktiv"

            val success = activityDetailService.updateRegistrationStatus(userId, activityId, status)
            if (success) {
                _isRegistered.value = isRegistering

                val currentActivity = _activity.value
                if (currentActivity != null) {
                    loadRegisteredParticipants(activityId)

                    // Convert date and time to milliseconds
                    val startTimeMillis = getActivityStartTimeInMillis(currentActivity)

                    if (isRegistering && startTimeMillis != null) {
                        scheduleNotificationForActivity(
                            context = context,
                            activityTitle = currentActivity.title,
                            activityId = activityId,
                            startTimeMillis = startTimeMillis,
                            userId = userId
                        )
                    } else if (!isRegistering) {
                        cancelNotificationForActivity(
                            context = context,
                            activityId = activityId
                        )
                    }
                }
            }
        }
    }

    private fun getActivityStartTimeInMillis(activity: Activity): Long? {
        val activityDate = activity.date?.toDate() ?: return null // Convert Timestamp to Date
        val startTime = activity.startTime

        return try {
            // Assuming startTime is in "HH:mm" format
            val timeParts = startTime.split(":")
            val calendar = Calendar.getInstance().apply {
                time = activityDate
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis // Return combined date and time in milliseconds
        } catch (e: Exception) {
            Log.e("ActivityDetailViewModel", "Invalid start time format: $startTime", e)
            null
        }
    }
}