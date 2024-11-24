package com.example.soclub.screens.ads

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.soclub.models.EditActivity
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityService
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class AdsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockActivityService = mockk<ActivityService>()
    private val mockAccountService = mockk<AccountService>()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    private lateinit var viewModel: AdsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AdsViewModel(
            context = mockContext,
            activityService = mockActivityService,
            accountService = mockAccountService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchActivitiesByCreator handles empty activities list`() = runTest {
        val mockCreatorId = "mockCreatorId"

        coEvery { mockAccountService.currentUserId } returns mockCreatorId
        coEvery { mockActivityService.getAllActivitiesByCreator(mockCreatorId) } returns emptyList()

        viewModel.fetchActivitiesByCreator()

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.first())
        assertTrue(viewModel.activities.first().isEmpty())
        assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `fetchActivitiesByCreator handles errors gracefully`() = runTest {
        val mockCreatorId = "mockCreatorId"
        val errorMessage = "An error occurred"

        coEvery { mockAccountService.currentUserId } returns mockCreatorId
        coEvery { mockActivityService.getAllActivitiesByCreator(mockCreatorId) } throws Exception("Test Error")
        coEvery { mockContext.getString(any()) } returns errorMessage

        viewModel.fetchActivitiesByCreator()

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.first())
        assertTrue(viewModel.activities.first().isEmpty())
        assertEquals(errorMessage, viewModel.errorMessage.first())
    }
}
