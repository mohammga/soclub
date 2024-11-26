package com.example.soclub.screens.changePassword

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockAccountService = mockk<AccountService>()
    private val mockContext = mockk<Context>(relaxed = true)

    private lateinit var viewModel: ChangePasswordViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChangePasswordViewModel(mockAccountService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onChangePasswordClick shows error for blank old password`() = runTest {
        viewModel.onNewPasswordChange("NewPassword1")
        viewModel.onConfirmPasswordChange("NewPassword1")

        viewModel.onChangePasswordClick(mockContext)

        assertEquals(R.string.error_old_password_required, viewModel.uiState.value.oldPasswordError)
        assertTrue(viewModel.isProcessing.value.not())
    }

    @Test
    fun `onChangePasswordClick shows error for invalid new password`() = runTest {
        viewModel.onOldPasswordChange("OldPassword")
        viewModel.onNewPasswordChange("short")
        viewModel.onConfirmPasswordChange("short")

        viewModel.onChangePasswordClick(mockContext)

        assertEquals(R.string.error_password_too_short, viewModel.uiState.value.newPasswordError)
        assertTrue(viewModel.isProcessing.value.not())
    }

    @Test
    fun `onChangePasswordClick shows error for mismatched passwords`() = runTest {
        viewModel.onOldPasswordChange("OldPassword")
        viewModel.onNewPasswordChange("ValidPassword1")
        viewModel.onConfirmPasswordChange("DifferentPassword")

        viewModel.onChangePasswordClick(mockContext)

        assertEquals(R.string.password_mismatch_error, viewModel.uiState.value.confirmPasswordError)
        assertTrue(viewModel.isProcessing.value.not())
    }



    @Test
    fun `onChangePasswordClick handles error from AccountService`() = runTest {
        viewModel.onOldPasswordChange("OldPassword")
        viewModel.onNewPasswordChange("ValidPassword1")
        viewModel.onConfirmPasswordChange("ValidPassword1")

        coEvery { mockAccountService.changePassword(any(), any()) } answers {
            lambda<(String?) -> Unit>().invoke("Error") // Simulerer feil fra AccountService
        }

        viewModel.onChangePasswordClick(mockContext)

        advanceUntilIdle()

        coVerify { mockAccountService.changePassword("OldPassword", "ValidPassword1") }
        assertEquals(R.string.error_could_not_change_password, viewModel.uiState.value.generalError)
        assertTrue(viewModel.isProcessing.value.not())
    }
}
