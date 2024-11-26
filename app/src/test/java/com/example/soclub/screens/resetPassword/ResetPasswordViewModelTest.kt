package com.example.soclub.screens.resetPassword

import android.content.Context
import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.soclub.R
import com.example.soclub.service.AccountService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockAccountService = mockk<AccountService>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)
    private lateinit var viewModel: ResetPasswordViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ResetPasswordViewModel(mockAccountService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEmailChange updates email and clears error`() {
        viewModel.onEmailChange("TEST@EXAMPLE.COM")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `onForgotPasswordClick shows error when email is blank`() {
        viewModel.onEmailChange("")
        viewModel.onForgotPasswordClick(mockContext)

        assertEquals(R.string.error_email_required, viewModel.uiState.value.emailError)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onForgotPasswordClick shows error for invalid email`() {
        viewModel.onEmailChange("invalid-email")
        viewModel.onForgotPasswordClick(mockContext)

        assertEquals(R.string.error_invalid_email, viewModel.uiState.value.emailError)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onForgotPasswordClick shows error when email reset fails`() = runTest {
        coEvery { mockAccountService.sendPasswordResetEmail(any()) } answers {
            val callback = secondArg<(String?) -> Unit>()
            callback.invoke("Error")
        }

        viewModel.onEmailChange("test@example.com")
        viewModel.onForgotPasswordClick(mockContext)

        advanceUntilIdle()

        assertEquals(R.string.error_could_not_send_reset_email, viewModel.uiState.value.statusMessage)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onForgotPasswordClick handles exceptions gracefully`() = runTest {
        coEvery { mockAccountService.sendPasswordResetEmail(any()) } throws RuntimeException("Exception occurred")

        viewModel.onEmailChange("test@example.com")
        viewModel.onForgotPasswordClick(mockContext)

        advanceUntilIdle()

        assertEquals(R.string.error_could_not_send_reset_email, viewModel.uiState.value.statusMessage)
        assertFalse(viewModel.isLoading.value)
    }
}
