package com.example.soclub.screens.notification

import android.content.Context
import com.example.soclub.R
import com.example.soclub.models.Notification
import com.example.soclub.screens.notifications.NotificationsViewModel
import com.example.soclub.service.NotificationService
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val mockNotificationService = mockk<NotificationService>()
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock the getNotificationsStream() method
        every { mockNotificationService.getNotificationsStream() } returns flowOf(emptyList())

        // Initialize the ViewModel with mockFirebaseAuth
        viewModel = NotificationsViewModel(mockNotificationService, mockFirebaseAuth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init collects notifications stream and updates notifications`() = runTest {
        val mockNotifications = listOf(
            Notification(message = "Notification 1", timestamp = System.currentTimeMillis()),
            Notification(message = "Notification 2", timestamp = System.currentTimeMillis())
        )
        every { mockNotificationService.getNotificationsStream() } returns flowOf(mockNotifications)

        viewModel = NotificationsViewModel(mockNotificationService, mockFirebaseAuth)

        advanceUntilIdle()

        assertEquals(mockNotifications.sortedByDescending { it.timestamp }, viewModel.notifications.value)
    }

    @Test
    fun `loadNotifications fetches notifications and updates state`() = runTest {
        val mockNotifications = listOf(
            Notification(message = "Notification 1", timestamp = System.currentTimeMillis()),
            Notification(message = "Notification 2", timestamp = System.currentTimeMillis())
        )

        coEvery { mockNotificationService.getAllNotifications() } returns mockNotifications

        viewModel.loadNotifications(mockContext)

        advanceUntilIdle()

        assertEquals(mockNotifications, viewModel.notifications.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(null, viewModel.errorMessage.value)
    }

    @Test
    fun `loadNotifications handles errors`() = runTest {
        coEvery { mockNotificationService.getAllNotifications() } throws RuntimeException("Error")

        every { mockContext.getString(R.string.error_message) } returns "Error loading notifications"

        viewModel.loadNotifications(mockContext)

        advanceUntilIdle()

        assertEquals(emptyList<Notification>(), viewModel.notifications.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals("Error loading notifications", viewModel.errorMessage.value)
    }
}
