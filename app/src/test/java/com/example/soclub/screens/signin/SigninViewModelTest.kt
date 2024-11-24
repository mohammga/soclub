package com.example.soclub.screens.signin

import android.content.Context
import androidx.navigation.NavController
import com.example.soclub.R
import com.example.soclub.service.AccountService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SigninViewModelTest {

    private val mockAccountService = mockk<AccountService>()
    private val mockNavController = mockk<NavController>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)
    private val viewModel = SigninViewModel(mockAccountService)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOnEmailChange() {
        viewModel.onEmailChange("TEST@EXAMPLE.COM")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun testOnPasswordChange() {
        viewModel.onPasswordChange("password123")
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun testOnLoginClickWithEmptyEmail() {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick(mockNavController, mockContext)

        assertEquals(R.string.error_email_required, viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun testOnLoginClickWithInvalidEmail() {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick(mockNavController, mockContext)

        assertEquals(R.string.error_invalid_email, viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun testOnLoginClickWithEmptyPassword() {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("")

        viewModel.onLoginClick(mockNavController, mockContext)

        assertEquals(R.string.error_password_required, viewModel.uiState.value.passwordError)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun testOnLoginClickSuccess() = runTest {
        coEvery { mockAccountService.authenticateWithEmail(any(), any(), any()) } answers {
            val callback = thirdArg<(String?) -> Unit>()
            callback.invoke(null)
        }

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick(mockNavController, mockContext)

    }

    @Test
    fun testOnLoginClickFailure() = runTest {
        coEvery { mockAccountService.authenticateWithEmail(any(), any(), any()) } answers {
            val callback = thirdArg<(String?) -> Unit>()
            callback.invoke("Error occurred")
        }

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick(mockNavController, mockContext)
    }

    @Test
    fun testLoadingStateDuringLogin() = runTest {
        coEvery { mockAccountService.authenticateWithEmail(any(), any(), any()) } answers {
            val callback = thirdArg<(String?) -> Unit>()
            assertTrue(viewModel.isLoading.value)
            callback.invoke(null)
            assertFalse(viewModel.isLoading.value)
        }

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick(mockNavController, mockContext)
    }

}
