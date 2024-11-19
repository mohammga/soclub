package com.example.soclub.models

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {

    @Test
    fun testActivityInitialization() {
        val timestamp = Timestamp.now()
        val activity = Activity(
            id = "1",
            title = "Soccer Match",
            description = "A fun match",
            date = timestamp
        )
        assertEquals("1", activity.id)
        assertEquals("Soccer Match", activity.title)
        assertEquals("A fun match", activity.description)
        assertEquals(timestamp, activity.date)
    }

    @Test
    fun testCreateActivityInitialization() {
        val timestamp = Timestamp.now()
        val createActivity = CreateActivity(
            creatorId = "user123",
            title = "Soccer Match",
            description = "A fun match",
            date = timestamp
        )
        assertEquals("user123", createActivity.creatorId)
        assertEquals("Soccer Match", createActivity.title)
        assertEquals("A fun match", createActivity.description)
        assertEquals(timestamp, createActivity.date)
    }

    @Test
    fun testEditActivityInitialization() {
        val timestamp = Timestamp.now()
        val editActivity = EditActivity(
            id = "1",
            creatorId = "user123",
            title = "Soccer Match",
            description = "Updated description",
            date = timestamp
        )
        assertEquals("1", editActivity.id)
        assertEquals("user123", editActivity.creatorId)
        assertEquals("Soccer Match", editActivity.title)
        assertEquals("Updated description", editActivity.description)
        assertEquals(timestamp, editActivity.date)
    }

    @Test
    fun testUserInitialization() {
        val user = User(id = "user123", isAnonymous = false)
        assertEquals("user123", user.id)
        assertEquals(false, user.isAnonymous)
    }

    @Test
    fun testNotificationInitialization() {
        val timestamp = 123456789L
        val notification = Notification(userId = "user123", message = "Reminder message", timestamp = timestamp)
        assertEquals("user123", notification.userId)
        assertEquals("Reminder message", notification.message)
        assertEquals(timestamp, notification.timestamp)
    }

    @Test
    fun testUserInfoInitialization() {
        val userInfo = UserInfo(firstname = "John", lastname = "Doe", email = "john@example.com", age = 18)
        assertEquals("John", userInfo.firstname)
        assertEquals("Doe", userInfo.lastname)
        assertEquals("john@example.com", userInfo.email)
        assertEquals(18, userInfo.age)
    }
}
