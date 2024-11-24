package com.example.soclub.screens.profile



import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.models.UserInfo
import com.example.soclub.service.AccountService
import com.example.soclub.components.navigation.AppScreens
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val mockAccountService = mockk<AccountService>()
    private val mockNavController = mockk<NavController>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(mockAccountService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchUserInfo loads user info successfully`() = runTest {
        val mockUserInfo = UserInfo(
            firstname = "John",
            lastname = "Doe",
            email = "john.doe@example.com",
            imageUrl = "http://example.com/profile.jpg",
            age = 30
        )
        coEvery { mockAccountService.getUserInfo() } returns mockUserInfo

        viewModel = ProfileViewModel(mockAccountService)

        advanceUntilIdle()

        assertEquals(mockUserInfo, viewModel.userInfo)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `fetchUserInfo handles errors gracefully`() = runTest {
        coEvery { mockAccountService.getUserInfo() } throws RuntimeException("Error fetching user info")

        viewModel = ProfileViewModel(mockAccountService)

        advanceUntilIdle()

        assertNull(viewModel.userInfo)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `fetchUserInfo updates userInfo and sets isLoading to false`() = runTest {
        val mockUserInfo = UserInfo(
            firstname = "John",
            lastname = "Doe",
            age = 30,
            email = "john.doe@example.com",
            imageUrl = "https://example.com/image.jpg"
        )

        coEvery { mockAccountService.getUserInfo() } returns mockUserInfo

        viewModel.fetchUserInfo()

        advanceUntilIdle()

        assertEquals(mockUserInfo, viewModel.userInfo)
        assertFalse(viewModel.isLoading)
    }

}
