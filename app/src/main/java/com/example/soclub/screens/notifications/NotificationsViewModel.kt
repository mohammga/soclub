package com.example.soclub.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Notification(val timeAgo: String, val message: String)

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Funksjon for å laste varslinger
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Simulerer lasting av data med en forsinkelse
                delay(1000)
                _notifications.value = listOf(
                    Notification("2 minutter siden", "Du har fått en ny melding fra Cathrine."),
                    Notification("20 minutter siden", "Cathrine har meldt seg på arrangementet."),
                    Notification("1 time siden", "Vennen din skal også delta på arrangementet."),
                    Notification("I går", "Det er opprettet et nytt arrangement av vennen din.")
                )
            } catch (e: Exception) {
                _errorMessage.value = "Det skjedde en feil. Vennligst prøv igjen senere."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
