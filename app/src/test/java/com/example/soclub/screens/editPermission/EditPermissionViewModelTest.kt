package com.example.soclub.screens.editPermission

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import com.example.soclub.R
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditPermissionViewModelTest {

    private val mockContext = mockk<Context>(relaxed = true)
    private val viewModel = EditPermissionViewModel()
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
    fun `checkPermissions updates location permission correctly`() = runTest {
        every { ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION) } returns
                android.content.pm.PackageManager.PERMISSION_GRANTED

        viewModel.checkPermissions(mockContext)

        assertEquals(true, viewModel.locationPermission.value)
    }

    @Test
    fun `checkPermissions updates gallery permission correctly`() = runTest {
        every { ContextCompat.checkSelfPermission(mockContext, Manifest.permission.READ_EXTERNAL_STORAGE) } returns
                android.content.pm.PackageManager.PERMISSION_GRANTED

        viewModel.checkPermissions(mockContext)

        assertEquals(true, viewModel.galleryPermission.value)
    }

    @Test
    fun `checkPermissions updates notification permission correctly`() = runTest {
        every { ContextCompat.checkSelfPermission(mockContext, Manifest.permission.POST_NOTIFICATIONS) } returns
                android.content.pm.PackageManager.PERMISSION_GRANTED

        viewModel.checkPermissions(mockContext)

        assertEquals(true, viewModel.notificationPermission.value)
    }

    @Test
    fun `navigateToSettings starts settings intent`() {
        val intentSlot = slot<android.content.Intent>()

        every { mockContext.startActivity(capture(intentSlot)) } just Runs

        viewModel.navigateToSettings(mockContext)

        val capturedIntent = intentSlot.captured
        assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, capturedIntent.action)
        assertEquals("package:${mockContext.packageName}", capturedIntent.data.toString())
    }
}
