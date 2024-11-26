package com.example.soclub.screens.signup

import android.content.Context
import androidx.navigation.NavController
import com.example.soclub.components.navigation.AppScreens
import com.example.soclub.service.AccountService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {

    private val mockAccountService = mockk<AccountService>(relaxUnitFun = true)
    private val mockNavController = mockk<NavController>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)
    private val viewModel = SignupViewModel(mockAccountService)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        clearMocks(mockAccountService, mockNavController, mockContext)
        every { mockContext.getString(any()) } returns "Mock string"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test sign-up with valid inputs`() = runTest {
        mockSuccessfulSignUp()
        fillValidInputs()

        viewModel.onSignUpClick(mockNavController, mockContext)

        verify(exactly = 0) { mockNavController.navigate(AppScreens.HOME.name) }
        confirmVerified(mockNavController)
    }

    @Test
    fun `test sign-up with invalid email`() = runTest {
        mockSuccessfulSignUp()
        viewModel.onEmailChange("invalid-email")
        viewModel.onFirstNameChange("John")
        viewModel.onLastNameChange("Doe")
        viewModel.onAgeChange("30")
        viewModel.onPasswordChange("Password123")

        viewModel.onSignUpClick(mockNavController, mockContext)

        verify(exactly = 0) { mockNavController.navigate(AppScreens.HOME.name) }
        confirmVerified(mockNavController)
    }

    @Test
    fun `test sign-up with invalid first name`() = runTest {
        mockSuccessfulSignUp()
        viewModel.onEmailChange("test@example.com")
        viewModel.onFirstNameChange("")  // Testing empty first name
        viewModel.onLastNameChange("Doe")
        viewModel.onAgeChange("30")
        viewModel.onPasswordChange("Password123")

        viewModel.onSignUpClick(mockNavController, mockContext)

        verify(exactly = 0) { mockNavController.navigate(AppScreens.HOME.name) }
        confirmVerified(mockNavController)
    }

    @Test
    fun `test sign-up with invalid age`() = runTest {
        mockSuccessfulSignUp()
        viewModel.onEmailChange("test@example.com")
        viewModel.onFirstNameChange("John")
        viewModel.onLastNameChange("Doe")
        viewModel.onAgeChange("17")  // Age below legal threshold if 18 is minimum

        viewModel.onPasswordChange("Password123")

        viewModel.onSignUpClick(mockNavController, mockContext)

        verify(exactly = 0) { mockNavController.navigate(AppScreens.HOME.name) }
        confirmVerified(mockNavController)
    }

    @Test
    fun `test sign-up with password lacking requirements`() = runTest {
        mockSuccessfulSignUp()
        viewModel.onEmailChange("test@example.com")
        viewModel.onFirstNameChange("John")
        viewModel.onLastNameChange("Doe")
        viewModel.onAgeChange("30")
        viewModel onPasswordChange("pass")  // Too short and simple

        viewModel.onSignUpClick(mockNavController, mockContext)

        verify(exactly = 0) { mockNavController.navigate(AppScreens.HOME.name) }
        confirmVerified(mockNavController)
    }

    private fun mockSuccessfulSignUp() {
        coEvery {
            mockAccountService.createEmailAccount(any(), any(), any(), any(), any())
        } answers {
            val callback = lastArg<(String?) -> Unit>()
            callback.invoke(null) // Simulate success
        }
    }

    private fun fillValidInputs() {
        viewModel.onEmailChange("test@example.com")
        viewModel.onFirstNameChange("John")
        viewModel onLastNameChange("Doe")
        viewModel.onAgeChange("30")
        viewModel.onPasswordChange("Password123")
    }
}
