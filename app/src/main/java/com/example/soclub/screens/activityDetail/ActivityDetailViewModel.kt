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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.soclub.utils.cancelNotificationForActivity
import com.example.soclub.utils.enqueueSignUpNotification
import com.example.soclub.utils.enqueueUnregistrationNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.soclub.utils.scheduleNotificationForActivity
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar


@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountService: AccountService,
    private val activityDetailService: ActivityDetailService
) : ViewModel() {

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> = _activities

    private val _currentParticipantsMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val currentParticipantsMap: StateFlow<Map<String, Int>> = _currentParticipantsMap

    private var activitiesListener: ListenerRegistration? = null

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

    // Listener for sanntidsoppdateringer av registreringer
    private var registrationListener: ListenerRegistration? = null

    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity

    private var activityListener: ListenerRegistration? = null



    private suspend fun updateCurrentParticipantsMap(activities: List<Activity>) {
        val participantsMap = mutableMapOf<String, Int>()
        for (activity in activities) {
            val count = activityDetailService.getRegisteredParticipantsCount(activity.id)
            participantsMap[activity.id] = count
        }
        _currentParticipantsMap.value = participantsMap
    }

    override fun onCleared() {
        super.onCleared()
        registrationListener?.remove()
        activityListener?.remove()  // Fjern aktivitetens lytter
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
                _errorMessage.value = "Det skjedde en feil. Vennligst prøv igjen senere."
            } finally {
                _isLoading.value = false
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
                        scheduleNotificationForActivity(
                            context = context,
                            activityTitle = currentActivity.title,
                            activityId = activityId,
                            startTimeMillis = startTimeMillis,
                            userId = userId
                        )
                        enqueueSignUpNotification(context, currentActivity.title, userId)
                    } else if (!isRegistering) {
                        cancelNotificationForActivity(context, userId, activityId)
                        enqueueUnregistrationNotification(context, currentActivity.title, userId)
                    }
                }
            }
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
}