package com.example.soclub.screens.activityDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.soclub.models.Activity
import com.example.soclub.models.UserInfo
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityDetailService
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockAccountService = mockk<AccountService>()
    private val mockActivityDetailService = mockk<ActivityDetailService>()
    private val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    private lateinit var viewModel: ActivityDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockAccountService.currentUserId } returns "mockUserId"
        viewModel = ActivityDetailViewModel(
            context = mockContext,
            accountService = mockAccountService,
            activityDetailService = mockActivityDetailService,
            firestore = mockFirestore
        )
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadActivityWithStatus successfully loads activity details`() = runTest {
        val mockActivity = Activity(
            id = "activity123",
            title = "Test Activity",
            creatorId = "user123",
            ageGroup = 18
        )
        every { mockAccountService.currentUserId } returns "user456"
        coEvery { mockActivityDetailService.getActivityById(any(), any()) } returns mockActivity
        coEvery { mockActivityDetailService.isUserRegisteredForActivity(any(), any()) } returns true
        coEvery { mockAccountService.getUserInfo() } returns UserInfo(firstname = "Abdullah", lastname = "Salha", age = 23, imageUrl = "url", email = "abdullahsalha3@gmail.com")
        coEvery { mockActivityDetailService.getRegisteredParticipantsCount("activity123") } returns 10


        viewModel.loadActivityWithStatus("category1", "activity123")

        advanceUntilIdle()

        assertEquals(mockActivity, viewModel.activity.first())
        assertTrue(viewModel.isRegistered.first())
        assertTrue(viewModel.canRegister.first())
    }

    @Test
    fun `test loadRegisteredParticipants updates participant count`() = runTest {
        coEvery { mockActivityDetailService.getRegisteredParticipantsCount(any()) } returns 15
        viewModel.loadRegisteredParticipants("activity123")
        advanceUntilIdle()
        assertEquals(15, viewModel.currentParticipants.first())
    }

}